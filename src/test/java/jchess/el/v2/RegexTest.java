package jchess.el.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegexTest {

    @Test
    void test_regexEquals() {
        Assertions.assertEquals(
                new Regex("30", true),
                new Regex("30", true)
        );

        Assertions.assertNotEquals(
                new Regex("30", true),
                new Regex("30", false)
        );

        Assertions.assertNotEquals(
                new Regex("30", true),
                new Regex("31", true)
        );
    }

    @Test
    void test_regexSingleDirection() {
        final boolean aerial = false;
        Regex regex = new Regex("30", aerial);

        ExpressionCompiler expected = TileExpression.filter2(
                TileExpression.or(
                        TileExpression.sequence(
                                aerial,
                                TileExpression.neighbor(30)
                        )
                ),
                TileExpression.FILTER_CAPTURE_OR_EMPTY
        );

        Assertions.assertEquals(expected, regex.compiler);
    }

    @Test
    void test_regexOrAndSequence() {
        final boolean aerial = true;
        Regex regex = new Regex("30.0 0.30", aerial);

        ExpressionCompiler expected = TileExpression.filter2(
                TileExpression.or(
                        TileExpression.sequence(
                                aerial,
                                TileExpression.neighbor(30),
                                TileExpression.neighbor(0)
                        ),
                        TileExpression.sequence(
                                aerial,
                                TileExpression.neighbor(0),
                                TileExpression.neighbor(30)
                        )
                ),
                TileExpression.FILTER_CAPTURE_OR_EMPTY
        );

        Assertions.assertEquals(expected, regex.compiler);
    }

    @Test
    void test_regexRepetitions() {
        final boolean aerial = false;
        Regex regex = new Regex("0? 0+ 0* 0{2} 0{3,} 0{4,5}", aerial);

        ExpressionCompiler expected = TileExpression.filter2(
                TileExpression.or(
                        TileExpression.sequence( // 0?
                                aerial,
                                TileExpression.repeat(
                                        TileExpression.neighbor(0),
                                        0, 1, aerial
                                )
                        ),
                        TileExpression.sequence( // 0+
                                aerial,
                                TileExpression.repeat(
                                        TileExpression.neighbor(0),
                                        1, -1, aerial
                                )
                        ),
                        TileExpression.sequence( // 0*
                                aerial,
                                TileExpression.repeat(
                                        TileExpression.neighbor(0),
                                        0, -1, aerial
                                )
                        ),
                        TileExpression.sequence( // 0{2}
                                aerial,
                                TileExpression.repeat(
                                        TileExpression.neighbor(0),
                                        2, 2, aerial
                                )
                        ),
                        TileExpression.sequence( // 0{3,}
                                aerial,
                                TileExpression.repeat(
                                        TileExpression.neighbor(0),
                                        3, -1, aerial
                                )
                        ),
                        TileExpression.sequence( // 0{4,5}
                                aerial,
                                TileExpression.repeat(
                                        TileExpression.neighbor(0),
                                        4, 5, aerial
                                )
                        )
                ),
                TileExpression.FILTER_CAPTURE_OR_EMPTY
        );

        Assertions.assertEquals(expected, regex.compiler);
    }

    @Test
    void test_regexGrouping() {
        final boolean aerial = true;
        Regex regex = new Regex("0 (30) (0 30) (0.(30 60) (30 60){2}){3}", aerial);

        ExpressionCompiler expected = TileExpression.filter2(
                TileExpression.or(
                        TileExpression.sequence( // 0
                                aerial,
                                TileExpression.neighbor(0)
                        ),
                        TileExpression.sequence( // (30)
                                aerial,
                                TileExpression.or(
                                        TileExpression.sequence(
                                                aerial,
                                                TileExpression.neighbor(30)
                                        )
                                )
                        ),
                        TileExpression.sequence( // (0 30)
                                aerial,
                                TileExpression.or(
                                        TileExpression.sequence(
                                                aerial,
                                                TileExpression.neighbor(0)
                                        ),
                                        TileExpression.sequence(
                                                aerial,
                                                TileExpression.neighbor(30)
                                        )
                                )
                        ),
                        TileExpression.sequence( // (0.(30 60) (30 60){2}){3}
                                aerial,
                                TileExpression.repeat(
                                        TileExpression.or(
                                                TileExpression.sequence( // 0.(30 60)
                                                        aerial,
                                                        TileExpression.neighbor(0),
                                                        TileExpression.or(
                                                                TileExpression.sequence(
                                                                        aerial,
                                                                        TileExpression.neighbor(30)
                                                                ),
                                                                TileExpression.sequence(
                                                                        aerial,
                                                                        TileExpression.neighbor(60)
                                                                )
                                                        )
                                                ),
                                                TileExpression.sequence( // (30 60){2}
                                                        aerial,
                                                        TileExpression.repeat(
                                                                TileExpression.or(
                                                                        TileExpression.sequence(
                                                                                aerial,
                                                                                TileExpression.neighbor(30)
                                                                        ),
                                                                        TileExpression.sequence(
                                                                                aerial,
                                                                                TileExpression.neighbor(60)
                                                                        )
                                                                ),
                                                                2, 2, aerial
                                                        )
                                                )
                                        ),
                                        3, 3, aerial
                                )
                        )
                ),
                TileExpression.FILTER_CAPTURE_OR_EMPTY
        );

        Assertions.assertEquals(expected, regex.compiler);
    }
}
