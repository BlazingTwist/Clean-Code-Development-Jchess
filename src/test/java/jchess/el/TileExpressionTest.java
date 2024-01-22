package jchess.el;

import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.components.TileComponent;
import jchess.ecs.Entity;
import jchess.el.v2.ExpressionCompiler;
import jchess.el.v2.TileExpression;
import jchess.gamemode.PieceStore;
import jchess.gamemode.hex3p.Hex3pPieces;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TileExpressionTest {
    private static Entity createPiece(Hex3pPieces pieceType, int player) {
        PieceStore.PieceDefinition pieceDef = pieceType.getPieceDefinition();
        PieceIdentifier identifier = new PieceIdentifier(pieceType.getPieceType(), pieceDef.shortName(), player, 0);

        Entity entity = new Entity();
        entity.piece = new PieceComponent(null, identifier, TileExpression.or());
        return entity;
    }

    @Test
    public void test_filterCapture() {
        final int player1 = 0;
        final int player2 = 1;
        Hex3pPieces pawn = Hex3pPieces.Pawn;
        Hex3pPieces rook = Hex3pPieces.Rook;

        Entity piece_1a = createPiece(pawn, player1);
        Entity piece_1b = createPiece(rook, player1);

        Entity piece_2a = createPiece(pawn, player2);
        Entity piece_2b = createPiece(rook, player2);

        Entity emptyTile = new Entity();

        assert piece_1a.piece != null;
        assert piece_1b.piece != null;
        assert piece_2a.piece != null;
        assert piece_2b.piece != null;

        // can capture opponent pieces
        Assertions.assertTrue(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_1a.piece.identifier, piece_2a));
        Assertions.assertTrue(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_1a.piece.identifier, piece_2b));
        Assertions.assertTrue(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_2a.piece.identifier, piece_1a));
        Assertions.assertTrue(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_2a.piece.identifier, piece_1b));

        // can not capture own pieces
        Assertions.assertFalse(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_1a.piece.identifier, piece_1b));
        Assertions.assertFalse(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_1b.piece.identifier, piece_1a));
        Assertions.assertFalse(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_2a.piece.identifier, piece_2b));
        Assertions.assertFalse(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_2b.piece.identifier, piece_2a));

        // can not capture on empty tile
        Assertions.assertFalse(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_1a.piece.identifier, emptyTile));
        Assertions.assertFalse(filterAcceptsTile(TileExpression.FILTER_CAPTURE, piece_2b.piece.identifier, emptyTile));
    }

    private static boolean filterAcceptsTile(ExpressionCompiler expression, PieceIdentifier pieceId, Entity tile) {
        return expression.toV1(pieceId).findTiles(tile).collect(Collectors.toSet()).contains(tile);
    }

    @ParameterizedTest
    @CsvSource({
            "90, 0, 90, 0", // expect forwardBasis is used for neighbor check
            "0, 90, 90, 0", // expect neighborDirection is used for neighbor check
            "270, 90, 0, 360", // expect 90° right of 270° overflows back to 0°
            "269, 90, 359, 0", // expect 90° right of 269° does not overflow
    })
    void test_neighborExpression(int forwardBasis, int neighborDirection, int expectedDirection, int unexpectedDirection) {
        TileComponent tile = Mockito.mock(TileComponent.class);
        Mockito.when(tile.getTile(Mockito.anyInt())).thenReturn(null);

        Entity entity = new Entity();
        entity.tile = tile;

        Hex3pPieces pawn = Hex3pPieces.Pawn;
        PieceStore.PieceDefinition pawnDef = pawn.getPieceDefinition();
        PieceIdentifier pieceIdentifier = new PieceIdentifier(pawn.getPieceType(), pawnDef.shortName(), 0, forwardBasis);

        //noinspection ResultOfMethodCallIgnored
        TileExpression.neighbor(neighborDirection).compile(pieceIdentifier).apply(Stream.of(entity)).toList();
        Mockito.verify(tile, Mockito.atLeastOnce()).getTile(expectedDirection);
        Mockito.verify(tile, Mockito.never()).getTile(unexpectedDirection);
    }
}
