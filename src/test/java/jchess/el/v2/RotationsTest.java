package jchess.el.v2;

import dx.schema.types.PieceType;
import jchess.ecs.Entity;
import jchess.gamemode.IPieceLayoutProvider;
import jchess.gamemode.PieceStore;
import jchess.gamemode.square2p.Square2PlayerGame;
import jchess.gamemode.square2p.Square2pPieces;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class RotationsTest {
    @Test
    void test_rotations() {
        ExpressionCompiler rotations = TileExpression.rotations(
                TileExpression.repeat(TileExpression.neighbor(0), 1, 2, true),
                4
        );
        ExpressionCompiler equivalentExpression = TileExpression.or(
                TileExpression.repeat(TileExpression.neighbor(0), 1, 2, true),
                TileExpression.repeat(TileExpression.neighbor(90), 1, 2, true),
                TileExpression.repeat(TileExpression.neighbor(180), 1, 2, true),
                TileExpression.repeat(TileExpression.neighbor(270), 1, 2, true)
        );

        Square2pBoardProvider square2pProvider = new Square2pBoardProvider(new PieceStore(Square2pPieces.values()), null);
        square2pProvider.generateBoard();
        Entity originTile = square2pProvider.getEntityAtPosition(3, 3);
        square2pProvider.createPiece(originTile, PieceType.PAWN, 0);

        assert originTile.piece != null;
        Set<Entity> actualTiles = rotations.toV1(originTile.piece.identifier).findTiles(originTile).collect(Collectors.toSet());
        Set<Entity> expectedTiles = equivalentExpression.toV1(originTile.piece.identifier).findTiles(originTile).collect(Collectors.toSet());

        Assertions.assertEquals(expectedTiles, actualTiles);
    }

    private static final class Square2pBoardProvider extends Square2PlayerGame {
        public Square2pBoardProvider(PieceStore pieceStore, IPieceLayoutProvider layoutProvider) {
            super(pieceStore, layoutProvider);
        }

        @Override
        protected Entity getEntityAtPosition(int x, int y) {
            return super.getEntityAtPosition(x, y);
        }

        @Override
        protected void generateBoard() {
            super.generateBoard();
        }
    }
}
