package jchess.gamemode.square2p;

import jchess.common.IChessGame;
import jchess.ecs.Entity;
import jchess.gamemode.IPieceLayoutProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

public enum Square2pPieceLayouts implements IPieceLayoutProvider {
    Standard((game, tileProvider) -> populateStandardBoard(new BoardController(game, tileProvider))),
    Custom((game, tileProvider) -> populateCustomBoard(new BoardController(game, tileProvider))),
    ;

    private static final Logger logger = LoggerFactory.getLogger(Square2pPieceLayouts.class);

    public final IPieceLayoutProvider layoutProvider;

    Square2pPieceLayouts(IPieceLayoutProvider layoutProvider) {
        this.layoutProvider = layoutProvider;
    }

    @Override
    public void placePieces(IChessGame game, BiFunction<Integer, Integer, Entity> tileProvider) {
        layoutProvider.placePieces(game, tileProvider);
    }

    private static void populateStandardBoard(BoardController boardController) {
        populateBackRank_Standard(boardController);

        for (int x = 0; x < 8; x++) {
            boardController.placePawn(x, 1, false);
            boardController.placePawn(x, 6, true);
        }
    }

    private static void populateCustomBoard(BoardController boardController) {
        populateBackRank_Standard(boardController);

        boardController.placeCatapult(0, 1, false);
        boardController.placeCatapult(7, 1, false);
        boardController.placeCatapult(0, 6, true);
        boardController.placeCatapult(7, 6, true);

        boardController.placePegasus(1, 1, false);
        boardController.placePegasus(6, 1, false);
        boardController.placePegasus(1, 6, true);
        boardController.placePegasus(6, 6, true);

        boardController.placeArcher(2, 1, false);
        boardController.placeArcher(5, 1, false);
        boardController.placeArcher(2, 6, true);
        boardController.placeArcher(5, 6, true);

        for (int x = 0; x < 8; x++) {
            boardController.placePawn(x, 2, false);
            boardController.placePawn(x, 5, true);
        }
    }

    private static void populateBackRank_Standard(BoardController boardController) {
        boardController.placeRook(0, 0, false);
        boardController.placeRook(7, 0, false);
        boardController.placeRook(0, 7, true);
        boardController.placeRook(7, 7, true);

        boardController.placeKnight(1, 0, false);
        boardController.placeKnight(6, 0, false);
        boardController.placeKnight(1, 7, true);
        boardController.placeKnight(6, 7, true);

        boardController.placeBishop(2, 0, false);
        boardController.placeBishop(5, 0, false);
        boardController.placeBishop(2, 7, true);
        boardController.placeBishop(5, 7, true);

        boardController.placeQueen(3, 0, false);
        boardController.placeQueen(3, 7, true);

        boardController.placeKing(4, 0, false);
        boardController.placeKing(4, 7, true);
    }

    @SuppressWarnings("SameParameterValue")
    private record BoardController(IChessGame game, BiFunction<Integer, Integer, Entity> tileProvider) {

        private Entity getEntityAtPosition(int x, int y) {
            return tileProvider.apply(x, y);
        }

        private void placeRook(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.Rook);
        }

        private void placeKnight(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.Knight);
        }

        private void placeBishop(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.Bishop);
        }

        private void placeQueen(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.Queen);
        }

        private void placeKing(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.King);
        }

        private void placePawn(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.Pawn);
        }

        private void placeArcher(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.Archer);
        }

        private void placePegasus(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.Pegasus);
        }

        private void placeCatapult(int x, int y, boolean isWhite) {
            placePiece(x, y, isWhite, Square2pPieces.Catapult);
        }

        private void placePiece(int x, int y, boolean isWhite, Square2pPieces piece) {
            Entity tile = getEntityAtPosition(x, y);
            if (tile == null) {
                logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
                return;
            }
            game.createPiece(tile, piece.getPieceType(), isWhite ? 0 : 1);
        }
    }
}
