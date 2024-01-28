package jchess.common.moveset;

import jchess.common.IChessGame;
import jchess.ecs.Entity;

public record MoveIntention(Entity displayTile, Runnable onClick, IMoveSimulator moveSimulator) {

    /**
     * This is a utility Method for constructing a MoveIntention in the specific use-case where the moveSimulator matches the actual move.
     */
    public static MoveIntention fromMoveSimulator(IChessGame game, Entity displayTile, IMoveSimulator moveSimulator) {
        return new MoveIntention(
                displayTile,
                () -> {
                    moveSimulator.simulate();
                    game.endTurn();
                },
                moveSimulator
        );
    }

    public interface IMoveSimulator {
        void simulate();

        void revert();
    }
}
