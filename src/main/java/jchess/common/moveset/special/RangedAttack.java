package jchess.common.moveset.special;

import jchess.common.IChessGame;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.el.CompiledTileExpression;
import jchess.el.v2.ExpressionCompiler;
import jchess.el.v2.TileExpression;

import java.util.stream.Stream;

public class RangedAttack implements ISpecialRule {
    private final IChessGame game;
    private final CompiledTileExpression movement;

    public RangedAttack(IChessGame game, PieceIdentifier thisRangedPieceID, int minRange, int maxRange, boolean includeDiagonals) {
        if (minRange < 0) throw new IllegalArgumentException("argument 'minRange' may not be negative. Got '" + minRange + "'");
        if (minRange > maxRange)
            throw new IllegalArgumentException("argument 'minRange' may not be greater than 'maxRange'. minRange= '" + minRange + "', maxRange= '" + maxRange + "'");

        this.game = game;

        final ExpressionCompiler baseMovesUnfiltered;
        if (includeDiagonals) {
            baseMovesUnfiltered = TileExpression.or(
                    TileExpression.repeat(TileExpression.regex("0 30 60", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("60 90 120", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("120 150 180", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("180 210 240", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("240 270 300", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("0 300 330", true), minRange, maxRange, true)
            );
        } else {
            baseMovesUnfiltered = TileExpression.or(
                    TileExpression.repeat(TileExpression.regex("30 90", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("90 150", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("150 210", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("210 270", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("270 330", true), minRange, maxRange, true),
                    TileExpression.repeat(TileExpression.regex("30 330", true), minRange, maxRange, true)
            );
        }

        movement = TileExpression.filter2(baseMovesUnfiltered, TileExpression.FILTER_CAPTURE).toV1(thisRangedPieceID);
    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity thisRangedPiece, Stream<MoveIntention> baseMoves) {
        return Stream.concat(
                baseMoves,
                movement.findTiles(thisRangedPiece)
                        .map(targetTile -> getRangedAttackMove(thisRangedPiece, targetTile))
        );
    }

    private MoveIntention getRangedAttackMove(Entity thisRangedPiece, Entity targetTile) {
        return new MoveIntention(
                targetTile,
                () -> {
                    targetTile.piece = null;
                    game.movePieceStationary(thisRangedPiece, RangedAttack.class);
                },
                new RangedAttackSimulator(targetTile.piece, targetTile)
        );
    }

    private record RangedAttackSimulator(
            PieceComponent attackedPiece, Entity attackedPieceTile
    ) implements MoveIntention.IMoveSimulator {

        @Override
        public void simulate() {
            attackedPieceTile.piece = null;
        }

        @Override
        public void revert() {
            attackedPieceTile.piece = attackedPiece;
        }
    }
}
