package jchess.gamemode.hex3p;

import jchess.ecs.Entity;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.components.TileComponent;
import jchess.common.moveset.MoveIntention;
import jchess.gamemode.PieceStore;
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
        TileComponent[] tiles = new TileComponent[out.length];
        for (int i = 0; i < out.length; i++) {
            tiles[i] = new TileComponent(null, 0);
            out[i] = new Entity();
            out[i].tile = tiles[i];
        }
        tiles[0].neighborsByDirection.put(90, out[1]);
        tiles[0].neighborsByDirection.put(60, out[10]);
        tiles[0].neighborsByDirection.put(120, out[5]);
        tiles[0].neighborsByDirection.put(150, out[4]);
        tiles[0].neighborsByDirection.put(180, out[11]);

        tiles[1].neighborsByDirection.put(30, out[10]);
        tiles[1].neighborsByDirection.put(90, out[2]);
        tiles[1].neighborsByDirection.put(120, out[6]);
        tiles[1].neighborsByDirection.put(150, out[5]);
        tiles[1].neighborsByDirection.put(180, out[7]);
        tiles[1].neighborsByDirection.put(210, out[4]);
        tiles[1].neighborsByDirection.put(270, out[0]);

        tiles[2].neighborsByDirection.put(90, out[3]);
        tiles[2].neighborsByDirection.put(150, out[6]);
        tiles[2].neighborsByDirection.put(180, out[8]);
        tiles[2].neighborsByDirection.put(210, out[5]);
        tiles[2].neighborsByDirection.put(240, out[4]);
        tiles[2].neighborsByDirection.put(270, out[1]);
        tiles[2].neighborsByDirection.put(330, out[10]);

        tiles[3].neighborsByDirection.put(180, out[12]);
        tiles[3].neighborsByDirection.put(210, out[6]);
        tiles[3].neighborsByDirection.put(240, out[5]);
        tiles[3].neighborsByDirection.put(270, out[2]);
        tiles[3].neighborsByDirection.put(300, out[10]);

        tiles[4].neighborsByDirection.put(30, out[1]);
        tiles[4].neighborsByDirection.put(60, out[2]);
        tiles[4].neighborsByDirection.put(90, out[5]);
        tiles[4].neighborsByDirection.put(120, out[8]);
        tiles[4].neighborsByDirection.put(150, out[7]);
        tiles[4].neighborsByDirection.put(210, out[11]);
        tiles[4].neighborsByDirection.put(330, out[0]);

        tiles[5].neighborsByDirection.put(0, out[10]);
        tiles[5].neighborsByDirection.put(30, out[2]);
        tiles[5].neighborsByDirection.put(60, out[3]);
        tiles[5].neighborsByDirection.put(90, out[6]);
        tiles[5].neighborsByDirection.put(120, out[12]);
        tiles[5].neighborsByDirection.put(150, out[8]);
        tiles[5].neighborsByDirection.put(180, out[9]);
        tiles[5].neighborsByDirection.put(210, out[7]);
        tiles[5].neighborsByDirection.put(240, out[11]);
        tiles[5].neighborsByDirection.put(270, out[4]);
        tiles[5].neighborsByDirection.put(300, out[0]);
        tiles[5].neighborsByDirection.put(330, out[1]);

        tiles[6].neighborsByDirection.put(30, out[3]);
        tiles[6].neighborsByDirection.put(150, out[12]);
        tiles[6].neighborsByDirection.put(210, out[8]);
        tiles[6].neighborsByDirection.put(240, out[7]);
        tiles[6].neighborsByDirection.put(270, out[5]);
        tiles[6].neighborsByDirection.put(300, out[1]);
        tiles[6].neighborsByDirection.put(330, out[2]);

        tiles[7].neighborsByDirection.put(0, out[1]);
        tiles[7].neighborsByDirection.put(30, out[5]);
        tiles[7].neighborsByDirection.put(60, out[6]);
        tiles[7].neighborsByDirection.put(90, out[8]);
        tiles[7].neighborsByDirection.put(150, out[9]);
        tiles[7].neighborsByDirection.put(270, out[11]);
        tiles[7].neighborsByDirection.put(330, out[4]);

        tiles[8].neighborsByDirection.put(0, out[2]);
        tiles[8].neighborsByDirection.put(30, out[6]);
        tiles[8].neighborsByDirection.put(90, out[12]);
        tiles[8].neighborsByDirection.put(210, out[9]);
        tiles[8].neighborsByDirection.put(270, out[7]);
        tiles[8].neighborsByDirection.put(300, out[4]);
        tiles[8].neighborsByDirection.put(330, out[5]);

        tiles[9].neighborsByDirection.put(0, out[5]);
        tiles[9].neighborsByDirection.put(30, out[8]);
        tiles[9].neighborsByDirection.put(60, out[12]);
        tiles[9].neighborsByDirection.put(300, out[11]);
        tiles[9].neighborsByDirection.put(330, out[7]);

        tiles[10].neighborsByDirection.put(120, out[3]);
        tiles[10].neighborsByDirection.put(150, out[2]);
        tiles[10].neighborsByDirection.put(180, out[5]);
        tiles[10].neighborsByDirection.put(210, out[1]);
        tiles[10].neighborsByDirection.put(240, out[0]);

        tiles[11].neighborsByDirection.put(0, out[0]);
        tiles[11].neighborsByDirection.put(30, out[4]);
        tiles[11].neighborsByDirection.put(60, out[5]);
        tiles[11].neighborsByDirection.put(90, out[7]);
        tiles[11].neighborsByDirection.put(120, out[9]);

        tiles[12].neighborsByDirection.put(0, out[3]);
        tiles[12].neighborsByDirection.put(240, out[9]);
        tiles[12].neighborsByDirection.put(270, out[8]);
        tiles[12].neighborsByDirection.put(300, out[5]);
        tiles[12].neighborsByDirection.put(330, out[6]);

        return out;
    }

    private void moveTestNoOtherPieces(Hex3pPieces pieceType, int tileToTest, int[] expectedTiles) {
        PieceStore.PieceDefinition pieceDefinition = pieceType.getPieceDefinition();
        PieceIdentifier identifier = new PieceIdentifier(pieceType.getPieceType(), pieceDefinition.shortName(), 0, 0);
        PieceComponent piece = new PieceComponent(null, identifier, pieceDefinition.baseMoves());

        List<Entity> moves = piece.findValidMoves(null, testField[tileToTest], false).map(MoveIntention::displayTile).toList();
        List<Entity> expectedMoves = Arrays.stream(expectedTiles).mapToObj(expectedTile -> testField[expectedTile]).toList();
        Assertions.assertEquals(new HashSet<>(expectedMoves), new HashSet<>(moves), "Set of possible moves does not match the expected ones. piece: '" + pieceType + "'");
    }

    @Test
    public void pawnMoveTest() {
        logger.info("--- Pawn Test ---");
        moveTestNoOtherPieces(Hex3pPieces.Pawn, 0, new int[]{});
        moveTestNoOtherPieces(Hex3pPieces.Pawn, 1, new int[]{10});
        moveTestNoOtherPieces(Hex3pPieces.Pawn, 5, new int[]{1, 2});
        moveTestNoOtherPieces(Hex3pPieces.Pawn, 9, new int[]{7, 8});//TODO wenn 2er move fertig eingebaut, 4 und 6 hinzufügen ins array
    }

    @Test
    public void rookMoveTest() {
        logger.info("--- Rook Test ---");
        moveTestNoOtherPieces(Hex3pPieces.Rook, 0, new int[]{1, 2, 3, 4, 7, 9});
        moveTestNoOtherPieces(Hex3pPieces.Rook, 3, new int[]{0, 1, 2, 6, 8, 9});
        moveTestNoOtherPieces(Hex3pPieces.Rook, 5, new int[]{1, 2, 4, 6, 7, 8});
        moveTestNoOtherPieces(Hex3pPieces.Rook, 9, new int[]{0, 3, 4, 6, 7, 8});
        moveTestNoOtherPieces(Hex3pPieces.Rook, 10, new int[]{1, 2, 4, 6, 11, 12});
        moveTestNoOtherPieces(Hex3pPieces.Rook, 11, new int[]{1, 4, 7, 8, 10, 12});
        moveTestNoOtherPieces(Hex3pPieces.Rook, 12, new int[]{2, 6, 7, 8, 10, 11});
    }

    @Test
    public void knightMoveTest() {
        logger.info("--- Knight Test ---");
        moveTestNoOtherPieces(Hex3pPieces.Knight, 0, new int[]{6, 8});
        moveTestNoOtherPieces(Hex3pPieces.Knight, 3, new int[]{4, 7});
        moveTestNoOtherPieces(Hex3pPieces.Knight, 5, new int[]{});
        moveTestNoOtherPieces(Hex3pPieces.Knight, 9, new int[]{1, 2});
        moveTestNoOtherPieces(Hex3pPieces.Knight, 10, new int[]{7, 8});
        moveTestNoOtherPieces(Hex3pPieces.Knight, 11, new int[]{2, 6});
        moveTestNoOtherPieces(Hex3pPieces.Knight, 12, new int[]{1, 4});
    }

    @Test
    public void bishopMoveTest() {
        logger.info("--- Bishop Test ---");
        moveTestNoOtherPieces(Hex3pPieces.Bishop, 1, new int[]{6, 7});
        moveTestNoOtherPieces(Hex3pPieces.Bishop, 5, new int[]{0, 3, 9, 10, 11, 12});
        moveTestNoOtherPieces(Hex3pPieces.Bishop, 10, new int[]{0, 3, 5, 9});
    }

    @Test
    public void kingMoveTest() {
        logger.info("--- King Test ---");
        moveTestNoOtherPieces(Hex3pPieces.King, 0, new int[]{1, 4, 5, 10, 11});
        moveTestNoOtherPieces(Hex3pPieces.King, 1, new int[]{0, 2, 4, 5, 6, 7, 10});
        moveTestNoOtherPieces(Hex3pPieces.King, 5, new int[]{0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12});
    }

    @Test
    public void queenMoveTest() {
        logger.info("--- Queen Test ---");
        moveTestNoOtherPieces(Hex3pPieces.Queen, 5, new int[]{0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12});
        moveTestNoOtherPieces(Hex3pPieces.Queen, 10, new int[]{0, 1, 2, 3, 4, 5, 6, 9, 11, 12});
    }
}
