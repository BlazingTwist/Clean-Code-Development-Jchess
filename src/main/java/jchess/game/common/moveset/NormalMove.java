package jchess.game.common.moveset;

import jchess.ecs.Entity;
import jchess.game.common.IChessGame;
import jchess.game.common.components.PieceComponent;

public class NormalMove {
    public static MoveIntention getMove(IChessGame game, Entity fromTile, Entity toTile) {
        return new MoveIntention(
                toTile,
                () -> game.movePiece(fromTile, toTile, NormalMove.class),
                new NormalMoveSimulator(toTile.piece, fromTile, toTile)
        );
    }

    private record NormalMoveSimulator(
            PieceComponent capturedPiece, Entity fromTile, Entity toTile
    ) implements MoveIntention.IMoveSimulator {

        @Override
        public void simulate() {
            toTile.piece = fromTile.piece;
            fromTile.piece = null;
        }

        @Override
        public void revert() {
            fromTile.piece = toTile.piece;
            toTile.piece = capturedPiece;
        }
    }
}
