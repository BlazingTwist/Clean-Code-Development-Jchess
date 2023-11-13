package jchess.game.layout.hex3p;

import jchess.game.common.IGameState;

public class Hex3pGameState implements IGameState {

    private int activePlayerId;

    public void nextPlayer() {
        this.activePlayerId = (activePlayerId + 1) % 3;
    }

    @Override
    public int activePlayerId() {
        return activePlayerId;
    }
}
