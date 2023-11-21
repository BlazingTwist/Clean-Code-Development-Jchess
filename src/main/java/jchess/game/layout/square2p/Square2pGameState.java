package jchess.game.layout.square2p;

import jchess.game.common.IGameState;

public class Square2pGameState implements IGameState {

    private int activePlayerId;

    public void nextPlayer() {
        this.activePlayerId = (activePlayerId + 1) % 2;
    }

    @Override
    public int activePlayerId() {
        return activePlayerId;
    }
}
