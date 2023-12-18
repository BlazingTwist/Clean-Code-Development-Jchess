package jchess.ecs;

import jchess.game.common.components.MarkerComponent;
import jchess.game.common.components.PieceComponent;
import jchess.game.common.components.TileComponent;
import jchess.game.common.moveset.MoveIntention;

import java.util.stream.Stream;

public class Entity {
    public TileComponent tile;
    public PieceComponent piece;
    public MarkerComponent marker;

    public Stream<MoveIntention> findValidMoves(boolean verifyKingSafe) {
        return piece == null ? Stream.empty() : piece.findValidMoves(this, verifyKingSafe);
    }

    public boolean isAttacked() {
        if (tile == null || piece == null) return false;

        final int ownerId = piece.identifier.ownerId();
        return tile.attackingPieces.stream().anyMatch(attacker -> attacker.piece.identifier.ownerId() != ownerId);
    }
}
