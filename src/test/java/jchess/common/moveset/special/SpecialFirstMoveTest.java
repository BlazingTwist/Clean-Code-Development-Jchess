package jchess.common.moveset.special;

import dx.schema.types.PieceType;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.gamemode.PieceStore;
import jchess.gamemode.square2p.Square2PlayerGame;
import jchess.gamemode.square2p.Square2pPieceLayouts;
import jchess.gamemode.square2p.Square2pPieces;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static helper.TestHelper.findMoveToTile;
import static helper.TestHelper.getTileAtPosition;
import static helper.TestHelper.hasNoPiece;
import static helper.TestHelper.hasPiece;

@SuppressWarnings("FieldCanBeLocal")
public class SpecialFirstMoveTest {
    private Square2PlayerGame game;

    private Entity x0y6;
    private Entity x0y5;
    private Entity x0y4;
    private Entity x0y3;

    @BeforeEach
    public void init() {
        game = new Square2PlayerGame(new PieceStore(Square2pPieces.values()), Square2pPieceLayouts.Standard);
        game.start();

        x0y6 = getTileAtPosition(game, 0, 6);
        x0y5 = getTileAtPosition(game, 0, 5);
        x0y4 = getTileAtPosition(game, 0, 4);
        x0y3 = getTileAtPosition(game, 0, 3);

        Assertions.assertTrue(hasPiece(x0y6, PieceType.PAWN, 0));
    }

    @Test
    public void test_specialMoveAllowed() {
        MoveIntention doubleMove = findMoveToTile(game, x0y6, x0y4);
        Assertions.assertNotNull(doubleMove);
        doubleMove.onClick().run();

        Assertions.assertTrue(hasPiece(x0y4, PieceType.PAWN, 0));
        Assertions.assertTrue(hasNoPiece(x0y5));
        Assertions.assertTrue(hasNoPiece(x0y6));
    }

    @Test
    public void test_specialMoveTooLate() {
        MoveIntention singleMove = findMoveToTile(game, x0y6, x0y5);
        Assertions.assertNotNull(singleMove);
        singleMove.onClick().run();

        MoveIntention doubleMove = findMoveToTile(game, x0y5, x0y3);
        MoveIntention singleMove2 = findMoveToTile(game, x0y5, x0y4);
        Assertions.assertNull(doubleMove);
        Assertions.assertNotNull(singleMove2);
    }

    @Test
    public void test_specialMoveBlockedNear() {
        game.createPiece(x0y5, PieceType.PAWN, 1);

        MoveIntention doubleMove = findMoveToTile(game, x0y6, x0y4);
        Assertions.assertNull(doubleMove);
    }

    @Test
    public void test_specialMoveBlockedFar() {
        game.createPiece(x0y4, PieceType.PAWN, 1);

        MoveIntention doubleMove = findMoveToTile(game, x0y6, x0y4);
        Assertions.assertNull(doubleMove);
    }
}
