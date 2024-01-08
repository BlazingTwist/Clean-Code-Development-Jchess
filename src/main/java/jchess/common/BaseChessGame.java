package jchess.common;

import dx.schema.types.MarkerType;
import jchess.common.components.MarkerComponent;
import jchess.common.components.TileComponent;
import jchess.common.events.BoardClickedEvent;
import jchess.common.events.BoardInitializedEvent;
import jchess.common.events.ComputeAttackInfoEvent;
import jchess.common.events.GameOverEvent;
import jchess.common.events.OfferPieceSelectionEvent;
import jchess.common.events.PieceMoveEvent;
import jchess.common.events.PieceOfferSelectedEvent;
import jchess.common.events.RenderEvent;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.EcsEventManager;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;
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

        eventManager.registerEvent(new GameOverEvent());
        eventManager.registerEvent(new RenderEvent());
        eventManager.registerEvent(new BoardInitializedEvent());
        eventManager.registerEvent(new PieceMoveEvent());

        ComputeAttackInfoEvent computeAttackInfoEvent = new ComputeAttackInfoEvent();
        eventManager.registerEvent(computeAttackInfoEvent);
        computeAttackInfoEvent.addListener(_void -> TileComponent.updateAttackInfo(this));

        BoardClickedEvent boardClickedEvent = new BoardClickedEvent();
        eventManager.registerEvent(boardClickedEvent);
        boardClickedEvent.addListener(point -> onBoardClicked(point.x, point.y));

        eventManager.registerEvent(new OfferPieceSelectionEvent());
        eventManager.registerEvent(new PieceOfferSelectedEvent());
    }

    protected abstract void generateBoard();

    protected abstract Entity getEntityAtPosition(int x, int y);

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
        if (marker.markerType == MarkerType.SELECTED) return true; // click consumed to hide all markers
        return false;
    }

    protected void createSelectionMarker(Entity selectedTile) {
        MarkerComponent marker = new MarkerComponent();
        marker.onMarkerClicked = null;
        marker.markerType = MarkerType.SELECTED;
        selectedTile.marker = marker;
    }

    protected void createMoveToMarker(MoveIntention moveIntention, boolean isActivePiece) {
        MarkerComponent marker = new MarkerComponent();
        marker.onMarkerClicked = isActivePiece ? moveIntention.onClick() : null;
        marker.markerType = isActivePiece ? MarkerType.YES_ACTION : MarkerType.NO_ACTION;
        moveIntention.displayTile().marker = marker;
    }

    protected void createMoveFromMarker(Entity fromTile) {
        MarkerComponent marker = new MarkerComponent();
        marker.onMarkerClicked = null;
        marker.markerType = MarkerType.NO_ACTION;
        fromTile.marker = marker;
    }

    protected void checkGameOver() {
        boolean gameOver = entityManager.getEntities().stream()
                .filter(entity -> entity.piece != null && entity.piece.identifier.ownerId() == activePlayerId)
                .allMatch(entity -> entity.findValidMoves(true).findAny().isEmpty());

        if (gameOver) {
            logger.info("Game Over! Losing Player: {}", activePlayerId);
            eventManager.getEvent(GameOverEvent.class).fire(null);

            // TODO erja, compute player scores
            // TODO erja, send game over event to frontend
        }

    }

    @Override
    public void start() {
        generateBoard();
        eventManager.getEvent(BoardInitializedEvent.class).fire(null);
        eventManager.getEvent(RenderEvent.class).fire(null);
        checkGameOver();
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
        checkGameOver();
    }
}
