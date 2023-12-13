package jchess.game.layout;

import jchess.game.common.IChessGame;
import jchess.game.layout.hex3p.Hex3PlayerGame;
import jchess.game.layout.square2p.Square2PlayerGame;

import java.util.function.Supplier;

// TODO erja, themeIds nicht hard-coded
public enum GameMode {
    Hex3P(Hex3PlayerGame::new, "3 Player Hexagonal Chess", 3, "default"),
    Square2P(Square2PlayerGame::new, "2 Player Classic Chess", 2, "default_square");

    private final Supplier<IChessGame> gameConstructor;
    private final String displayName;
    private final int numPlayers;
    private final String[] allowedThemeIds;

    GameMode(Supplier<IChessGame> gameConstructor, String displayName, int numPlayers, String... allowedThemeIds) {
        this.gameConstructor = gameConstructor;
        this.displayName = displayName;
        this.numPlayers = numPlayers;
        this.allowedThemeIds = allowedThemeIds;
    }

    public IChessGame newGame() {
        return gameConstructor.get();
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public String[] getAllowedThemeIds() {
        return allowedThemeIds;
    }
}
