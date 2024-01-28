package jchess;

import dx.schema.types.PieceType;
import jchess.common.IChessGame;
import jchess.common.events.BoardClickedEvent;
import jchess.common.moveset.NormalMove;
import jchess.ecs.Entity;
import jchess.gamemode.PieceStore;
import jchess.gamemode.hex3p.Hex3PlayerGame;
import jchess.gamemode.hex3p.Hex3pPieceLayouts;
import jchess.gamemode.hex3p.Hex3pPieces;
import jchess.gamemode.square2p.Square2PlayerGame;
import jchess.gamemode.square2p.Square2pPieceLayouts;
import jchess.gamemode.square2p.Square2pPieces;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegressionTests {
    private static Entity getTileAtPosition(IChessGame game, int x, int y) {
        return game.getEntityManager().getEntities().stream()
                .filter(entity -> entity.tile != null
                        && entity.tile.position.x == x
                        && entity.tile.position.y == y)
                .findFirst().orElse(null);
    }

    private static void movePiece(IChessGame game, Entity from, Entity to) {
        NormalMove.getMove(game, from, to).onClick().run();
    }

    /**
     * Simulates an error-condition caused by incorrect MoveSimulator#revert logic, as described in Issue #27
     */
    @Test
    public void test_issue27_kingCheckException() {
        Hex3PlayerGame game = new Hex3PlayerGame(new PieceStore(Hex3pPieces.values()), Hex3pPieceLayouts.Standard);
        game.start();

        Entity whiteRook = getTileAtPosition(game, 24, 16);
        Assertions.assertTrue(whiteRook.piece != null
                        && whiteRook.piece.identifier.pieceType() == PieceType.ROOK
                        && whiteRook.piece.identifier.ownerId() == 0,
                "Expected to find white rook at position 24,16");

        // basic setup:
        //   white rook checks black king.
        //   white rook can move to un-check king.
        Entity distance1 = getTileAtPosition(game, 26, 4);
        Entity distance2 = getTileAtPosition(game, 25, 5);
        distance1.piece = null;
        NormalMove.getMove(game, whiteRook, distance2).onClick().run();

        Entity blackKingTile = getTileAtPosition(game, 27, 3);

        Assertions.assertNotNull(blackKingTile.tile);
        Assertions.assertNotNull(distance2.tile);

        Assertions.assertTrue(blackKingTile.isAttacked(), "Black King should be attacked by White Rook");
        Assertions.assertTrue(blackKingTile.tile.attackingPieces.contains(distance2), "Black King should be attacked by White Rook");

        Assertions.assertDoesNotThrow(() -> game.getEventManager().getEvent(BoardClickedEvent.class).fire(distance2.tile.position));
    }

    @Test
    public void test_issue33_castleMoveNoCheck() {
        Square2PlayerGame game = new Square2PlayerGame(new PieceStore(Square2pPieces.values()), Square2pPieceLayouts.Standard);
        game.start();

        Entity x1 = getTileAtPosition(game, 1, 0);
        Entity x2 = getTileAtPosition(game, 2, 0);
        Entity x3 = getTileAtPosition(game, 3, 0);
        Entity x4 = getTileAtPosition(game, 4, 1);

        Entity blackKingTile = getTileAtPosition(game, 4, 0);

        Entity bishop0 = getTileAtPosition(game, 2, 7);
        Entity bishop1 = getTileAtPosition(game, 6, 3);

        x1.piece = null;
        x2.piece = null;
        x3.piece = null;
        x4.piece = null;

        movePiece(game, bishop0, bishop1);

        Assertions.assertTrue(blackKingTile.piece != null && blackKingTile.tile != null);
        Assertions.assertNotNull(x3.tile);

        Assertions.assertSame(blackKingTile.piece.identifier.pieceType(), PieceType.KING);
        Assertions.assertSame(blackKingTile.piece.identifier.ownerId(), 1);

        Assertions.assertTrue(x3.isAttacked(1), "white bishop should be attacking this tile");
        Assertions.assertFalse(blackKingTile.findValidMoves(game, true).anyMatch(move -> move.displayTile() == x2),
                "black king should not be able to castle");

        movePiece(game, bishop1, bishop0);
        Assertions.assertFalse(x3.isAttacked(1), "white bishop should not be attacking this tile anymore");
        Assertions.assertTrue(blackKingTile.findValidMoves(game, true).anyMatch(move -> move.displayTile() == x2),
                "black king should be able to castle");
    }
}
