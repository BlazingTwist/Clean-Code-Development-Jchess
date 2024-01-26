package jchess.gamemode.hex3p;

import jchess.common.IChessGame;
import jchess.ecs.Entity;
import jchess.gamemode.IPieceLayoutProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

public enum Hex3pPieceLayouts implements IPieceLayoutProvider {
    Standard((game, tileProvider) -> populateStandardBoard(new BoardController(game, tileProvider))),
    Custom((game, tileProvider) -> populateCustomBoard(new BoardController(game, tileProvider))),
    ;

    private static final int PLAYER_LIGHT = 0;
    private static final int PLAYER_MEDIUM = 1;
    private static final int PLAYER_DARK = 2;

    private static final Logger logger = LoggerFactory.getLogger(Hex3pPieceLayouts.class);

    public final IPieceLayoutProvider layoutProvider;

    Hex3pPieceLayouts(IPieceLayoutProvider layoutProvider) {
        this.layoutProvider = layoutProvider;
    }

    @Override
    public void placePieces(IChessGame game, BiFunction<Integer, Integer, Entity> tileProvider) {
        layoutProvider.placePieces(game, tileProvider);
    }

    private static void populateStandardBoard(BoardController boardController) {
        populateBackRank_Standard(boardController);
        populateFrontRank_Standard(boardController);

        for (int i = 0; i < 10; i++) {
            boardController.placePawn(7 + (i * 2), 15, PLAYER_LIGHT);
            boardController.placePawn(31 - i, 9 - i, PLAYER_MEDIUM);
            boardController.placePawn(10 - i, i, PLAYER_DARK);
        }
    }

    private static void populateCustomBoard(BoardController boardController) {
        populateBackRank_Standard(boardController);
        populateFrontRank_Standard(boardController);

        boardController.placeSkrull(7, 15, PLAYER_LIGHT);
        boardController.placeSkrull(25, 15, PLAYER_LIGHT);

        boardController.placeSkrull(31, 9, PLAYER_MEDIUM);
        boardController.placeSkrull(22, 0, PLAYER_MEDIUM);

        boardController.placeSkrull(10, 0, PLAYER_DARK);
        boardController.placeSkrull(1, 9, PLAYER_DARK);

        boardController.placeArcher(9, 15, PLAYER_LIGHT);
        boardController.placeArcher(15, 15, PLAYER_LIGHT);
        boardController.placeArcher(17, 15, PLAYER_LIGHT);
        boardController.placeArcher(23, 15, PLAYER_LIGHT);

        boardController.placeArcher(30, 8, PLAYER_MEDIUM);
        boardController.placeArcher(27, 5, PLAYER_MEDIUM);
        boardController.placeArcher(26, 4, PLAYER_MEDIUM);
        boardController.placeArcher(23, 1, PLAYER_MEDIUM);

        boardController.placeArcher(9, 1, PLAYER_DARK);
        boardController.placeArcher(6, 4, PLAYER_DARK);
        boardController.placeArcher(5, 5, PLAYER_DARK);
        boardController.placeArcher(2, 8, PLAYER_DARK);

        boardController.placePegasus(11, 15, PLAYER_LIGHT);
        boardController.placePegasus(21, 15, PLAYER_LIGHT);

        boardController.placePegasus(29, 7, PLAYER_MEDIUM);
        boardController.placePegasus(24, 2, PLAYER_MEDIUM);

        boardController.placePegasus(8, 2, PLAYER_DARK);
        boardController.placePegasus(3, 7, PLAYER_DARK);

        boardController.placeCatapult(13, 15, PLAYER_LIGHT);
        boardController.placeCatapult(19, 15, PLAYER_LIGHT);

        boardController.placeCatapult(28, 6, PLAYER_MEDIUM);
        boardController.placeCatapult(25, 3, PLAYER_MEDIUM);

        boardController.placeCatapult(7, 3, PLAYER_DARK);
        boardController.placeCatapult(4, 6, PLAYER_DARK);
    }

    private static void populateBackRank_Standard(BoardController boardController) {
        boardController.placeRook(8, 16, PLAYER_LIGHT);
        boardController.placeRook(24, 16, PLAYER_LIGHT);
        boardController.placeRook(32, 8, PLAYER_MEDIUM);
        boardController.placeRook(24, 0, PLAYER_MEDIUM);
        boardController.placeRook(8, 0, PLAYER_DARK);
        boardController.placeRook(0, 8, PLAYER_DARK);

        boardController.placeKnight(10, 16, PLAYER_LIGHT);
        boardController.placeKnight(22, 16, PLAYER_LIGHT);
        boardController.placeKnight(31, 7, PLAYER_MEDIUM);
        boardController.placeKnight(25, 1, PLAYER_MEDIUM);
        boardController.placeKnight(7, 1, PLAYER_DARK);
        boardController.placeKnight(1, 7, PLAYER_DARK);

        boardController.placeBishop(12, 16, PLAYER_LIGHT);
        boardController.placeBishop(16, 16, PLAYER_LIGHT);
        boardController.placeBishop(20, 16, PLAYER_LIGHT);
        boardController.placeBishop(30, 6, PLAYER_MEDIUM);
        boardController.placeBishop(28, 4, PLAYER_MEDIUM);
        boardController.placeBishop(26, 2, PLAYER_MEDIUM);
        boardController.placeBishop(6, 2, PLAYER_DARK);
        boardController.placeBishop(4, 4, PLAYER_DARK);
        boardController.placeBishop(2, 6, PLAYER_DARK);

        boardController.placeQueen(14, 16, PLAYER_LIGHT);
        boardController.placeQueen(29, 5, PLAYER_MEDIUM);
        boardController.placeQueen(5, 3, PLAYER_DARK);

        boardController.placeKing(18, 16, PLAYER_LIGHT);
        boardController.placeKing(27, 3, PLAYER_MEDIUM);
        boardController.placeKing(3, 5, PLAYER_DARK);
    }

    private static void populateFrontRank_Standard(BoardController boardController) {
        for (int i = 0; i < 9; i++) {
            boardController.placePawn(8 + (i * 2), 14, PLAYER_LIGHT);
            boardController.placePawn(29 - i, 9 - i, PLAYER_MEDIUM);
            boardController.placePawn(11 - i, 1 + i, PLAYER_DARK);
        }
    }

    private record BoardController(IChessGame game, BiFunction<Integer, Integer, Entity> tileProvider) {

        private Entity getEntityAtPosition(int x, int y) {
            return tileProvider.apply(x, y);
        }

        private void placeRook(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Rook);
        }

        private void placeKnight(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Knight);
        }

        private void placeBishop(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Bishop);
        }

        private void placeQueen(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Queen);
        }

        private void placeKing(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.King);
        }

        private void placePawn(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Pawn);
        }

        private void placeArcher(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Archer);
        }

        private void placePegasus(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Pegasus);
        }

        private void placeSkrull(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Skrull);
        }

        private void placeCatapult(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex3pPieces.Catapult);
        }

        private void placePiece(int x, int y, int playerColor, Hex3pPieces piece) {
            Entity tile = getEntityAtPosition(x, y);
            if (tile == null) {
                logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
                return;
            }
            game.createPiece(tile, piece.getPieceType(), playerColor);
        }
    }
}
