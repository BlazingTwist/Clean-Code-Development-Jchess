package jchess.gamemode.hex2p;

import jchess.common.IChessGame;
import jchess.ecs.Entity;
import jchess.gamemode.IPieceLayoutProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

public enum Hex2pPieceLayouts implements IPieceLayoutProvider {
    Standard((game, tileProvider) -> populateStandardBoard(new BoardController(game, tileProvider))),
    Custom((game, tileProvider) -> populateCustomBoard(new BoardController(game, tileProvider))),
    ;

    private static final int PLAYER_LIGHT = 0;
    private static final int PLAYER_DARK = 1;

    private static final Logger logger = LoggerFactory.getLogger(Hex2pPieceLayouts.class);

    public final IPieceLayoutProvider layoutProvider;

    Hex2pPieceLayouts(IPieceLayoutProvider layoutProvider) {
        this.layoutProvider = layoutProvider;
    }

    @Override
    public void placePieces(IChessGame game, BiFunction<Integer, Integer, Entity> tileProvider) {
        layoutProvider.placePieces(game, tileProvider);
    }

    private static void populateStandardBoard(BoardController boardController) {
        // place kings
        boardController.placeKing(6, 19, PLAYER_LIGHT);
        boardController.placeKing(6, 1, PLAYER_DARK);

        // place queens
        //boardController.placeQueen(4, 19, PLAYER_LIGHT);
        //boardController.placeQueen(4, 1, PLAYER_DARK);


        // place bishops
        for (int i = 0; i <= 4; i+=2) {
            boardController.placeBishop(5, i, PLAYER_DARK);
            boardController.placeBishop(5, 20 - i, PLAYER_LIGHT);
        }
/*
        // place knights
        for (int x : new int[]{3, 7}) {
            boardController.placeKnight(x,18, PLAYER_LIGHT);
            boardController.placeKnight(x,2, PLAYER_DARK);
        }

        // place rooks
        for (int x : new int[] {2, 8}) {
            boardController.placeRook(x,17, PLAYER_LIGHT);
            boardController.placeRook(x,3, PLAYER_DARK);
        }

        // place pawns
        for (int i = 0; i < 5; i++) {
            for (int x : new int[] {1+i, 9-i}) {
                boardController.placePawn(x, 16 - i, PLAYER_LIGHT);
                boardController.placePawn(x, 4 + i, PLAYER_DARK);
            }
        }

         */

    }

    private static void populateCustomBoard(BoardController boardController) {
        populateStandardBoard(boardController);
    }

    private record BoardController(IChessGame game, BiFunction<Integer, Integer, Entity> tileProvider) {

        private Entity getEntityAtPosition(int x, int y) {
            return tileProvider.apply(x, y);
        }

        private void placeRook(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Rook);
        }

        private void placeKnight(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Knight);
        }

        private void placeBishop(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Bishop);
        }

        private void placeQueen(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Queen);
        }

        private void placeKing(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.King);
        }

        private void placePawn(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Pawn);
        }

        private void placeArcher(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Archer);
        }

        private void placePegasus(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Pegasus);
        }

        private void placeSkrull(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Skrull);
        }

        private void placeCatapult(int x, int y, int playerColor) {
            placePiece(x, y, playerColor, Hex2pPieces.Catapult);
        }

        private void placePiece(int x, int y, int playerColor, Hex2pPieces piece) {
            Entity tile = getEntityAtPosition(x, y);
            if (tile == null) {
                logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
                return;
            }
            game.createPiece(tile, piece.getPieceType(), playerColor);
        }
    }
}
