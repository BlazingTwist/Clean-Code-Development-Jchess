package jchess.common.moveset;

import jchess.common.IChessGame;
import jchess.common.components.PieceComponent;
import jchess.ecs.Entity;

public class NormalMove {
    public static MoveIntention getMove(IChessGame game, Entity fromTile, Entity toTile) {
        NormalMoveSimulator moveSimulator = new NormalMoveSimulator(game, fromTile, toTile, NormalMove.class);
        return MoveIntention.fromMoveSimulator(game, toTile, moveSimulator);
    }

    public static class NormalMoveSimulator implements MoveIntention.IMoveSimulator {
        private final IChessGame game;
        private final Entity fromTile;
        private final Entity toTile;
        private final Class<?> moveType;
        private final PieceComponent capturedPiece;

        public NormalMoveSimulator(IChessGame game, Entity fromTile, Entity toTile, Class<?> moveType) {
            this.game = game;
            this.fromTile = fromTile;
            this.toTile = toTile;
            this.moveType = moveType;
            this.capturedPiece = toTile.piece;
        }

        @Override
        public void simulate() {
            toTile.piece = fromTile.piece;
            fromTile.piece = null;
            game.notifyPieceMove(fromTile, toTile, moveType);
        }

        @Override
        public void revert() {
            fromTile.piece = toTile.piece;
            toTile.piece = capturedPiece;
        }
    }
}
