package jchess.common.moveset.special;

import dx.schema.types.PieceType;
import jchess.common.IChessGame;
import jchess.common.components.PieceIdentifier;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.el.CompiledTileExpression;
import jchess.el.v2.ExpressionCompiler;
import java.util.Set;
import java.util.stream.Stream;

public class ShapeShifting implements ISpecialRule {

    private final IChessGame game;
    private final PieceIdentifier shiftingPieceId;
    private final Set<PieceType> shiftablePieceTypes; // pieces that can be used for shape-shifting
    private final CompiledTileExpression shiftableExpressions;

    public ShapeShifting(IChessGame game, PieceIdentifier shiftingPieceId, ExpressionCompiler shiftableExpressions, PieceType... shiftablePieceTypes) {
        this.game = game;
        this.shiftingPieceId = shiftingPieceId;
        this.shiftablePieceTypes = Set.of(shiftablePieceTypes);
        this.shiftableExpressions = shiftableExpressions.toV1(shiftingPieceId);
    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity movingPiece, Stream<MoveIntention> currentMoves) {
        assert movingPiece.piece != null;
        Stream<Entity> targetTiles = shiftableExpressions.findTiles(movingPiece);
        return Stream.concat(currentMoves, targetTiles
                .filter(move -> move.tile != null
                        && move.piece != null
                        && move.piece.identifier.ownerId() != movingPiece.piece.identifier.ownerId()
                        && shiftablePieceTypes.contains(move.piece.identifier.pieceType()))
                .map(move -> new MoveIntention(
                        move,
                        () -> {
                            int owner = movingPiece.piece.identifier.ownerId();
                            game.createPiece(movingPiece, move.piece.identifier.pieceType(), owner);
                            game.movePieceStationary(movingPiece, ShapeShifting.class);
                        },
                        new ShapeShiftingSimulator(movingPiece, move.piece.identifier.pieceType(), shiftingPieceId.pieceType(), game)
                )));
    }

    private record ShapeShiftingSimulator(Entity shiftingPieceTile,
                                          PieceType attackedPieceType,
                                          PieceType originalPieceType,
                                          IChessGame game) implements MoveIntention.IMoveSimulator {
        @Override
        public void simulate() {
            assert shiftingPieceTile.piece != null;
            int owner = shiftingPieceTile.piece.identifier.ownerId();
            game.createPiece(shiftingPieceTile, attackedPieceType, owner);
        }

        @Override
        public void revert() {
            assert shiftingPieceTile.piece != null;
            int owner = shiftingPieceTile.piece.identifier.ownerId();
            game.createPiece(shiftingPieceTile, originalPieceType, owner);
        }
    }
}