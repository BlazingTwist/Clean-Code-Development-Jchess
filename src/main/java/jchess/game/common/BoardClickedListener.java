package jchess.game.common;

import jchess.ecs.EcsEvent;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;
import jchess.game.common.marker.MarkerComponent;
import jchess.game.common.marker.MarkerType;

import java.util.function.BiConsumer;

public class BoardClickedListener {
    private final IGameState gameState;
    private final EntityManager entityManager;
    private final EcsEvent<Void> renderEvent;
    public final BiConsumer<Entity, Entity> moveEventListener;

    public BoardClickedListener(
            IGameState gameState,
            EntityManager entityManager,
            EcsEvent<Void> renderEvent,
            BiConsumer<Entity, Entity> moveEventListener
    ) {
        this.gameState = gameState;
        this.entityManager = entityManager;
        this.renderEvent = renderEvent;
        this.moveEventListener = moveEventListener;
    }

    public void onClick(Entity tile) {
        MarkerComponent clickedMarker = tile.marker;
        // delete all markers
        for (Entity entity : entityManager.getEntities()) {
            entity.marker = null;
        }

        if (markerShouldConsumeClick(clickedMarker)) {
            if (clickedMarker.onMarkerClicked != null) {
                clickedMarker.onMarkerClicked.run();
            }
        } else if (tile.piece != null) {
            // show the tiles this piece can move to
            boolean isActivePiece = tile.piece.identifier.ownerId() == gameState.activePlayerId();
            tile.findValidMoves().forEach(validMove -> createMoveMarker(tile, validMove, isActivePiece));
            createSelectionMarker(tile);
        } else if (tile.tile != null) {
            // show which pieces can move to the selected tile
            entityManager.getEntities().stream()
                    .filter(entity -> entity.piece != null
                            && entity.tile != null
                            && entity.findValidMoves().anyMatch(x -> x == tile))
                    .forEach(attacker -> {
                        createMoveMarker(tile, attacker, false);
                    });
            createSelectionMarker(tile);
        }

        renderEvent.fire(null);
    }

    private boolean markerShouldConsumeClick(MarkerComponent marker) {
        if (marker == null) return false;
        if (marker.onMarkerClicked != null) return true;
        if (marker.markerType == MarkerType.Selection) return true; // click consumed to hide all markers
        return false;
    }

    private void createSelectionMarker(Entity selectedTile) {
        MarkerComponent marker = new MarkerComponent();
        marker.onMarkerClicked = null;
        marker.markerType = MarkerType.Selection;
        selectedTile.marker = marker;
    }

    private void createMoveMarker(Entity fromTile, Entity toTile, boolean isActivePiece) {
        MarkerComponent marker = new MarkerComponent();
        marker.onMarkerClicked = isActivePiece ? () -> movePiece(fromTile, toTile) : null;
        marker.markerType = isActivePiece ? MarkerType.YesAction : MarkerType.NoAction;
        toTile.marker = marker;
    }

    private void movePiece(Entity fromTile, Entity toTile) {
        toTile.piece = fromTile.piece;
        fromTile.piece = null;
        moveEventListener.accept(fromTile, toTile);
    }
}
