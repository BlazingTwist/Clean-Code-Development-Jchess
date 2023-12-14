package jchess.game.common.components;

import jchess.ecs.Entity;
import jchess.game.common.theme.IIconKey;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class TileComponent {
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
