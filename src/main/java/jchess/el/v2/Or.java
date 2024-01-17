package jchess.el.v2;

import jchess.common.components.PieceIdentifier;
import jchess.ecs.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

final class Or implements ExpressionCompiler {
    private final ExpressionCompiler[] orCompilers;

    public Or(ExpressionCompiler... orCompilers) {
        this.orCompilers = orCompilers;
    }

    @Override
    public TileExpression.CompiledExpression compile(PieceIdentifier movingPiece) {
        final TileExpression.CompiledExpression[] expressions = Arrays.stream(orCompilers).map(compiler -> compiler.compile(movingPiece)).toArray(TileExpression.CompiledExpression[]::new);
        if (expressions.length == 0) {
            return startTiles -> Stream.empty();
        }

        if (expressions.length == 1) {
            return expressions[0];
        }

        return startTiles -> {
            // have to collect to a list, because we have to construct one stream for each OR-ed expression
            List<Entity> startTileList = startTiles.toList();
            return Arrays.stream(expressions).flatMap(expression -> expression.apply(startTileList.stream()));
        };
    }
}
