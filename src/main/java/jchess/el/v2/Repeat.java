package jchess.el.v2;

import jchess.common.components.PieceIdentifier;

import java.util.Objects;

final class Repeat implements ExpressionCompiler {
    private final ExpressionCompiler repeatedExpression;
    private final int repeatMin;
    private final int repeatMax;
    private final boolean aerial;

    /**
     * Extends the result to include the tiles obtained by recursively applying the expression [min, max] times.
     * @param repeatedExpression the expression to repeat
     * @param repeatMin          the minimum amount of times the expression should be applied
     * @param repeatMax          the maximum amount of times the expression should be applied, or -1 for unlimited applications
     * @param aerial             allows repetition across occupied tiles, otherwise: stop on capture-able tile, or stop before friendly tile
     */
    public Repeat(ExpressionCompiler repeatedExpression, int repeatMin, int repeatMax, boolean aerial) {
        if (repeatMin < 0) throw new IllegalArgumentException("argument 'repeatMin' may not be negative. Got '" + repeatMin + "'");

        this.repeatedExpression = repeatedExpression;
        this.repeatMin = repeatMin;
        this.repeatMax = repeatMax;
        this.aerial = aerial;
    }

    @Override
    public TileExpression.CompiledExpression compile(PieceIdentifier movingPiece) {
        final TileExpression.CompiledExpression moveExpression = (aerial ? repeatedExpression : TileExpression.filterNoCollide(repeatedExpression)).compile(movingPiece);
        return startTiles -> {
            for (int i = 1; i <= repeatMin; i++) {
                startTiles = moveExpression.apply(startTiles);
            }

            int maxDepth = repeatMax >= 0 ? (repeatMax - repeatMin) : Integer.MAX_VALUE;
            return TileExpression.recursiveMap(startTiles, moveExpression, maxDepth);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Repeat other = (Repeat) o;
        if (repeatMin != other.repeatMin) return false;
        if (repeatMax != other.repeatMax) return false;
        if (aerial != other.aerial) return false;
        if (!Objects.equals(repeatedExpression, other.repeatedExpression)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = repeatedExpression != null ? repeatedExpression.hashCode() : 0;
        result = 31 * result + repeatMin;
        result = 31 * result + repeatMax;
        result = 31 * result + (aerial ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Repeat(min=" + repeatMin + ",max=" + repeatMax + ",aerial=" + aerial + "," + repeatedExpression + ")";
    }
}
