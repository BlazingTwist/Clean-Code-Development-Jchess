package jchess.el.v2;

import jchess.common.components.PieceIdentifier;
import jchess.el.CompiledTileExpression;

@FunctionalInterface
public interface ExpressionCompiler {
    TileExpression.CompiledExpression compile(PieceIdentifier movingPiece);

    default CompiledTileExpression toV1(PieceIdentifier movingPiece) {
        final TileExpression.CompiledExpression expression = compile(movingPiece);
        return new CompiledTileExpression(expression::apply);
    }
}
