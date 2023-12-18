package jchess.game.common.moveset;

import jchess.ecs.Entity;
import jchess.game.common.IChessGame;

public class NormalMove {
    public static MoveIntention getMove(IChessGame game, Entity fromTile, Entity toTile) {
        return new MoveIntention(toTile, () -> game.movePiece(fromTile, toTile, NormalMove.class));
    }
}
