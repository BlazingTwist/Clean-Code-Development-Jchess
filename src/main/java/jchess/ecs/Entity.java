package jchess.ecs;

import jchess.common.components.MarkerComponent;
import jchess.common.components.PieceComponent;
import jchess.common.components.TileComponent;
import jchess.common.moveset.MoveIntention;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class Entity {
    @Nullable
    public TileComponent tile;

    @Nullable
    public PieceComponent piece;

    @Nullable
    public MarkerComponent marker;

    public Stream<MoveIntention> findValidMoves(boolean verifyKingSafe) {
        return piece == null ? Stream.empty() : piece.findValidMoves(this, verifyKingSafe);
    }

    public boolean isAttacked() {
        if (tile == null || piece == null) return false;

        final int ownerId = piece.identifier.ownerId();
        return tile.attackingPieces.stream().anyMatch(attacker -> {
            assert attacker.piece != null; // attacker must be a piece.
            return attacker.piece.identifier.ownerId() != ownerId;
        });
    }
}
