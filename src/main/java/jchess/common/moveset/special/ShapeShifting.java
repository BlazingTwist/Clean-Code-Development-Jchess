package jchess.common.moveset.special;

import dx.schema.types.PieceType;
import jchess.common.IChessGame;
import jchess.common.components.PieceIdentifier;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.el.v2.TileExpression;

import java.util.List;
import java.util.stream.Stream;

public class ShapeShifting implements ISpecialRule {

    private final IChessGame game;
    private final PieceIdentifier shiftingPieceId;
    private final int minRange;
    private final int maxRange;
    private final List<PieceType> shiftablePieceTypes; // pieces that can be used for shape-shifting

    public ShapeShifting(IChessGame game, PieceIdentifier shiftingPieceId, int minRange, int maxRange, PieceType... shiftablePieceTypes) {
        this.game = game;
        this.shiftingPieceId = shiftingPieceId;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.shiftablePieceTypes = List.of(shiftablePieceTypes);
    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity movingPiece, Stream<MoveIntention> currentMoves) {
        assert movingPiece.piece != null;
        Stream<Entity> targetTiles = TileExpression.or(
                        TileExpression.repeat(TileExpression.regex("0 30 60", true), minRange, maxRange, true),
                        TileExpression.repeat(TileExpression.regex("60 90 120", true), minRange, maxRange, true),
                        TileExpression.repeat(TileExpression.regex("120 150 180", true), minRange, maxRange, true),
                        TileExpression.repeat(TileExpression.regex("180 210 240", true), minRange, maxRange, true),
                        TileExpression.repeat(TileExpression.regex("240 270 300", true), minRange, maxRange, true),
                        TileExpression.repeat(TileExpression.regex("0 300 330", true), minRange, maxRange, true))
                .toV1(shiftingPieceId).findTiles(movingPiece);

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
