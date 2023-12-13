package jchess.game.layout;

import jchess.game.common.IChessGame;
import jchess.game.layout.hex3p.Hex3PlayerGame;
import jchess.game.layout.square2p.Square2PlayerGame;

import java.util.function.Supplier;

// TODO erja, themeIds nicht hard-coded
public enum GameMode {
    Hex3P(Hex3PlayerGame::new, "3 Player Hexagonal Chess", "default"),
    Square2P(Square2PlayerGame::new, "2 Player Classic Chess", "default_square");

    private final Supplier<IChessGame> gameConstructor;
    private final String displayName;
    private final String[] allowedThemeIds;

    GameMode(Supplier<IChessGame> gameConstructor, String displayName, String... allowedThemeIds) {
        this.gameConstructor = gameConstructor;
        this.displayName = displayName;
        this.allowedThemeIds = allowedThemeIds;
    }

    public IChessGame newGame() {
        return gameConstructor.get();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getAllowedThemeIds() {
        return allowedThemeIds;
    }
}
