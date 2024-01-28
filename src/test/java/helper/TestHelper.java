package helper;

import dx.schema.types.PieceType;
import jchess.common.IChessGame;
import jchess.common.moveset.MoveIntention;
import jchess.common.moveset.NormalMove;
import jchess.ecs.Entity;

import java.awt.Point;

public class TestHelper {
    public static Entity getTileAtPosition(IChessGame game, int x, int y) {
        return game.getEntityManager().getEntities().stream()
                .filter(entity -> entity.tile != null
                        && entity.tile.position.x == x
                        && entity.tile.position.y == y)
                .findFirst().orElse(null);
    }

    public static void movePiece(IChessGame game, Entity from, Entity to) {
        NormalMove.getMove(game, from, to).onClick().run();
    }

    public static boolean hasPiece(Entity entity, PieceType pieceType, int owner) {
        return entity.piece != null
                && entity.piece.identifier.pieceType() == pieceType
                && entity.piece.identifier.ownerId() == owner;
    }

    public static MoveIntention findMoveToTile(IChessGame game, Entity piece, Point displayTile) {
        return piece.findValidMoves(game, false)
                .filter(move -> {
                    Entity displayEntity = move.displayTile();
                    if (displayEntity == null || displayEntity.tile == null) return false;

                    return displayEntity.tile.position.equals(displayTile);
                })
                .findFirst().orElse(null);
    }
}
