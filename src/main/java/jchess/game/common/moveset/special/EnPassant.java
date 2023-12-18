package jchess.game.common.moveset.special;

import jchess.ecs.Entity;
import jchess.game.common.IChessGame;
import jchess.game.common.components.PieceComponent;
import jchess.game.common.components.PieceIdentifier;
import jchess.game.common.events.PieceMoveEvent;
import jchess.game.common.moveset.ISpecialRule;
import jchess.game.common.moveset.MoveIntention;
import jchess.game.el.TileExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnPassant implements ISpecialRule {
    private final IChessGame game;
    private final PieceIdentifier thisPawnId;
    private final int pawnTypeId;
    private final int[] pawnDoubleMoveDirections;
    private final int[] pawnCaptureDirections;
    private final Map<Integer, PieceMoveEvent.PieceMove> doubleMovesByPlayer = new HashMap<>();

    public EnPassant(IChessGame game, PieceIdentifier thisPawnId, int pawnTypeId, int[] pawnDoubleMoveDirections, int[] pawnCaptureDirections) {
        this.game = game;
        this.thisPawnId = thisPawnId;
        this.pawnTypeId = pawnTypeId;
        this.pawnDoubleMoveDirections = pawnDoubleMoveDirections;
        this.pawnCaptureDirections = pawnCaptureDirections;

        game.getEventManager().<PieceMoveEvent>getEvent(PieceMoveEvent.class).addListener(this::onPieceMove);
    }

    private void onPieceMove(PieceMoveEvent.PieceMove move) {
        PieceComponent movedPiece = move.toTile().piece;
        if (movedPiece.identifier.ownerId() == thisPawnId.ownerId()) {
            // player made a move -> the window for an EnPassant move has passed
            doubleMovesByPlayer.clear();
            return;
        }

        if (movedPiece.identifier.pieceTypeId() == pawnTypeId && move.moveType() == SpecialFirstMove.class) {
            // opponent has made a move that can be attacked with EnPassant
            doubleMovesByPlayer.put(movedPiece.identifier.ownerId(), move);
        }
    }

    @Override
    public List<MoveIntention> getSpecialMoves(Entity thisPawn) {
        if (doubleMovesByPlayer.isEmpty()) {
            return Collections.emptyList();
        }

        List<MoveIntention> result = new ArrayList<>();
        for (Map.Entry<Integer, PieceMoveEvent.PieceMove> doubleMove : doubleMovesByPlayer.entrySet()) {
            int doubleMovePlayer = doubleMove.getKey();
            PieceMoveEvent.PieceMove moveInfo = doubleMove.getValue();

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
        return result;
    }

    private Entity findEnPassantTargetTile(PieceMoveEvent.PieceMove doubleMove, PieceIdentifier doubleMovedPawn, Entity thisPawn) {
        for (int captureDirection : pawnCaptureDirections) {
            Entity captureTile = TileExpression.neighbor(captureDirection).compile(thisPawnId).findTiles(thisPawn).findFirst().orElse(null);
            if (captureTile == null || captureTile.piece != null) {
                // tile is out of bounds || tile can already be captured by normal move
                continue;
            }

            for (int doubleMoveDirection : pawnDoubleMoveDirections) {
                // enPassantTile must be reachable by doubleMovedPawn in 1 step, and must also be our captureTile
                Entity enPassantTile = TileExpression.filter(TileExpression.neighbor(doubleMoveDirection), tile -> tile == captureTile)
                        .compile(doubleMovedPawn).findTiles(doubleMove.fromTile())
                        .findFirst().orElse(null);
                if (enPassantTile == null) {
                    continue;
                }

                // enPassantTile must reach the moveResult-Tile in 1 step.
                boolean isValidEnPassant = TileExpression.neighbor(doubleMoveDirection)
                        .compile(doubleMovedPawn).findTiles(enPassantTile)
                        .anyMatch(tile -> tile == doubleMove.toTile());
                if (isValidEnPassant) {
                    return enPassantTile;
                }
            }
        }
        return null;
    }

    private MoveIntention getEnPassantMove(PieceMoveEvent.PieceMove doubleMove, Entity thisPawnFromTile, Entity thisPawnToTile) {
        return new MoveIntention(thisPawnToTile, () -> {
            doubleMove.toTile().piece = null;
            game.movePiece(thisPawnFromTile, thisPawnToTile, EnPassant.class);
        }, new EnPassantSimulator(doubleMove.toTile().piece, doubleMove.toTile(), thisPawnFromTile, thisPawnToTile));
    }

    private record EnPassantSimulator(
            PieceComponent capturedPiece, Entity capturedPieceTile,
            Entity moveFromTile, Entity moveToTile
    ) implements MoveIntention.IMoveSimulator {

        @Override
        public void simulate() {
            capturedPieceTile.piece = null;
            moveToTile.piece = moveFromTile.piece;
            moveFromTile.piece = null;
        }

        @Override
        public void revert() {
            capturedPieceTile.piece = capturedPiece;
            moveFromTile.piece = moveToTile.piece;
            moveToTile.piece = null;
        }
    }
}
