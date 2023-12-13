package jchess.game.common;

import jchess.ecs.EcsEventManager;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;
import jchess.game.common.events.BoardClickedEvent;
import jchess.game.common.events.PieceMoveEvent;
import jchess.game.common.events.RenderEvent;
import jchess.game.common.components.MarkerComponent;
import jchess.game.common.components.MarkerType;
import jchess.game.common.theme.IIconKey;

public abstract class BaseChessGame implements IChessGame {
    protected final EntityManager entityManager;
    protected final EcsEventManager eventManager;
    protected final int numPlayers;

    protected int activePlayerId = 0;

    public BaseChessGame(int numPlayers) {
        this.entityManager = new EntityManager();
        this.eventManager = new EcsEventManager();
        this.numPlayers = numPlayers;

        eventManager.registerEvent(new RenderEvent());
        eventManager.registerEvent(new PieceMoveEvent());

        BoardClickedEvent boardClickedEvent = new BoardClickedEvent();
        eventManager.registerEvent(boardClickedEvent);
        boardClickedEvent.addPostEventListener(vector -> onBoardClicked(vector.getX(), vector.getY()));
    }

    public abstract void start();

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
            // show the tiles this piece can move to
            boolean isActivePiece = clickedEntity.piece.identifier.ownerId() == activePlayerId;
            clickedEntity.findValidMoves().forEach(validMove -> createMoveMarker(clickedEntity, validMove, isActivePiece));
            createSelectionMarker(clickedEntity);
        } else if (clickedEntity.tile != null) {
            // show which pieces can move to the selected tile
            entityManager.getEntities().stream()
                    .filter(entity -> entity.piece != null
                            && entity.tile != null
                            && entity.findValidMoves().anyMatch(move -> move == clickedEntity))
                    .forEach(attacker -> {
                        createMoveMarker(clickedEntity, attacker, false);
                    });
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

    protected void createMoveMarker(Entity fromTile, Entity toTile, boolean isActivePiece) {
        MarkerComponent marker = new MarkerComponent(this::getMarkerIcon);
        marker.onMarkerClicked = isActivePiece ? () -> movePiece(fromTile, toTile) : null;
        marker.markerType = isActivePiece ? MarkerType.YesAction : MarkerType.NoAction;
        toTile.marker = marker;
    }

    protected void movePiece(Entity fromTile, Entity toTile) {
        toTile.piece = fromTile.piece;
        fromTile.piece = null;

        // end turn
        activePlayerId = (activePlayerId + 1) % numPlayers;
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
}
