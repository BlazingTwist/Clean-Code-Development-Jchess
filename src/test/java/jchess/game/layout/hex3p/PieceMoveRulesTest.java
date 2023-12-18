package jchess.game.layout.hex3p;

import jchess.ecs.Entity;
import jchess.game.common.components.PieceComponent;
import jchess.game.common.components.PieceIdentifier;
import jchess.game.common.components.TileComponent;
import jchess.game.common.moveset.MoveIntention;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PieceMoveRulesTest {
    private static final Logger logger = LoggerFactory.getLogger(PieceMoveRulesTest.class);
    private final Entity[] testField = generateLittleTestField();

    private Entity[] generateLittleTestField() {
        Entity[] out = new Entity[13];
        for (int i = 0; i < out.length; i++) {
            Entity entity = new Entity();
            TileComponent tile = new TileComponent();
            out[i] = entity;
            out[i].tile = tile;
        }
        out[0].tile.neighborsByDirection.put(90, out[1]);
        out[0].tile.neighborsByDirection.put(60, out[10]);
        out[0].tile.neighborsByDirection.put(120, out[5]);
        out[0].tile.neighborsByDirection.put(150, out[4]);
        out[0].tile.neighborsByDirection.put(180, out[11]);

        out[1].tile.neighborsByDirection.put(30, out[10]);
        out[1].tile.neighborsByDirection.put(90, out[2]);
        out[1].tile.neighborsByDirection.put(120, out[6]);
        out[1].tile.neighborsByDirection.put(150, out[5]);
        out[1].tile.neighborsByDirection.put(180, out[7]);
        out[1].tile.neighborsByDirection.put(210, out[4]);
        out[1].tile.neighborsByDirection.put(270, out[0]);

        out[2].tile.neighborsByDirection.put(90, out[3]);
        out[2].tile.neighborsByDirection.put(150, out[6]);
        out[2].tile.neighborsByDirection.put(180, out[8]);
        out[2].tile.neighborsByDirection.put(210, out[5]);
        out[2].tile.neighborsByDirection.put(240, out[4]);
        out[2].tile.neighborsByDirection.put(270, out[1]);
        out[2].tile.neighborsByDirection.put(330, out[10]);

        out[3].tile.neighborsByDirection.put(180, out[12]);
        out[3].tile.neighborsByDirection.put(210, out[6]);
        out[3].tile.neighborsByDirection.put(240, out[5]);
        out[3].tile.neighborsByDirection.put(270, out[2]);
        out[3].tile.neighborsByDirection.put(300, out[10]);

        out[4].tile.neighborsByDirection.put(30, out[1]);
        out[4].tile.neighborsByDirection.put(60, out[2]);
        out[4].tile.neighborsByDirection.put(90, out[5]);
        out[4].tile.neighborsByDirection.put(120, out[8]);
        out[4].tile.neighborsByDirection.put(150, out[7]);
        out[4].tile.neighborsByDirection.put(210, out[11]);
        out[4].tile.neighborsByDirection.put(330, out[0]);

        out[5].tile.neighborsByDirection.put(0, out[10]);
        out[5].tile.neighborsByDirection.put(30, out[2]);
        out[5].tile.neighborsByDirection.put(60, out[3]);
        out[5].tile.neighborsByDirection.put(90, out[6]);
        out[5].tile.neighborsByDirection.put(120, out[12]);
        out[5].tile.neighborsByDirection.put(150, out[8]);
        out[5].tile.neighborsByDirection.put(180, out[9]);
        out[5].tile.neighborsByDirection.put(210, out[7]);
        out[5].tile.neighborsByDirection.put(240, out[11]);
        out[5].tile.neighborsByDirection.put(270, out[4]);
        out[5].tile.neighborsByDirection.put(300, out[0]);
        out[5].tile.neighborsByDirection.put(330, out[1]);

        out[6].tile.neighborsByDirection.put(30, out[3]);
        out[6].tile.neighborsByDirection.put(150, out[12]);
        out[6].tile.neighborsByDirection.put(210, out[8]);
        out[6].tile.neighborsByDirection.put(240, out[7]);
        out[6].tile.neighborsByDirection.put(270, out[5]);
        out[6].tile.neighborsByDirection.put(300, out[1]);
        out[6].tile.neighborsByDirection.put(330, out[2]);

        out[7].tile.neighborsByDirection.put(0, out[1]);
        out[7].tile.neighborsByDirection.put(30, out[5]);
        out[7].tile.neighborsByDirection.put(60, out[6]);
        out[7].tile.neighborsByDirection.put(90, out[8]);
        out[7].tile.neighborsByDirection.put(150, out[9]);
        out[7].tile.neighborsByDirection.put(270, out[11]);
        out[7].tile.neighborsByDirection.put(330, out[4]);

        out[8].tile.neighborsByDirection.put(0, out[2]);
        out[8].tile.neighborsByDirection.put(30, out[6]);
        out[8].tile.neighborsByDirection.put(90, out[12]);
        out[8].tile.neighborsByDirection.put(210, out[9]);
        out[8].tile.neighborsByDirection.put(270, out[7]);
        out[8].tile.neighborsByDirection.put(300, out[4]);
        out[8].tile.neighborsByDirection.put(330, out[5]);

        out[9].tile.neighborsByDirection.put(0, out[5]);
        out[9].tile.neighborsByDirection.put(30, out[8]);
        out[9].tile.neighborsByDirection.put(60, out[12]);
        out[9].tile.neighborsByDirection.put(300, out[11]);
        out[9].tile.neighborsByDirection.put(330, out[7]);

        out[10].tile.neighborsByDirection.put(120, out[3]);
        out[10].tile.neighborsByDirection.put(150, out[2]);
        out[10].tile.neighborsByDirection.put(180, out[5]);
        out[10].tile.neighborsByDirection.put(210, out[1]);
        out[10].tile.neighborsByDirection.put(240, out[0]);

        out[11].tile.neighborsByDirection.put(0, out[0]);
        out[11].tile.neighborsByDirection.put(30, out[4]);
        out[11].tile.neighborsByDirection.put(60, out[5]);
        out[11].tile.neighborsByDirection.put(90, out[7]);
        out[11].tile.neighborsByDirection.put(120, out[9]);

        out[12].tile.neighborsByDirection.put(0, out[3]);
        out[12].tile.neighborsByDirection.put(240, out[9]);
        out[12].tile.neighborsByDirection.put(270, out[8]);
        out[12].tile.neighborsByDirection.put(300, out[5]);
        out[12].tile.neighborsByDirection.put(330, out[6]);

        return out;
    }

    private void MoveTestNoOtherPieces(PieceType pieceType, int tileToTest, int[] expectedTiles) {
        PieceIdentifier identifier = new PieceIdentifier(pieceType.getId(), pieceType.getShortName(), null, 0, 0);
        PieceComponent piece = new PieceComponent(null, identifier, pieceType.getBaseMoves());

        List<Entity> moves = piece.findValidMoves(testField[tileToTest], false).map(MoveIntention::displayTile).toList();
        List<Entity> expectedMoves = Arrays.stream(expectedTiles).mapToObj(expectedTile -> testField[expectedTile]).toList();
        Assertions.assertEquals(new HashSet<>(expectedMoves), new HashSet<>(moves), "Set of possible moves does not match the expected ones. piece: '" + pieceType + "'");
    }

    @Test
    public void pawnMoveTest() {
        logger.info("--- Pawn Test ---");
        MoveTestNoOtherPieces(PieceType.Pawn, 0, new int[]{});
        MoveTestNoOtherPieces(PieceType.Pawn, 1, new int[]{10});
        MoveTestNoOtherPieces(PieceType.Pawn, 5, new int[]{1, 2});
        MoveTestNoOtherPieces(PieceType.Pawn, 9, new int[]{7, 8});//TODO wenn 2er move fertig eingebaut, 4 und 6 hinzufügen ins array
    }

    @Test
    public void rookMoveTest() {
        logger.info("--- Rook Test ---");
        MoveTestNoOtherPieces(PieceType.Rook, 0, new int[]{1, 2, 3, 4, 7, 9});
        MoveTestNoOtherPieces(PieceType.Rook, 3, new int[]{0, 1, 2, 6, 8, 9});
        MoveTestNoOtherPieces(PieceType.Rook, 5, new int[]{1, 2, 4, 6, 7, 8});
        MoveTestNoOtherPieces(PieceType.Rook, 9, new int[]{0, 3, 4, 6, 7, 8});
        MoveTestNoOtherPieces(PieceType.Rook, 10, new int[]{1, 2, 4, 6, 11, 12});
        MoveTestNoOtherPieces(PieceType.Rook, 11, new int[]{1, 4, 7, 8, 10, 12});
        MoveTestNoOtherPieces(PieceType.Rook, 12, new int[]{2, 6, 7, 8, 10, 11});
    }

    @Test
    public void knightMoveTest() {
        logger.info("--- Knight Test ---");
        MoveTestNoOtherPieces(PieceType.Knight, 0, new int[]{6, 8});
        MoveTestNoOtherPieces(PieceType.Knight, 3, new int[]{4, 7});
        MoveTestNoOtherPieces(PieceType.Knight, 5, new int[]{});
        MoveTestNoOtherPieces(PieceType.Knight, 9, new int[]{1, 2});
        MoveTestNoOtherPieces(PieceType.Knight, 10, new int[]{7, 8});
        MoveTestNoOtherPieces(PieceType.Knight, 11, new int[]{2, 6});
        MoveTestNoOtherPieces(PieceType.Knight, 12, new int[]{1, 4});
    }

    @Test
    public void bishopMoveTest() {
        logger.info("--- Bishop Test ---");
        MoveTestNoOtherPieces(PieceType.Bishop, 1, new int[]{6, 7});
        MoveTestNoOtherPieces(PieceType.Bishop, 5, new int[]{0, 3, 9, 10, 11, 12});
        MoveTestNoOtherPieces(PieceType.Bishop, 10, new int[]{0, 3, 5, 9});
    }

    @Test
    public void kingMoveTest() {
        logger.info("--- King Test ---");
        MoveTestNoOtherPieces(PieceType.King, 0, new int[]{1, 4, 5, 10, 11});
        MoveTestNoOtherPieces(PieceType.King, 1, new int[]{0, 2, 4, 5, 6, 7, 10});
        MoveTestNoOtherPieces(PieceType.King, 5, new int[]{0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12});
    }

    @Test
    public void queenMoveTest() {
        logger.info("--- Queen Test ---");
        MoveTestNoOtherPieces(PieceType.Queen, 5, new int[]{0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12});
        MoveTestNoOtherPieces(PieceType.Queen, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 9, 11, 12});
    }
}
