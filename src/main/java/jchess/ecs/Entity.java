package jchess.ecs;

import jchess.game.common.components.MarkerComponent;
import jchess.game.common.components.PieceComponent;
import jchess.game.common.components.TileComponent;

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
