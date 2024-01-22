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
                                RepetitionParseResult repetition = parseRepetition(exp2);
                                int neighbor = Integer.parseInt(repetition.repeatedExpression);
                                if (repetition.minRepetitions == 1 && repetition.maxRepetitions == 1) {
                                    return new Neighbor(neighbor);
                                } else {
                                    return new Repeat(new Neighbor(neighbor), repetition.minRepetitions, repetition.maxRepetitions, aerial);
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

    private static RepetitionParseResult parseRepetition(String expression) {
        if (expression.endsWith("*")) {
            return new RepetitionParseResult(expression.substring(0, expression.length() - 1), 0, -1);
        }
        if (expression.endsWith("+")) {
            return new RepetitionParseResult(expression.substring(0, expression.length() - 1), 1, -1);
        }
        if (expression.endsWith("}")) {
            String[] expSplit = expression.substring(0, expression.length() - 1).split("\\{");
            String direction = expSplit[0];
            String[] rangeSplit = expSplit[1].split(",");
            if (rangeSplit.length >= 2) {
                return new RepetitionParseResult(direction, Integer.parseInt(rangeSplit[0]), Integer.parseInt(rangeSplit[1]));
            } else {
                return new RepetitionParseResult(direction, Integer.parseInt(rangeSplit[0]), -1);
            }
        }
        return new RepetitionParseResult(expression, 1, 1);
    }

    private record RepetitionParseResult(String repeatedExpression, int minRepetitions, int maxRepetitions) {
    }
}
