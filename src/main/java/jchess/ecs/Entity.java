package jchess.ecs;

import jchess.game.common.marker.MarkerComponent;
import jchess.game.common.piece.PieceComponent;
import jchess.game.common.tile.TileComponent;

import java.util.stream.Stream;

public class Entity {
    public TileComponent tile;
    public PieceComponent piece;
    public MarkerComponent marker;

    public Stream<Entity> findValidMoves() {
        if (tile == null || piece == null) return Stream.empty();

        return piece.moveSet.findValidMoves(this);
    }
}
