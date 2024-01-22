package jchess.el.v2;

import jchess.common.components.PieceIdentifier;

import java.util.Arrays;

class Regex implements ExpressionCompiler {
    private final ExpressionCompiler compiler;

    public Regex(String tileRegex, boolean aerial) {
        ExpressionCompiler[] orExpressions = Arrays.stream(tileRegex.split(" "))
                .filter(expr -> !expr.isBlank())
                .map(expr -> {
                    ExpressionCompiler[] sequence = Arrays.stream(expr.split("\\."))
                            .map(exp2 -> {
                                boolean repeatStar = exp2.endsWith("*");
                                boolean repeatPlus = exp2.endsWith("+");
                                int direction = Integer.parseInt((repeatStar || repeatPlus)
                                        ? exp2.substring(0, exp2.length() - 1)
                                        : exp2);

                                if (repeatStar) {
                                    return new Repeat(new Neighbor(direction), 0, -1, aerial);
                                } else if (repeatPlus) {
                                    return new Repeat(new Neighbor(direction), 1, -1, aerial);
                                } else {
                                    return new Neighbor(direction);
                                }
                            }).toArray(ExpressionCompiler[]::new);
                    return new Sequence(aerial, sequence);
                }).toArray(ExpressionCompiler[]::new);

        final ExpressionCompiler baseMoveCompiler = new Or(orExpressions);
        this.compiler = new Filter(baseMoveCompiler, TileExpression.FILTER_CAPTURE_OR_EMPTY);
    }

    @Override
    public TileExpression.CompiledExpression compile(PieceIdentifier movingPiece) {
        return this.compiler.compile(movingPiece);
    }
}
