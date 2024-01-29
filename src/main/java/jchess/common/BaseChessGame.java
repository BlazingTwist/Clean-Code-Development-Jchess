package jchess.common;

import dx.schema.types.MarkerType;
import dx.schema.types.PieceType;
import jchess.common.components.MarkerComponent;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.components.TileComponent;
import jchess.common.events.BoardClickedEvent;
import jchess.common.events.BoardInitializedEvent;
import jchess.common.events.GameOverEvent;
import jchess.common.events.OfferPieceSelectionEvent;
import jchess.common.events.PieceMoveEvent;
import jchess.common.events.PieceOfferSelectedEvent;
import jchess.common.events.RenderEvent;
import jchess.common.moveset.MoveIntention;
import jchess.common.state.IRevertibleState;
import jchess.common.state.StateManager;
import jchess.ecs.EcsEventManager;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;
import jchess.gamemode.IPieceLayoutProvider;
import jchess.gamemode.PieceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public abstract class BaseChessGame implements IChessGame {
    private static final Logger logger = LoggerFactory.getLogger(BaseChessGame.class);
    protected final EntityManager entityManager;
    protected final EcsEventManager eventManager;
    protected final StateManager stateManager;
    protected final int numPlayers;
    protected final PieceStore pieceStore;
    protected final IPieceLayoutProvider pieceLayout;

    protected int activePlayerId = 0;

    public BaseChessGame(int numPlayers, PieceStore pieceStore, IPieceLayoutProvider pieceLayout) {
        this.entityManager = new EntityManager();
        this.eventManager = new EcsEventManager();
        this.stateManager = new StateManager();
        this.numPlayers = numPlayers;
        this.pieceStore = pieceStore;
        this.pieceLayout = pieceLayout;

        eventManager.registerEvent(new GameOverEvent());
        eventManager.registerEvent(new RenderEvent());
        eventManager.registerEvent(new BoardInitializedEvent());
        eventManager.registerEvent(new PieceMoveEvent());

        BoardClickedEvent boardClickedEvent = new BoardClickedEvent();
        eventManager.registerEvent(boardClickedEvent);
        boardClickedEvent.addListener(point -> onBoardClicked(point.x, point.y));

        eventManager.registerEvent(new OfferPieceSelectionEvent());
        eventManager.registerEvent(new PieceOfferSelectedEvent());

        stateManager.registerState(new IRevertibleState() {
            @Override
            public void saveState() {
                // attackInfo is temporary data, no need to save it
            }

            @Override
            public void revertState() {
                // on revert, "simply" recompute the attackInfo
                TileComponent.updateAttackInfo(BaseChessGame.this);
            }
        });
    }

    protected abstract void generateBoard();

    protected abstract Entity getEntityAtPosition(int x, int y);

    protected abstract int getDirectionFromOwnerId(int ownerId);

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
            assert clickedMarker != null;
            if (clickedMarker.onMarkerClicked != null) {
                clickedMarker.onMarkerClicked.run();
            }
            eventManager.getEvent(RenderEvent.class).fire(null);
            return;
        }

        if (clickedEntity.piece != null) {
            long startTime = System.currentTimeMillis();

            // show the tiles this piece can move to
            boolean isActivePiece = clickedEntity.piece.identifier.ownerId() == activePlayerId;
            clickedEntity.findValidMoves(true).forEach(move -> createMoveToMarker(move, isActivePiece));
            createSelectionMarker(clickedEntity);

            long endTime = System.currentTimeMillis();
            logger.info("Computing valid moves with kingCheck took {} ms", endTime - startTime);
        }

        if (clickedEntity.tile != null) {
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
        /*
         * In 3- or more-Player chess, there is an edge case where the King may be captured.
         * This is possible through a discovered-check involving 2 players. (see https://greenchess.net/rules.php?type=three-player)
         * Solution: If the active player is able to capture any king, the active player wins immediately (the players whose king can be captured lose)
         */
        int[] playersLosingByCapture = entityManager.getEntities().stream()
                .filter(entity -> entity.piece != null && entity.tile != null
                                && entity.piece.identifier.pieceType() == PieceType.KING
                                && entity.piece.identifier.ownerId() != activePlayerId
                                && entity.isAttacked() // isAttack is a somewhat expensive operation, evaluate last
                                && entity.tile.attackingPieces.stream().anyMatch(attacker -> {
                            assert attacker.piece != null;
                            return attacker.piece.identifier.ownerId() == activePlayerId;
                        })
                )
                .mapToInt(entity -> entity.piece.identifier.ownerId())
                .toArray();
        if (playersLosingByCapture.length > 0) {
            logger.info("Game Over by {} capturable King(s)!", playersLosingByCapture.length);
            eventManager.getEvent(GameOverEvent.class).fire(MateResult.capturableKings(numPlayers, activePlayerId, playersLosingByCapture).score);
            return;
        }

        // Game is over if the current player is unable to make any moves.
        boolean gameOver = entityManager.getEntities().stream()
                .filter(entity -> entity.piece != null && entity.piece.identifier.ownerId() == activePlayerId)
                .allMatch(entity -> entity.findValidMoves(true).findAny().isEmpty());

        if (gameOver) {
            logger.info("Game Over! Losing Player: {}", activePlayerId);
            eventManager.getEvent(GameOverEvent.class).fire(computeMateResult().score);
        }

    }

    /**
     * <p> If the active player is not in check, the game ends in a draw. (Everyone receives 1 point).
     * <p> If the active player is in check, the player loses and receives 0 points.
     * <p> - the first player (based on the move-order) that could capture the king wins (receives 2 points), the other player receives 1 point.
     */
    protected MateResult computeMateResult() {
        Entity activeKing = entityManager.getEntities().stream()
                .filter(entity -> entity.piece != null && entity.piece.identifier.ownerId() == activePlayerId && entity.piece.identifier.pieceType() == PieceType.KING)
                .findFirst().orElseThrow(() -> new RuntimeException("Impossible! Player '" + activePlayerId + "' has no King!"));
        assert activeKing.tile != null;

        // check for draw
        if (!activeKing.isAttacked()) {
            return MateResult.draw(numPlayers);
        }

        // otherwise identify winning player:
        int nextPlayerId = activePlayerId;
        while (true) {
            nextPlayerId = (nextPlayerId + 1) % numPlayers;
            if (nextPlayerId == activePlayerId) {
                logger.error("Unable to find attacker of player '" + activePlayerId + "'.");
                return MateResult.draw(numPlayers);
            }

            final int checkPlayerId = nextPlayerId;
            boolean isAttackingKing = activeKing.tile.attackingPieces.stream()
                    .anyMatch(attacker -> {
                        assert attacker.piece != null;
                        return attacker.piece.identifier.ownerId() == checkPlayerId;
                    });
            if (isAttackingKing) {
                return MateResult.checkmate(numPlayers, nextPlayerId, activePlayerId);
            }
        }
    }

    protected void placePiece(Entity tile, int ownerId, int direction, PieceStore.IPieceDefinitionProvider pieceProvider) {
        PieceStore.PieceDefinition pieceDefinition = pieceProvider.getPieceDefinition();
        PieceIdentifier pieceIdentifier = new PieceIdentifier(
                pieceProvider.getPieceType(),
                pieceDefinition.shortName(),
                ownerId,
                direction
        );

        PieceComponent pieceComp = new PieceComponent(this, pieceIdentifier, pieceDefinition.baseMoves());
        pieceComp.addSpecialMoves(pieceDefinition.specialRules());
        tile.piece = pieceComp;
    }

    @Override
    public void start() {
        generateBoard();
        pieceLayout.placePieces(this, this::getEntityAtPosition);
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
    public StateManager getStateManager() {
        return stateManager;
    }

    @Override
    public int getActivePlayerId() {
        return activePlayerId;
    }

    @Override
    public void createPiece(Entity targetTile, PieceType pieceType, int ownerId) {
        PieceStore.IPieceDefinitionProvider pieceProvider = pieceStore.getPiece(pieceType);
        if (pieceProvider == null) {
            logger.error("unable to place piece with pieceType '" + pieceType + "'. PieceType does not exist.");
            return;
        }

        int direction = getDirectionFromOwnerId(ownerId);
        placePiece(targetTile, ownerId, direction, pieceProvider);
    }

    @Override
    public int getNumPlayers() {
        return numPlayers;
    }

    @Override
    public void notifyPieceMove(Entity fromTile, Entity toTile, Class<?> moveType) {
        eventManager.getEvent(PieceMoveEvent.class).fire(new PieceMoveEvent.PieceMove(fromTile, toTile, moveType));
        TileComponent.updateAttackInfo(this);
    }

    @Override
    public void endTurn() {
        activePlayerId = (activePlayerId + 1) % numPlayers;
        checkGameOver();
    }

    public record MateResult(Integer[] score) {
        public static MateResult draw(int numPlayers) {
            Integer[] score = new Integer[numPlayers];
            Arrays.fill(score, 1);
            return new MateResult(score);
        }

        public static MateResult checkmate(int numPlayers, int winningPlayer, int losingPlayer) {
            Integer[] score = new Integer[numPlayers];
            Arrays.fill(score, 1);
            score[winningPlayer] = 2;
            score[losingPlayer] = 0;
            return new MateResult(score);
        }

        public static MateResult capturableKings(int numPlayers, int winningPlayer, int[] losingPlayers) {
            Integer[] score = new Integer[numPlayers];
            Arrays.fill(score, 1);
            score[winningPlayer] = 2;
            for (Integer losingPlayer : losingPlayers) {
                score[losingPlayer] = 0;
            }
            return new MateResult(score);
        }
    }
}
