package jchess.common.components;

import jchess.common.IChessGame;
import jchess.ecs.Entity;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TileComponent {
    public static void updateAttackInfo(IChessGame game) {
        game.getEntityManager().getEntities().stream()
                .filter(entity -> entity.tile != null)
                .forEach(entity -> entity.tile.attackingPieces.clear());

        game.getEntityManager().getEntities().parallelStream()
                .filter(entity -> entity.tile != null && entity.piece != null)
                .forEach(entity -> entity
                        .findValidMoves(game, false)
                        .forEach(move -> {
                            assert move.displayTile().tile != null;
                            move.displayTile().tile.attackingPieces.add(entity);
                        })
                );
    }

    public final Set<Entity> attackingPieces = ConcurrentHashMap.newKeySet();
    public final Point position;
    public final int colorIndex;

    public TileComponent(Point position, int colorIndex) {
        this.position = position;
        this.colorIndex = colorIndex;
    }

    /**
     * key = number in range [0, 360), indicating the direction of travel towards the neighbor
     */
    public final Map<Integer, Entity> neighborsByDirection = new HashMap<>();

    public Entity getTile(int direction) {
        return neighborsByDirection.get(direction);
    }

    public static String getTileId(TileComponent tile) {
        return tile.position.x + ";" + tile.position.y;
    }

    public static Point getTilePosition(String tileId) {
        String[] posSplit = tileId.split(";");
        if (posSplit.length != 2) {
            throw new IllegalArgumentException("Invalid tileId: '" + tileId + "'");
        }

        return new Point(Integer.parseInt(posSplit[0]), Integer.parseInt(posSplit[1]));
    }
}
