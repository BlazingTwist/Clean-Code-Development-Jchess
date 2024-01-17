package jchess.el.v2;

import jchess.common.components.PieceIdentifier;
import jchess.ecs.Entity;

import java.util.function.Predicate;

final class Filter implements ExpressionCompiler {
    private final ExpressionCompiler filteredExpression;
    private final ExpressionCompiler filterCompiler;

    /**
     * Restricts the expression to return only those tiles that match the filter.
     * @param filteredExpression the expression to apply the filter to
     * @param filter             the filter to apply
     */
    public Filter(ExpressionCompiler filteredExpression, Predicate<Entity> filter) {
        this.filteredExpression = filteredExpression;
        this.filterCompiler = movingPiece -> startTiles -> startTiles.filter(filter);
    }

    /**
     * Restricts the expression to return only those tiles that match the filter.
     * @param filteredExpression the expression to apply the filter to
     * @param filterCompiler     the filter to apply
     */
    public Filter(ExpressionCompiler filteredExpression, ExpressionCompiler filterCompiler) {
        this.filteredExpression = filteredExpression;
        this.filterCompiler = filterCompiler;
    }

    @Override
    public TileExpression.CompiledExpression compile(PieceIdentifier movingPiece) {
        TileExpression.CompiledExpression compiledExpression = filteredExpression.compile(movingPiece);
        TileExpression.CompiledExpression filterExpression = filterCompiler.compile(movingPiece);
        return startTiles -> filterExpression.apply(compiledExpression.apply(startTiles));
    }
}
