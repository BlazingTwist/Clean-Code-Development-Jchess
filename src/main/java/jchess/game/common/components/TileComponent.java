package jchess.game.common.components;

import jchess.ecs.Entity;
import jchess.game.common.IChessGame;
import jchess.game.common.theme.IIconKey;

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
                .forEach(entity -> {
                    entity.findValidMoves(false).forEach(move -> {
                        move.displayTile().tile.attackingPieces.add(entity);
                    });
                });
    }

    public final Set<Entity> attackingPieces = ConcurrentHashMap.newKeySet();
    public Point position;
    public IIconKey iconKey;

    /**
     * key = number in range [0, 360), indicating the direction of travel towards the neighbor
     */
    public final Map<Integer, Entity> neighborsByDirection = new HashMap<>();

    public Entity getTile(int direction) {
        return neighborsByDirection.get(direction);
    }
}
