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
}
