package jchess.el.v2;

import jchess.common.components.PieceIdentifier;

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
            return TileExpression._recursiveMap(startTiles, moveExpression, maxDepth);
        };
    }
}
