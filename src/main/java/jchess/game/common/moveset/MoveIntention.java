package jchess.game.common.moveset;

import jchess.ecs.Entity;

public record MoveIntention(Entity displayTile, Runnable onClick, IMoveSimulator moveSimulator) {

    public interface IMoveSimulator {
        void simulate();
        void revert();
    }
}
