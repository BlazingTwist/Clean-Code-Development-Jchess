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

    public RangedAttack(IChessGame game, PieceIdentifier thisRangedPieceID, ExpressionCompiler attackExpression) {
        this.game = game;
        movement = TileExpression.filter2(attackExpression, TileExpression.FILTER_CAPTURE).toV1(thisRangedPieceID);
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
