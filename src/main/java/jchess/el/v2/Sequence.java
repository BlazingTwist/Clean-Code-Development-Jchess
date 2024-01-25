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
        if (sequenceCompilers.length == 0) {
            return startTiles -> Stream.empty();
        }

        Stream<ExpressionCompiler> compilers = Arrays.stream(sequenceCompilers);
        if (!aerial) {
            compilers = compilers.map(TileExpression::filterNoCollide);
        }
        final TileExpression.CompiledExpression[] expressions = compilers.map(compiler -> compiler.compile(movingPiece)).toArray(TileExpression.CompiledExpression[]::new);

        return TileExpression.mergeOpTree(expressions, 0, expressions.length - 1, (before, after) -> startTiles -> after.apply(before.apply(startTiles)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sequence other = (Sequence) o;
        if (aerial != other.aerial) return false;
        if (!Arrays.equals(sequenceCompilers, other.sequenceCompilers)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(sequenceCompilers);
        result = 31 * result + (aerial ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Sequence(aerial=" + aerial + "," + Arrays.toString(sequenceCompilers) + ")";
    }
}
