package jchess.gamemode;

import jchess.common.IChessGame;
import jchess.gamemode.hex3p.Hex3PlayerGame;
import jchess.gamemode.square2p.Square2PlayerGame;

// TODO erja, themeIds nicht hard-coded
public enum GameMode {
    Hex3P(Hex3PlayerGame::new, "3 Player Hexagonal Chess", 3, "default"),
    Square2P(Square2PlayerGame::new, "2 Player Classic Chess", 2, "default_square");

    private final IGameConstructor gameConstructor;
    private final String displayName;
    private final int numPlayers;
    private final String[] allowedThemeIds;

    GameMode(IGameConstructor gameConstructor, String displayName, int numPlayers, String... allowedThemeIds) {
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

    @FunctionalInterface
    public interface IGameConstructor {
        IChessGame get();
    }
}
