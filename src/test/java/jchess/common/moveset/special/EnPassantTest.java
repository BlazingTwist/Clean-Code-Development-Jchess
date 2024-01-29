package jchess.common.moveset.special;

import dx.schema.types.PieceType;
import helper.TestHelper;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.gamemode.PieceStore;
import jchess.gamemode.square2p.Square2PlayerGame;
import jchess.gamemode.square2p.Square2pPieceLayouts;
import jchess.gamemode.square2p.Square2pPieces;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static helper.TestHelper.getTileAtPosition;
import static helper.TestHelper.hasPiece;

@SuppressWarnings("FieldCanBeLocal")
public class EnPassantTest {
    private Square2PlayerGame game;

    // enPassant pawn
    private Entity x1y4;

    // unrelated pawn / movement
    private Entity x0y1;
    private Entity x0y2;

    // white pawn movement
    private Entity x2y6;
    private Entity x2y5;
    private Entity x2y4;


    @BeforeEach
    public void init() {
        game = new Square2PlayerGame(new PieceStore(Square2pPieces.values()), Square2pPieceLayouts.Standard);
        game.start();

        x1y4 = getTileAtPosition(game, 1, 4);

        x0y1 = getTileAtPosition(game, 0, 1);
        x0y2 = getTileAtPosition(game, 0, 2);

        x2y6 = getTileAtPosition(game, 2, 6);
        x2y5 = getTileAtPosition(game, 2, 5);
        x2y4 = getTileAtPosition(game, 2, 4);

        game.createPiece(x1y4, PieceType.PAWN, 1);

        Assertions.assertTrue(hasPiece(x1y4, PieceType.PAWN, 1));
        Assertions.assertTrue(hasPiece(x0y1, PieceType.PAWN, 1));
        Assertions.assertTrue(hasPiece(x2y6, PieceType.PAWN, 0));
    }

    @Test
    public void test_enPassantAllowed() {
        MoveIntention whiteDoubleMove = TestHelper.findMoveToTile(game, x2y6, x2y4);
        Assertions.assertNotNull(whiteDoubleMove);
        whiteDoubleMove.onClick().run();

        MoveIntention enPassantMove = TestHelper.findMoveToTile(game, x1y4, x2y5);
        Assertions.assertNull(x2y5.piece);
        Assertions.assertNotNull(enPassantMove);

        enPassantMove.onClick().run();
        Assertions.assertNull(x2y4.piece);
        Assertions.assertTrue(hasPiece(x2y5, PieceType.PAWN, 1));
    }

    @Test
    public void test_enPassantTooLate() {
        MoveIntention whiteDoubleMove = TestHelper.findMoveToTile(game, x2y6, x2y4);
        Assertions.assertNotNull(whiteDoubleMove);
        whiteDoubleMove.onClick().run();

        // black misses the enPassant opportunity by moving an unrelated piece
        TestHelper.movePiece(game, x0y1, x0y2);

        MoveIntention enPassantMove = TestHelper.findMoveToTile(game, x1y4, x2y5);
        Assertions.assertNull(enPassantMove);
    }

    @Test
    public void test_enPassantAlreadyCaptured() {
        MoveIntention whiteDoubleMove = TestHelper.findMoveToTile(game, x2y6, x2y4);
        Assertions.assertNotNull(whiteDoubleMove);
        whiteDoubleMove.onClick().run();

        // pretend another player (relevant for 3-Player chess) has already captured the pawn
        x2y4.piece = null;

        MoveIntention enPassantMove = TestHelper.findMoveToTile(game, x1y4, x2y5);
        Assertions.assertNull(enPassantMove);
    }

    @Test
    public void test_enPassantBlocked() {
        MoveIntention whiteDoubleMove = TestHelper.findMoveToTile(game, x2y6, x2y4);
        Assertions.assertNotNull(whiteDoubleMove);
        whiteDoubleMove.onClick().run();

        // suppose the active player is occupying the tile that was skipped over (relevant for SpecialFirstMove that isn't the default pawn behaviour)
        game.createPiece(x2y5, PieceType.BISHOP, 1);

        // then enPassant is blocked by a friendly piece
        MoveIntention enPassantMove = TestHelper.findMoveToTile(game, x1y4, x2y5);
        Assertions.assertNull(enPassantMove);
    }

    @Test
    public void test_enPassantBlockedCapture() {
        MoveIntention whiteDoubleMove = TestHelper.findMoveToTile(game, x2y6, x2y4);
        Assertions.assertNotNull(whiteDoubleMove);
        whiteDoubleMove.onClick().run();

        // suppose another opponent moved onto the tile skipped by the white pawn
        game.createPiece(x2y5, PieceType.BISHOP, 0); // 0 in this case, because 2 player game

        // then the move should be a normal capture move
        MoveIntention enPassantMove = TestHelper.findMoveToTile(game, x1y4, x2y5);
        Assertions.assertTrue(hasPiece(x2y5, PieceType.BISHOP, 0));
        Assertions.assertNotNull(enPassantMove);

        enPassantMove.onClick().run();
        Assertions.assertTrue(hasPiece(x2y5, PieceType.PAWN, 1));
        Assertions.assertTrue(hasPiece(x2y4, PieceType.PAWN, 0)); // white pawn should not be captured in this edge-case
    }
}
