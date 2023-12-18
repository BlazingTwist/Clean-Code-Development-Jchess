package jchess.game.common;

import jchess.ecs.EcsEventManager;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;
import jchess.game.common.components.MarkerComponent;
import jchess.game.common.components.MarkerType;
import jchess.game.common.components.TileComponent;
import jchess.game.common.events.BoardClickedEvent;
import jchess.game.common.events.BoardInitializedEvent;
import jchess.game.common.events.ComputeAttackInfoEvent;
import jchess.game.common.events.PieceMoveEvent;
import jchess.game.common.events.RenderEvent;
import jchess.game.common.moveset.MoveIntention;
import jchess.game.common.theme.IIconKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseChessGame implements IChessGame {
    private static final Logger logger = LoggerFactory.getLogger(BaseChessGame.class);
    protected final EntityManager entityManager;
    protected final EcsEventManager eventManager;
    protected final int numPlayers;

    protected int activePlayerId = 0;

    public BaseChessGame(int numPlayers) {
        this.entityManager = new EntityManager();
        this.eventManager = new EcsEventManager();
        this.numPlayers = numPlayers;

        eventManager.registerEvent(new RenderEvent());
        eventManager.registerEvent(new BoardInitializedEvent());
        eventManager.registerEvent(new PieceMoveEvent());

        ComputeAttackInfoEvent computeAttackInfoEvent = new ComputeAttackInfoEvent();
        eventManager.registerEvent(computeAttackInfoEvent);
        computeAttackInfoEvent.addListener(_void -> TileComponent.updateAttackInfo(this));

        BoardClickedEvent boardClickedEvent = new BoardClickedEvent();
        eventManager.registerEvent(boardClickedEvent);
        boardClickedEvent.addListener(vector -> onBoardClicked(vector.getX(), vector.getY()));
    }

    protected abstract void generateBoard();

    protected abstract Entity getEntityAtPosition(int x, int y);

    protected abstract IIconKey getMarkerIcon(MarkerType markerType);

    protected void onBoardClicked(int x, int y) {
        Entity clickedEntity = getEntityAtPosition(x, y);
        if (clickedEntity == null) {
            return;
        }

        MarkerComponent clickedMarker = clickedEntity.marker;
        // delete all markers
        for (Entity entity : entityManager.getEntities()) {
            entity.marker = null;
        }

        if (markerShouldConsumeClick(clickedMarker)) {
            if (clickedMarker.onMarkerClicked != null) {
                clickedMarker.onMarkerClicked.run();
            }
        } else if (clickedEntity.piece != null) {
            long startTime = System.currentTimeMillis();

            // show the tiles this piece can move to
            boolean isActivePiece = clickedEntity.piece.identifier.ownerId() == activePlayerId;
            clickedEntity.findValidMoves(true).forEach(move -> createMoveToMarker(move, isActivePiece));
            createSelectionMarker(clickedEntity);

            long endTime = System.currentTimeMillis();
            logger.info("Computing valid moves with kingCheck took {} ms", endTime - startTime);
        } else if (clickedEntity.tile != null) {
            // show which pieces can move to the selected tile
            for (Entity attacker : clickedEntity.tile.attackingPieces) {
                createMoveFromMarker(attacker);
            }
            createSelectionMarker(clickedEntity);
        }

        eventManager.getEvent(RenderEvent.class).fire(null);
    }

    protected boolean markerShouldConsumeClick(MarkerComponent marker) {
        if (marker == null) return false;
        if (marker.onMarkerClicked != null) return true;
        if (marker.markerType == MarkerType.Selection) return true; // click consumed to hide all markers
        return false;
    }

    protected void createSelectionMarker(Entity selectedTile) {
        MarkerComponent marker = new MarkerComponent(this::getMarkerIcon);
        marker.onMarkerClicked = null;
        marker.markerType = MarkerType.Selection;
        selectedTile.marker = marker;
    }

    protected void createMoveToMarker(MoveIntention moveIntention, boolean isActivePiece) {
        MarkerComponent marker = new MarkerComponent(this::getMarkerIcon);
        marker.onMarkerClicked = isActivePiece ? moveIntention.onClick() : null;
        marker.markerType = isActivePiece ? MarkerType.YesAction : MarkerType.NoAction;
        moveIntention.displayTile().marker = marker;
    }

    protected void createMoveFromMarker(Entity fromTile) {
        MarkerComponent marker = new MarkerComponent(this::getMarkerIcon);
        marker.onMarkerClicked = null;
        marker.markerType = MarkerType.NoAction;
        fromTile.marker = marker;
    }

    @Override
    public void start() {
        generateBoard();
        eventManager.getEvent(BoardInitializedEvent.class).fire(null);
        eventManager.getEvent(RenderEvent.class).fire(null);
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public EcsEventManager getEventManager() {
        return eventManager;
    }

    @Override
    public int getActivePlayerId() {
        return activePlayerId;
    }

    @Override
    public void movePiece(Entity fromTile, Entity toTile, Class<?> moveType) {
        toTile.piece = fromTile.piece;
        fromTile.piece = null;

        eventManager.<PieceMoveEvent>getEvent(PieceMoveEvent.class).fire(new PieceMoveEvent.PieceMove(fromTile, toTile, moveType));
        eventManager.getEvent(ComputeAttackInfoEvent.class).fire(null);

        // end turn
        activePlayerId = (activePlayerId + 1) % numPlayers;
    }
}
