package jchess.gamemode;

import dx.schema.conf.LayoutTheme;
import jchess.common.IChessGame;
import jchess.gamemode.hex3p.Hex3PlayerGame;
import jchess.gamemode.hex3p.Hex3pPieces;
import jchess.gamemode.square2p.Square2PlayerGame;
import jchess.gamemode.square2p.Square2pPieces;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GameModeStore {

    private static final Map<LayoutTheme.LayoutId, GameModeProvider> gameModeProviders = new HashMap<>();

    static {
        gameModeProviders.put(LayoutTheme.LayoutId.HEX_3_P, new GameModeProvider(
                Hex3PlayerGame::new, "3 Player Hexagonal Chess", 3, new PieceStore(Hex3pPieces.values())
        ));
        gameModeProviders.put(LayoutTheme.LayoutId.SQUARE_2_P, new GameModeProvider(
                Square2PlayerGame::new, "2 Player Classic Chess", 2, new PieceStore(Square2pPieces.values())
        ));
    }

    public static GameModeProvider getGameMode(LayoutTheme.LayoutId layout) {
        return gameModeProviders.get(layout);
    }

    public static final class GameModeProvider {
        private final Supplier<IChessGame> gameConstructor;
        private final String displayName;
        private final int numPlayers;
        private final PieceStore pieceStore;

        public GameModeProvider(Supplier<IChessGame> gameConstructor, String displayName, int numPlayers, PieceStore pieceStore) {
            this.gameConstructor = gameConstructor;
            this.displayName = displayName;
            this.numPlayers = numPlayers;
            this.pieceStore = pieceStore;
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

        public PieceStore getPieceStore() {
            return pieceStore;
        }
    }
}
