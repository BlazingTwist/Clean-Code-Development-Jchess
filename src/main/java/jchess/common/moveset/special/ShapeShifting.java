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
                .filter(target -> target.tile != null
                        && target.piece != null
                        && target.piece.identifier.ownerId() != movingPiece.piece.identifier.ownerId()
                        && shiftablePieceTypes.contains(target.piece.identifier.pieceType()))
                .map(target -> {
                    ShapeShiftingSimulator simulator = new ShapeShiftingSimulator(
                            game, movingPiece, target.piece.identifier.pieceType(), shiftingPieceId.pieceType()
                    );
                    return MoveIntention.fromMoveSimulator(game, target, simulator);
                }));
    }

    private record ShapeShiftingSimulator(
            IChessGame game,
            Entity shiftingPieceTile,
            PieceType attackedPieceType,
            PieceType originalPieceType
    ) implements MoveIntention.IMoveSimulator {
        @Override
        public void simulate() {
            assert shiftingPieceTile.piece != null;
            int owner = shiftingPieceTile.piece.identifier.ownerId();
            game.createPiece(shiftingPieceTile, attackedPieceType, owner);
            game.notifyPieceMove(shiftingPieceTile, shiftingPieceTile, ShapeShifting.class);
        }

        @Override
        public void revert() {
            assert shiftingPieceTile.piece != null;
            int owner = shiftingPieceTile.piece.identifier.ownerId();
            game.createPiece(shiftingPieceTile, originalPieceType, owner);
        }
    }
}
