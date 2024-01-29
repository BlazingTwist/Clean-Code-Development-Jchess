package jchess.common.moveset.special;

import dx.schema.types.PieceType;
import helper.TestHelper;
import jchess.common.components.TileComponent;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.gamemode.PieceStore;
import jchess.gamemode.square2p.Square2PlayerGame;
import jchess.gamemode.square2p.Square2pPieceLayouts;
import jchess.gamemode.square2p.Square2pPieces;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;

import static helper.TestHelper.getTileAtPosition;
import static helper.TestHelper.hasPiece;

@SuppressWarnings("FieldCanBeLocal")
public class CastlingTest {
    private Square2PlayerGame game;
    private Entity x0y0;
    private Entity x1y0;
    private Entity x2y0;
    private Entity x3y0;
    private Entity x4y0;
    private Entity x7y7;
    private Entity x6y7;
    private Entity x5y7;
    private Entity x4y7;

    @BeforeEach
    public void init() {
        game = new Square2PlayerGame(new PieceStore(Square2pPieces.values()), Square2pPieceLayouts.Standard);
        game.start();

        x0y0 = getTileAtPosition(game, 0, 0);
        x1y0 = getTileAtPosition(game, 1, 0);
        x2y0 = getTileAtPosition(game, 2, 0);
        x3y0 = getTileAtPosition(game, 3, 0);
        x4y0 = getTileAtPosition(game, 4, 0);

        x7y7 = getTileAtPosition(game, 7, 7);
        x6y7 = getTileAtPosition(game, 6, 7);
        x5y7 = getTileAtPosition(game, 5, 7);
        x4y7 = getTileAtPosition(game, 4, 7);

        Assertions.assertTrue(hasPiece(x4y0, PieceType.KING, 1));
        Assertions.assertTrue(hasPiece(x4y7, PieceType.KING, 0));
        Assertions.assertTrue(hasPiece(x0y0, PieceType.ROOK, 1));
        Assertions.assertTrue(hasPiece(x7y7, PieceType.ROOK, 0));
    }

    @Test
    public void test_castlingAllowed() {
        x1y0.piece = null;
        x2y0.piece = null;
        x3y0.piece = null;
        x6y7.piece = null;
        x5y7.piece = null;
        TileComponent.updateAttackInfo(game);

        MoveIntention blackCastleMove = TestHelper.findMoveToTile(game, x4y0, new Point(2, 0));
        MoveIntention whiteCastleMove = TestHelper.findMoveToTile(game, x4y7, new Point(6, 7));
        Assertions.assertNotNull(blackCastleMove);
        Assertions.assertNotNull(whiteCastleMove);

        blackCastleMove.onClick().run();
        whiteCastleMove.onClick().run();
        Assertions.assertTrue(hasPiece(x2y0, PieceType.KING, 1));
        Assertions.assertTrue(hasPiece(x6y7, PieceType.KING, 0));
        Assertions.assertTrue(hasPiece(x3y0, PieceType.ROOK, 1));
        Assertions.assertTrue(hasPiece(x5y7, PieceType.ROOK, 0));
    }

    @Test
    public void test_castlingBlocked() {
        // x1y0.piece = null;
        x2y0.piece = null;
        x3y0.piece = null;
        x6y7.piece = null;
        // x5y7.piece = null;
        TileComponent.updateAttackInfo(game);

        MoveIntention blackCastleMove = TestHelper.findMoveToTile(game, x4y0, new Point(2, 0));
        MoveIntention whiteCastleMove = TestHelper.findMoveToTile(game, x4y7, new Point(6, 7));
        Assertions.assertNull(blackCastleMove);
        Assertions.assertNull(whiteCastleMove);
    }

    @Test
    public void test_castlingPieceMoved() {
        x1y0.piece = null;
        x2y0.piece = null;
        x3y0.piece = null;
        x6y7.piece = null;
        x5y7.piece = null;
        TileComponent.updateAttackInfo(game);

        // move black king and back
        TestHelper.movePiece(game, x4y0, x3y0);
        TestHelper.movePiece(game, x3y0, x4y0);

        // move white rook and back
        TestHelper.movePiece(game, x7y7, x6y7);
        TestHelper.movePiece(game, x6y7, x7y7);

        MoveIntention blackCastleMove = TestHelper.findMoveToTile(game, x4y0, new Point(2, 0));
        MoveIntention whiteCastleMove = TestHelper.findMoveToTile(game, x4y7, new Point(6, 7));
        Assertions.assertNull(blackCastleMove);
        Assertions.assertNull(whiteCastleMove);
    }

    @Test
    public void test_castlingKingChecked() {
        x1y0.piece = null;
        x2y0.piece = null;
        x3y0.piece = null;
        x6y7.piece = null;
        x5y7.piece = null;
        game.createPiece(getTileAtPosition(game, 4, 6), PieceType.ROOK, 1); // create black rook checking white king
        game.createPiece(getTileAtPosition(game, 4, 1), PieceType.ROOK, 0); // create white rook checking black king
        TileComponent.updateAttackInfo(game);

        Assertions.assertTrue(x4y0.isAttacked()); // black king is attacked
        Assertions.assertTrue(x4y7.isAttacked()); // white king is attacked

        MoveIntention blackCastleMove = TestHelper.findMoveToTile(game, x4y0, new Point(2, 0));
        MoveIntention whiteCastleMove = TestHelper.findMoveToTile(game, x4y7, new Point(6, 7));
        Assertions.assertNull(blackCastleMove);
        Assertions.assertNull(whiteCastleMove);
    }

    @Test
    public void test_castlingKingPathChecked() {
        x1y0.piece = null;
        x2y0.piece = null;
        x3y0.piece = null;
        x6y7.piece = null;
        x5y7.piece = null;
        game.createPiece(getTileAtPosition(game, 5, 6), PieceType.ROOK, 1); // create black rook checking white kings path
        game.createPiece(getTileAtPosition(game, 2, 1), PieceType.ROOK, 0); // create white rook checking black kings destination
        TileComponent.updateAttackInfo(game);

        Assertions.assertTrue(x2y0.isAttacked(1)); // black king destination is attacked
        Assertions.assertTrue(x5y7.isAttacked(0)); // white king path is attacked

        MoveIntention blackCastleMove = TestHelper.findMoveToTile(game, x4y0, new Point(2, 0));
        MoveIntention whiteCastleMove = TestHelper.findMoveToTile(game, x4y7, new Point(6, 7));
        Assertions.assertNull(blackCastleMove);
        Assertions.assertNull(whiteCastleMove);
    }
}
