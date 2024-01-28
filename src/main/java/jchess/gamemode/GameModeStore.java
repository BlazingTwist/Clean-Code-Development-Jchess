package jchess.gamemode;

import dx.schema.types.LayoutId;
import jchess.common.IChessGame;
import jchess.gamemode.hex2p.Hex2pPieceLayouts;
import jchess.gamemode.hex2p.Hex2pPieces;
import jchess.gamemode.hex2p.Hex2PlayerGame;
import jchess.gamemode.hex3p.Hex3PlayerGame;
import jchess.gamemode.hex3p.Hex3pPieceLayouts;
import jchess.gamemode.hex3p.Hex3pPieces;
import jchess.gamemode.square2p.Square2PlayerGame;
import jchess.gamemode.square2p.Square2pPieceLayouts;
import jchess.gamemode.square2p.Square2pPieces;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameModeStore {

    private static final Map<String, GameModeProvider> gameModeProviders = new HashMap<>();

    static {
        for (Hex3pPieceLayouts pieceLayout : Hex3pPieceLayouts.values()) {
            gameModeProviders.put(LayoutId.HEX_3_P.name() + "." + pieceLayout.name(), new GameModeProvider(
                    Hex3PlayerGame::new, LayoutId.HEX_3_P, "3 Player Hexagonal Chess - " + pieceLayout.name(), 3,
                    new PieceStore(Hex3pPieces.values()), pieceLayout
            ));
        }

        for (Hex2pPieceLayouts pieceLayout : Hex2pPieceLayouts.values()) {
            gameModeProviders.put(LayoutId.HEX_2_P.name() + "." + pieceLayout.name(), new GameModeProvider(
                    Hex2PlayerGame::new, LayoutId.HEX_2_P, "2 Player Hexagonal Chess - " + pieceLayout.name(), 2,
                    new PieceStore(Hex2pPieces.values()), pieceLayout
            ));
        }

        for (Square2pPieceLayouts pieceLayout : Square2pPieceLayouts.values()) {
            gameModeProviders.put(LayoutId.SQUARE_2_P.name() + "." + pieceLayout.name(), new GameModeProvider(
                    Square2PlayerGame::new, LayoutId.SQUARE_2_P, "2 Player Classic Chess - " + pieceLayout.name(), 2,
                    new PieceStore(Square2pPieces.values()), pieceLayout
            ));
        }
    }

    public static Set<String> getGameModeIds() {
        return gameModeProviders.keySet();
    }

    public static GameModeProvider getGameMode(String modeId) {
        return gameModeProviders.get(modeId);
    }

    public record GameModeProvider(
            GameConstructor gameConstructor, LayoutId layoutId, String displayName, int numPlayers, PieceStore pieceStore, IPieceLayoutProvider pieceLayout
    ) {
        public IChessGame newGame() {
            return gameConstructor.newGame(pieceStore, pieceLayout);
        }
    }

    @FunctionalInterface
    public interface GameConstructor {
        IChessGame newGame(PieceStore pieceStore, IPieceLayoutProvider layoutProvider);
    }
}
