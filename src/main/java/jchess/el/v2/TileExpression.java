package jchess.el.v2;

import jchess.ecs.Entity;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TileExpression {

    public static final ExpressionCompiler FILTER_CAPTURE = movingPiece -> {
        final int ownerId = movingPiece.ownerId();
        return startTiles -> startTiles.filter(tile -> tile.piece != null && tile.piece.identifier.ownerId() != ownerId);
    };

    public static final ExpressionCompiler FILTER_CAPTURE_OR_EMPTY = movingPiece -> {
        final int ownerId = movingPiece.ownerId();
        return startTiles -> startTiles.filter(tile -> tile.piece == null || tile.piece.identifier.ownerId() != ownerId);
    };

    public static final Predicate<Entity> FILTER_EMPTY_TILE = tile -> tile.piece == null;

    /**
     * Converts the expression such that the move is discarded under either of these conditions
     * <ul>
     *     <li>the move starts on a tile occupied by an enemy</li>
     *     <li>the move ends on a tile occupied by an ally</li>
     * </ul>
     */
    public static ExpressionCompiler filterNoCollide(ExpressionCompiler expressionToFilter) {
        return movingPiece -> {
            final int moveOwner = movingPiece.ownerId();
            final CompiledExpression compiledMovement = expressionToFilter.compile(movingPiece);
            return startTiles -> {
                // if standing on an enemy tile, the previous move was a capture move -> stop repetition
                Stream<Entity> tilesMovableFrom = startTiles.filter(tile -> tile.piece == null || tile.piece.identifier.ownerId() == moveOwner);

                // exclude moves that land on a friendly tile
                return compiledMovement.apply(tilesMovableFrom).filter(tile -> tile.piece == null || tile.piece.identifier.ownerId() != moveOwner);
            };
        };
    }

    /**
     * <p>A tileRegex expression consists of the characters [0-9. +*]
     * <p>Any number is a valid tileRegex expression indicating the direction of travel from white's perspective.
     * <p>"a.b" means "a then b".
     * <p>"a b" means "a or b".
     * <p>"a*" means "repeat a zero or more times"
     * <p>"a+" means "repeat a one or more times"
     * <br/><br/>
     * <p>Some examples:
     * <ul>
     *     <li>"0.90*" = "north then (east zero or more times)</li>
     *     <li>"0+ 90+ 180+ 270+" = movement of the rook in "normal" chess</li>
     * </ul>
     * @param tileRegex the tileRegex expression
     * @param aerial    if true, allow repetition across occupied tiles
     * @return a compiled TileExpression
     */
    public static ExpressionCompiler regex(String tileRegex, boolean aerial) {
        return new Regex(tileRegex, aerial);
    }

    /**
     * Restricts the expression to return only those tiles that match the filter.
     * @param filteredExpression the expression to apply the filter to
     * @param filter             the filter to apply
     */
    public static ExpressionCompiler filter(ExpressionCompiler filteredExpression, Predicate<Entity> filter) {
        return new Filter(filteredExpression, filter);
    }

    /**
     * Restricts the expression to return only those tiles that match the filter.
     * @param filteredExpression the expression to apply the filter to
     * @param filterCompiler     the filter to apply
     */
    public static ExpressionCompiler filter2(ExpressionCompiler filteredExpression, ExpressionCompiler filterCompiler) {
        return new Filter(filteredExpression, filterCompiler);
    }

    public static ExpressionCompiler neighbor(int... directions) {
        return new Neighbor(directions);
    }

    public static ExpressionCompiler or(ExpressionCompiler... orExpressions) {
        return new Or(orExpressions);
    }

    /**
     * Extends the result to include the tiles obtained by recursively applying the expression [min, max] times.
     * @param repeatedExpression the expression to repeat
     * @param repeatMin          the minimum amount of times the expression should be applied
     * @param repeatMax          the maximum amount of times the expression should be applied, or -1 for unlimited applications
     * @param aerial             allows repetition across occupied tiles, otherwise: stop on capture-able tile, or stop before friendly tile
     */
    public static ExpressionCompiler repeat(ExpressionCompiler repeatedExpression, int repeatMin, int repeatMax, boolean aerial) {
        return new Repeat(repeatedExpression, repeatMin, repeatMax, aerial);
    }

    public static ExpressionCompiler sequence(boolean aerial, ExpressionCompiler... sequenceCompilers) {
        return new Sequence(aerial, sequenceCompilers);
    }

    /**
     * Apply a binary operator to a list of expression as a balanced tree to minimize callstack depth
     * @param expressions   expressions to merge
     * @param start         index of first element (inclusive)
     * @param end           index of last element (inclusive)
     * @param mergeOperator operator to use for merging expressions
     * @return the merged expression
     */
    public static CompiledExpression _mergeOpTree(CompiledExpression[] expressions, int start, int end, BinaryOperator<CompiledExpression> mergeOperator) {
        if (start >= end) {
            return expressions[start];
        }
        if ((start + 1) == end) {
            return mergeOperator.apply(expressions[start], expressions[start + 1]);
        }

        int mid = (end - start) / 2;
        return mergeOperator.apply(
                _mergeOpTree(expressions, start, mid, mergeOperator),
                _mergeOpTree(expressions, mid + 1, end, mergeOperator)
        );
    }

    /**
     * returns all elements obtained by recursively applying the mapper [0, numRecursive] many times to the baseStream.
     * @param baseStream the values to apply the mapper to
     * @param mapper     the mapper
     * @param numRecurse the maximum recursion depth
     * @return all elements obtained by recursively applying the 0 to numRecursive many times.
     */
    public static Stream<Entity> _recursiveMap(Stream<Entity> baseStream, CompiledExpression mapper, int numRecurse) {
        if (numRecurse <= 0) {
            return baseStream;
        }

        List<Entity> zeroApplicationList = baseStream.toList();
        if (zeroApplicationList.isEmpty()) {
            return Stream.empty();
        }

        Stream<Entity> oneApplicationStream = mapper.apply(zeroApplicationList.stream());
        return Stream.of(
                zeroApplicationList.stream(),
                _recursiveMap(oneApplicationStream, mapper, numRecurse - 1)
        ).flatMap(s -> s);
    }

    @FunctionalInterface
    public interface CompiledExpression {
        Stream<Entity> apply(Stream<Entity> startTiles);
    }

}
