package jchess.common.moveset.special;

import dx.schema.types.PieceType;
import jchess.common.IChessGame;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.events.PieceMoveEvent;
import jchess.common.events.PieceMoveEvent.PieceMove;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.common.state.impl.ArrayState;
import jchess.ecs.Entity;
import jchess.el.CompiledTileExpression;
import jchess.el.v2.TileExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class EnPassant implements ISpecialRule {
    private final IChessGame game;
    private final PieceIdentifier thisPawnId;
    private final PieceType pawnTypeId;
    private final int[] pawnDoubleMoveDirections;
    private final CompiledTileExpression captureTiles;
    private final PieceMove[] doubleMovesByPlayer;

    public EnPassant(IChessGame game, PieceIdentifier thisPawnId, PieceType pawnTypeId, int[] pawnDoubleMoveDirections, int[] pawnCaptureDirections) {
        this.game = game;
        this.thisPawnId = thisPawnId;
        this.pawnTypeId = pawnTypeId;
        this.pawnDoubleMoveDirections = pawnDoubleMoveDirections;
        this.captureTiles = TileExpression.neighbor(pawnCaptureDirections).toV1(thisPawnId);

        ArrayState<PieceMove> movesState = new ArrayState<>(game.getNumPlayers(), PieceMove[]::new);
        game.getStateManager().registerState(movesState);
        doubleMovesByPlayer = movesState.getCurrent();

        game.getEventManager().getEvent(PieceMoveEvent.class).addListener(this::onPieceMove);
    }

    private void onPieceMove(PieceMove move) {
        assert move.toTile().piece != null; // move always contains moved piece in toTile
        PieceComponent movedPiece = move.toTile().piece;
        if (movedPiece.identifier.ownerId() == thisPawnId.ownerId()) {
            // player made a move -> the window for an EnPassant move has passed
            Arrays.fill(doubleMovesByPlayer, null);
            return;
        }

        if (movedPiece.identifier.pieceType() == pawnTypeId && move.moveType() == SpecialFirstMove.class) {
            // opponent has made a move that can be attacked with EnPassant
            doubleMovesByPlayer[movedPiece.identifier.ownerId()] = move;
        }
    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity thisPawn, Stream<MoveIntention> baseMoves) {
        List<MoveIntention> result = new ArrayList<>();
        for (int doubleMovePlayer = 0; doubleMovePlayer < doubleMovesByPlayer.length; doubleMovePlayer++) {
            PieceMove moveInfo = doubleMovesByPlayer[doubleMovePlayer];
            if (moveInfo == null) {
                continue;
            }

            if (moveInfo.toTile().piece == null) {
                // Might happen if another player took the moved pawn by EnPassant
                continue;
            }
            if (moveInfo.toTile().piece.identifier.ownerId() != doubleMovePlayer) {
                // Might happen if the doubleMoved pawn was taken directly by another player
                continue;
            }

            Entity targetTile = findEnPassantTargetTile(moveInfo, moveInfo.toTile().piece.identifier, thisPawn);
            if (targetTile != null) {
                result.add(getEnPassantMove(moveInfo, thisPawn, targetTile));
            }
        }
        return result.isEmpty() ? baseMoves : Stream.concat(baseMoves, result.stream());
    }

    private Entity findEnPassantTargetTile(PieceMove doubleMove, PieceIdentifier doubleMovedPawn, Entity thisPawn) {
        for (Entity captureTile : captureTiles.findTiles(thisPawn).toList()) {
            if (captureTile == null || captureTile.piece != null) {
                // tile is out of bounds || tile can already be captured by normal move
                continue;
            }

            for (int doubleMoveDirection : pawnDoubleMoveDirections) {
                // enPassantTile must be reachable by doubleMovedPawn in 1 step, and must also be our captureTile
                Entity enPassantTile = TileExpression.filter(TileExpression.neighbor(doubleMoveDirection), entity -> entity == captureTile)
                        .toV1(doubleMovedPawn)
                        .findTiles(doubleMove.fromTile())
                        .findFirst().orElse(null);
                if (enPassantTile == null) {
                    continue;
                }

                // enPassantTile must reach the moveResult-Tile in 1 step.
                boolean isValidEnPassant = TileExpression.neighbor(doubleMoveDirection)
                        .toV1(doubleMovedPawn)
                        .findTiles(enPassantTile)
                        .anyMatch(tile -> tile == doubleMove.toTile());
                if (isValidEnPassant) {
                    return enPassantTile;
                }
            }
        }
        return null;
    }

    private MoveIntention getEnPassantMove(PieceMove doubleMove, Entity thisPawnFromTile, Entity thisPawnToTile) {
        EnPassantSimulator simulator = new EnPassantSimulator(
                game,
                doubleMove.toTile().piece, doubleMove.toTile(),
                thisPawnFromTile, thisPawnToTile
        );
        return MoveIntention.fromMoveSimulator(game, thisPawnToTile, simulator);
    }

    private record EnPassantSimulator(
            IChessGame game,
            PieceComponent capturedPiece, Entity capturedPieceTile,
            Entity moveFromTile, Entity moveToTile
    ) implements MoveIntention.IMoveSimulator {

        @Override
        public void simulate() {
            capturedPieceTile.piece = null;
            moveToTile.piece = moveFromTile.piece;
            moveFromTile.piece = null;
            game.notifyPieceMove(moveFromTile, moveToTile, EnPassant.class);
        }

        @Override
        public void revert() {
            capturedPieceTile.piece = capturedPiece;
            moveFromTile.piece = moveToTile.piece;
            moveToTile.piece = null;
        }
    }
}
