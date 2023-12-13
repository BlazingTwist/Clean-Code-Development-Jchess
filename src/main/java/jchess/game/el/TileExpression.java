package jchess.game.el;

import jchess.ecs.Entity;
import jchess.game.common.components.PieceIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TileExpression {

    /**
     * A filter that restricts the result to capturing moves
     */
    public static final BiPredicate<PieceIdentifier, Entity> FILTER_CAPTURE =
            (movingPiece, tile) -> tile.piece != null && tile.piece.identifier.ownerId() != movingPiece.ownerId();

    /**
     * A filter that restricts the result to empty tiles (tiles without an occupying piece)
     */
    public static final Predicate<Entity> FILTER_EMPTY_TILE = tile -> tile.piece == null;

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
    public static TileExpression regex(String tileRegex, boolean aerial) {
        TileExpression[] orExpressions = Arrays.stream(tileRegex.split(" "))
                .filter(expr -> !expr.isBlank())
                .map(expr -> {
                    TileExpression[] sequence = Arrays.stream(expr.split("\\."))
                            .map(exp2 -> {
                                boolean repeatStar = exp2.endsWith("*");
                                boolean repeatPlus = exp2.endsWith("+");
                                int direction = Integer.parseInt((repeatStar || repeatPlus)
                                        ? exp2.substring(0, exp2.length() - 1)
                                        : exp2);

                                if (repeatStar) {
                                    return repeat(neighbor(direction), 0, -1, aerial);
                                } else if (repeatPlus) {
                                    return repeat(neighbor(direction), 1, -1, aerial);
                                } else {
                                    return neighbor(direction);
                                }
                            }).toArray(TileExpression[]::new);
                    return sequence(sequence);
                }).toArray(TileExpression[]::new);

        TileExpression baseMoves = or(orExpressions);
        return new TileExpression(movingPiece -> {
            final int moveOwner = movingPiece.ownerId();
            return stream -> baseMoves.operationBuilder.apply(movingPiece)
                    .apply(stream)
                    .filter(tile -> tile.piece == null || tile.piece.identifier.ownerId() != moveOwner);
        });
    }

    public static TileExpression neighbor(int direction) {
        return new TileExpression(movingPiece -> {
            int localDirection = (movingPiece.forwardBasis() + direction) % 360;
            return stream -> stream
                    .map(tile -> tile.tile.getTile(localDirection))
                    .filter(Objects::nonNull);
        });
    }

    public static TileExpression neighbor(int... direction) {
        return new TileExpression(movingPiece -> stream -> stream.flatMap(
                tile -> Arrays.stream(direction)
                        .mapToObj(dir -> {
                            int localDirection = (movingPiece.forwardBasis() + dir) % 360;
                            return tile.tile.getTile(localDirection);
                        })
                        .filter(Objects::nonNull)
        ));
    }

    /**
     * Restricts the expression to return only those tiles that match the filter.
     * @param expression the expression to apply the filter to
     * @param filter     the filter to apply
     * @return the resulting expression
     */
    public static TileExpression filter(TileExpression expression, Predicate<Entity> filter) {
        return new TileExpression(movingPiece -> expression
                .operationBuilder.apply(movingPiece)
                .andThen(stream -> stream.filter(filter)));
    }

    /**
     * Restricts the expression to return only those tiles that match the filter.
     * @param expression the expression to apply the filter to
     * @param filter     the filter to apply
     * @return the resulting expression
     */
    public static TileExpression filter(TileExpression expression, BiPredicate<PieceIdentifier, Entity> filter) {
        return new TileExpression(movingPiece -> expression
                .operationBuilder.apply(movingPiece)
                .andThen(stream -> stream.filter(entity -> filter.test(movingPiece, entity))));
    }

    /**
     * Extends the result to include the tiles obtained by recursively applying the expression [min, max] times.
     * @param expression the expression to repeat
     * @param min        the minimum amount of times the expression should be applied
     * @param max        the maximum amount of times the expression should be applied, or -1 for unlimited applications
     * @param aerial     allows repetition across occupied tiles, otherwise: stop on capture-able tile, or stop before friendly tile
     * @return the resulting expression
     */
    public static TileExpression repeat(TileExpression expression, int min, int max, boolean aerial) {
        if (min < 0) throw new IllegalArgumentException("argument 'min' may not be negative. Got '" + min + "'");

        return new TileExpression(movingPiece -> stream -> {
            final int moveOwner = movingPiece.ownerId();
            Function<Stream<Entity>, Stream<Entity>> expressionOp = expression.operationBuilder.apply(movingPiece);
            Function<Stream<Entity>, Stream<Entity>> collisionOp = aerial ? expressionOp : stream2 -> {
                // if standing on an enemy tile, the previous move was a capture move -> stop repetition
                Stream<Entity> tilesMovableFrom = stream2
                        .filter(tile -> tile.piece == null || tile.piece.identifier.ownerId() == moveOwner);

                // exclude moves that land on a friendly tile
                return expressionOp.apply(tilesMovableFrom)
                        .filter(tile -> tile.piece == null || tile.piece.identifier.ownerId() != moveOwner);
            };

            // apply the expression the minimum amount of times
            Stream<Entity> startTiles = stream;
            for (int i = 1; i <= min; i++) {
                startTiles = collisionOp.apply(startTiles);
            }

            int maxDepth = max >= 0 ? (max - min) : Integer.MAX_VALUE;
            return recursiveMap(startTiles, collisionOp, maxDepth);
        });
    }

    public static TileExpression sequence(TileExpression... sequence) {
        return new TileExpression(mergeOpTree(sequence, 0, sequence.length - 1, Function::andThen));
    }

    public static TileExpression or(TileExpression... expressions) {
        return new TileExpression(movingPiece -> stream -> {
            List<Entity> list = stream.toList();
            return Arrays.stream(expressions)
                    .map(expression -> expression.operationBuilder.apply(movingPiece))
                    .flatMap(op -> op.apply(list.stream()));
        });
    }

    private final Function<PieceIdentifier, Function<Stream<Entity>, Stream<Entity>>> operationBuilder;

    public TileExpression(Function<PieceIdentifier, Function<Stream<Entity>, Stream<Entity>>> operationBuilder) {
        this.operationBuilder = operationBuilder;
    }

    public CompiledTileExpression compile(PieceIdentifier movingPiece) {
        return new CompiledTileExpression(operationBuilder.apply(movingPiece));
    }

    /**
     * Apply a binary operator to a list of expression as a balanced tree to minimize callstack depth
     * @param expressions   expressions to merge
     * @param start         index of first element (inclusive)
     * @param end           index of last element (inclusive)
     * @param mergeOperator operator to use for merging expressions
     * @return the merged expression
     */
    private static Function<PieceIdentifier, Function<Stream<Entity>, Stream<Entity>>> mergeOpTree(
            TileExpression[] expressions, int start, int end,
            BinaryOperator<Function<Stream<Entity>, Stream<Entity>>> mergeOperator
    ) {
        if (start >= end) {
            return expressions[start].operationBuilder;
        }
        if ((start + 1) == end) {
            return movingPiece -> mergeOperator.apply(
                    expressions[start].operationBuilder.apply(movingPiece),
                    expressions[start + 1].operationBuilder.apply(movingPiece)
            );
        }

        int mid = (end - start) / 2;
        return movingPiece -> mergeOperator.apply(
                mergeOpTree(expressions, start, mid, mergeOperator).apply(movingPiece),
                mergeOpTree(expressions, mid + 1, end, mergeOperator).apply(movingPiece)
        );
    }

    /**
     * returns all elements obtained by recursively applying the mapper [0, numRecursive] many times to the baseStream.
     * @param baseStream the values to apply the mapper to
     * @param mapper     the mapper
     * @param numRecurse the maximum recursion depth
     * @param <T>        the type of the elements
     * @return all elements obtained by recursively applying the 0 to numRecursive many times.
     */
    private static <T> Stream<T> recursiveMap(Stream<T> baseStream, Function<Stream<T>, Stream<T>> mapper, int numRecurse) {
        if (numRecurse <= 0) {
            return baseStream;
        }

        List<T> zeroApplicationList = baseStream.toList();
        if (zeroApplicationList.isEmpty()) {
            return Stream.empty();
        }

        Stream<T> oneApplicationStream = mapper.apply(zeroApplicationList.stream());
        return Stream.of(
                zeroApplicationList.stream(),
                recursiveMap(oneApplicationStream, mapper, numRecurse - 1)
        ).flatMap(s -> s);
    }
}
