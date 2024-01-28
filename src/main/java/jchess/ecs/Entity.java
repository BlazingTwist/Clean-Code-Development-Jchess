package jchess.ecs;

import jchess.common.IChessGame;
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

    public Stream<MoveIntention> findValidMoves(IChessGame game, boolean verifyKingSafe) {
        return piece == null ? Stream.empty() : piece.findValidMoves(game, this, verifyKingSafe);
    }

    public boolean isAttacked() {
        if (tile == null || piece == null) return false;
        return isAttacked(piece.identifier.ownerId());
    }

    public boolean isAttacked(int attackedPlayer) {
        return tile != null && tile.attackingPieces.stream().anyMatch(attacker -> {
            assert attacker.piece != null; // attacker must be a piece.
            return attacker.piece.identifier.ownerId() != attackedPlayer;
        });
    }
}
