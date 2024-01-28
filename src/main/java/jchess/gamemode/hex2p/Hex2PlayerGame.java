package jchess.gamemode.hex2p;

import dx.schema.types.Vector2I;
import jchess.common.BaseChessGame;
import jchess.common.components.TileComponent;
import jchess.ecs.Entity;
import jchess.gamemode.IPieceLayoutProvider;
import jchess.gamemode.PieceStore;

import java.awt.Point;
import java.util.Arrays;
import java.util.Objects;

public class Hex2PlayerGame extends BaseChessGame {

    private static final int numTilesHorizontal = 6 + 5;

    private static final int numTilesVertical = 21;

    private final Entity[][] tiles = new Entity[numTilesVertical][numTilesHorizontal];

    public Hex2PlayerGame(PieceStore pieceStore, IPieceLayoutProvider layoutProvider) {
        super(2, pieceStore, layoutProvider);
    }

    @Override
    public dx.schema.types.Entity applyPerspective(dx.schema.types.Entity tile, int playerIndex) {
        if (playerIndex < 0 || playerIndex > 1) {
            throw new IllegalArgumentException("playerIndex must be 0 or 1, but was " + playerIndex);
        }
        if (playerIndex == 0) return tile;
        if (tile == null || tile.getTile() == null || tile.getTile().getDisplayPos() == null) return tile;

        Vector2I displayPos = tile.getTile().getDisplayPos();
        displayPos.setX(numTilesHorizontal - displayPos.getX());
        displayPos.setY(numTilesVertical - displayPos.getY());
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
        return ownerId == 0 ? 0 : 180;
    }

    @Override
    protected void generateBoard() {
        for (int y = 0; y < numTilesVertical; y++) {
            Entity[] tileRow = tiles[y];
            int x_start, x_end;

            if (y < 5) { // top part
                x_start = 5 - y;
                x_end = x_start + 2 * (y + 1);
            } else if (y < 16) { // middle part
                x_start = (y % 2 == 0) ? 1 : 0;
                x_end = numTilesHorizontal;
            } else { // bottom part
                x_start = y - 15;
                x_end = x_start + (6 - x_start) * 2;
            }

            for (int x = x_start; x < x_end; x += 2) {
                tileRow[x] = entityManager.createEntity();
                tileRow[x].tile = new TileComponent(new Point(x, y), y % 3);
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
                    tile.neighborsByDirection.put(30, getEntityAtPosition(x + 1, y - 3));
                    tile.neighborsByDirection.put(60, getEntityAtPosition(x + 1, y - 1));
                    tile.neighborsByDirection.put(90, getEntityAtPosition(x + 2, y));
                    tile.neighborsByDirection.put(120, getEntityAtPosition(x + 1, y + 1));
                    tile.neighborsByDirection.put(150, getEntityAtPosition(x + 1, y + 3));
                    tile.neighborsByDirection.put(180, getEntityAtPosition(x, y + 2));
                    tile.neighborsByDirection.put(210, getEntityAtPosition(x - 1, y + 3));
                    tile.neighborsByDirection.put(240, getEntityAtPosition(x - 1, y + 1));
                    tile.neighborsByDirection.put(270, getEntityAtPosition(x - 2, y));
                    tile.neighborsByDirection.put(300, getEntityAtPosition(x - 1, y - 1));
                    tile.neighborsByDirection.put(330, getEntityAtPosition(x - 1, y - 3));
                });
    }

}
