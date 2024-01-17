package jchess.el.v2;

import jchess.common.components.PieceIdentifier;

import java.util.Arrays;
import java.util.stream.Stream;

final class Sequence implements ExpressionCompiler {
    private final ExpressionCompiler[] sequenceCompilers;
    private final boolean aerial;

    public Sequence(boolean aerial, ExpressionCompiler... sequenceCompilers) {
        this.sequenceCompilers = sequenceCompilers;
        this.aerial = aerial;
    }

    @Override
    public TileExpression.CompiledExpression compile(PieceIdentifier movingPiece) {
        Stream<ExpressionCompiler> compilers = Arrays.stream(sequenceCompilers);
        if (!aerial) {
            compilers = compilers.map(TileExpression::filterNoCollide);
        }
        final TileExpression.CompiledExpression[] expressions = compilers.map(compiler -> compiler.compile(movingPiece)).toArray(TileExpression.CompiledExpression[]::new);

        return TileExpression._mergeOpTree(expressions, 0, expressions.length - 1, (before, after) -> startTiles -> after.apply(before.apply(startTiles)));
    }
}
