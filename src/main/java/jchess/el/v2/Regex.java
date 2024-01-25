package jchess.el.v2;

import jchess.common.components.PieceIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

class Regex implements ExpressionCompiler {
    public final ExpressionCompiler compiler;

    public Regex(String tileRegex, boolean aerial) {
        this.compiler = new Filter(parseRegex(tileRegex, aerial), TileExpression.FILTER_CAPTURE_OR_EMPTY);
    }

    @Override
    public TileExpression.CompiledExpression compile(PieceIdentifier movingPiece) {
        return this.compiler.compile(movingPiece);
    }

    private static ExpressionCompiler parseRegex(String regex, boolean aerial) {
        return new Or(
                splitExpression(regex, ' ')
                        .map(orExpr -> parseSequence(orExpr, aerial))
                        .toArray(ExpressionCompiler[]::new)
        );
    }

    private static ExpressionCompiler parseSequence(String expression, boolean aerial) {
        return new Sequence(
                aerial,
                splitExpression(expression, '.')
                        .map(andExpr -> {
                            RepetitionParseResult repetition = parseRepetition(andExpr);
                            ExpressionCompiler groupExpression = parseGroup(repetition.repeatedExpression, aerial);
                            if (repetition.minRepetitions == 1 && repetition.maxRepetitions == 1) {
                                return groupExpression;
                            } else {
                                return new Repeat(groupExpression, repetition.minRepetitions, repetition.maxRepetitions, aerial);
                            }
                        }).toArray(ExpressionCompiler[]::new)
        );
    }

    private static RepetitionParseResult parseRepetition(String expression) {
        if (expression.endsWith("*")) {
            return new RepetitionParseResult(substring(expression, 0, -1), 0, -1);
        }
        if (expression.endsWith("+")) {
            return new RepetitionParseResult(substring(expression, 0, -1), 1, -1);
        }
        if (expression.endsWith("?")) {
            return new RepetitionParseResult(substring(expression, 0, -1), 0, 1);
        }
        if (expression.endsWith("}")) {
            int splitIndex = expression.lastIndexOf('{');
            String repeatedExp = substring(expression, 0, splitIndex);
            String repetitionExp = substring(expression, splitIndex + 1, -1);

            if (repetitionExp.contains(",")) {
                String[] rangeSplit = repetitionExp.split(",");
                if (rangeSplit.length >= 2) {
                    return new RepetitionParseResult(repeatedExp, Integer.parseInt(rangeSplit[0]), Integer.parseInt(rangeSplit[1]));
                } else {
                    return new RepetitionParseResult(repeatedExp, Integer.parseInt(rangeSplit[0]), -1);
                }
            } else {
                int repetitions = Integer.parseInt(repetitionExp);
                return new RepetitionParseResult(repeatedExp, repetitions, repetitions);
            }
        }
        return new RepetitionParseResult(expression, 1, 1);
    }

    private static ExpressionCompiler parseGroup(String expression, boolean aerial) {
        if (expression.startsWith("(")) {
            return parseRegex(substring(expression, 1, -1), aerial);
        }
        return new Neighbor(Integer.parseInt(expression));
    }

    /**
     * Unlike the regular String#substring Method, this supports negative indices.
     * <p> E.g. `-1` means "first char from end of string"
     */
    @SuppressWarnings("SameParameterValue")
    private static String substring(String text, int start, int end) {
        if (start < 0) start = text.length() + start;
        if (end < 0) end = text.length() + end;
        return text.substring(start, end);
    }

    private static Stream<String> splitExpression(String expression, char splitChar) {
        List<String> split = new ArrayList<>();
        StringBuilder expressionBuilder = new StringBuilder();
        int groupDepth = 0;
        for (char c : expression.toCharArray()) {
            if (groupDepth == 0 && c == splitChar) {
                split.add(expressionBuilder.toString());
                expressionBuilder.setLength(0);
                continue;
            }

            if (c == '(') {
                groupDepth++;
            } else if (c == ')') {
                groupDepth--;
            }
            expressionBuilder.append(c);
        }
        split.add(expressionBuilder.toString());

        if (groupDepth > 0) {
            throw new IllegalArgumentException("expression '" + expression + "' is missing " + groupDepth + " ')' to close its groups");
        }
        if (groupDepth < 0) {
            throw new IllegalArgumentException("expression '" + expression + "' has " + (-groupDepth) + " too many ')' compared to '('");
        }

        return split.stream().filter(str -> !str.isBlank()).map(String::trim);
    }

    private record RepetitionParseResult(String repeatedExpression, int minRepetitions, int maxRepetitions) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Regex other = (Regex) o;
        return Objects.equals(compiler, other.compiler);
    }

    @Override
    public int hashCode() {
        return compiler != null ? compiler.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Regex(" + compiler + ')';
    }
}
