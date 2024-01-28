package jchess.gamemode.hex3p;

import dx.schema.types.Vector2I;
import jchess.common.BaseChessGame;
import jchess.common.components.TileComponent;
import jchess.ecs.Entity;
import jchess.gamemode.IPieceLayoutProvider;
import jchess.gamemode.PieceStore;

import java.awt.Point;
import java.util.Arrays;
import java.util.Objects;

public class Hex3PlayerGame extends BaseChessGame {
    private static final int numTilesHorizontal = 17 + 16;
    private static final int numTilesVertical = 17;

    private final Entity[][] tiles = new Entity[numTilesVertical][numTilesHorizontal];

    public Hex3PlayerGame(PieceStore pieceStore, IPieceLayoutProvider layoutProvider) {
        super(3, pieceStore, layoutProvider);
    }

    @Override
    public dx.schema.types.Entity applyPerspective(dx.schema.types.Entity tile, int playerIndex) {
        if (playerIndex < 0 || playerIndex > 2) {
            throw new IllegalArgumentException("playerIndex must be 0, 1 or 2, but was " + playerIndex);
        }
        if (playerIndex == 0) return tile;
        if (tile == null || tile.getTile() == null || tile.getTile().getDisplayPos() == null) return tile;

        final double strideX = 1.73205;
        final double strideY = 3;
        final double cosine = -0.5;
        final double sine = (playerIndex == 1 ? 1 : -1) * 0.8660254;

        Vector2I displayPos = tile.getTile().getDisplayPos();
        double scaledX = (displayPos.getX() - 16) * strideX;
        double scaledY = (displayPos.getY() - 8) * strideY;
        double rotatedX = (scaledX * cosine) - (scaledY * sine);
        double rotatedY = (scaledX * sine) + (scaledY * cosine);
        displayPos.setX((int) Math.round(rotatedX / strideX) + 16);
        displayPos.setY((int) Math.round(rotatedY / strideY) + 8);
        return tile;
    }

    @Override
    protected Entity getEntityAtPosition(int x, int y) {
        if (x < 0 || x >= numTilesHorizontal) return null;
        if (y < 0 || y >= numTilesVertical) return null;

        return tiles[y][x];
    }

    @Override
    protected int getDirectionFromOwnerId(int ownerId) {
        return ((ownerId - 3) * (-120)) % 360; // [0, 240, 120]
    }

    @Override
    protected void generateBoard() {
        // first pass: create entities
        for (int y = 0; y < numTilesVertical; y++) {
            Entity[] tileRow = tiles[y];
            int x0 = Math.abs(8 - y);
            int x1 = 32 - x0;
            for (int x = x0; x <= x1; x += 2) {
                tileRow[x] = entityManager.createEntity();
                tileRow[x].tile = new TileComponent(new Point(x, y), x % 3);
            }
        }

        // second pass: generate neighbors
        Arrays.stream(tiles).flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .forEach(entity -> {
                    TileComponent tile = entity.tile;
                    assert tile != null;
                    int x = tile.position.x;
                    int y = tile.position.y;
                    tile.neighborsByDirection.put(0, getEntityAtPosition(x, y - 2));
                    tile.neighborsByDirection.put(30, getEntityAtPosition(x + 1, y - 1));
                    tile.neighborsByDirection.put(60, getEntityAtPosition(x + 3, y - 1));
                    tile.neighborsByDirection.put(90, getEntityAtPosition(x + 2, y));
                    tile.neighborsByDirection.put(120, getEntityAtPosition(x + 3, y + 1));
                    tile.neighborsByDirection.put(150, getEntityAtPosition(x + 1, y + 1));
                    tile.neighborsByDirection.put(180, getEntityAtPosition(x, y + 2));
                    tile.neighborsByDirection.put(210, getEntityAtPosition(x - 1, y + 1));
                    tile.neighborsByDirection.put(240, getEntityAtPosition(x - 3, y + 1));
                    tile.neighborsByDirection.put(270, getEntityAtPosition(x - 2, y));
                    tile.neighborsByDirection.put(300, getEntityAtPosition(x - 3, y - 1));
                    tile.neighborsByDirection.put(330, getEntityAtPosition(x - 1, y - 1));
                });
    }
}
