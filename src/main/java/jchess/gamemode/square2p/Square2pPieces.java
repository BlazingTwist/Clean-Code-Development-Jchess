package jchess.gamemode.square2p;

import dx.schema.message.Piece;
import dx.schema.types.PieceType;
import jchess.common.moveset.special.*;
import jchess.ecs.Entity;
import jchess.el.v2.TileExpression;
import jchess.gamemode.PieceStore;

import java.awt.Point;
import java.util.stream.Stream;

import static jchess.el.v2.TileExpression.neighbor;
import static jchess.el.v2.TileExpression.regex;
import static jchess.el.v2.TileExpression.rotations;

public enum Square2pPieces implements PieceStore.IPieceDefinitionProvider {
    Rook(PieceType.ROOK, new PieceStore.PieceDefinition(
            "R",
            rotations(regex("0+", false), 4)
    )),
    Knight(PieceType.KNIGHT, new PieceStore.PieceDefinition(
            "N",
            rotations(regex("0.315 0.45", true), 4)
    )),
    Bishop(PieceType.BISHOP, new PieceStore.PieceDefinition(
            "B",
            rotations(regex("45+", false), 4)
    )),
    Queen(PieceType.QUEEN, new PieceStore.PieceDefinition(
            "Q",
            TileExpression.or(Rook.pieceDefinition.baseMoves(), Bishop.pieceDefinition.baseMoves())
    )),
    King(PieceType.KING, new PieceStore.PieceDefinition(
            "K",
            rotations(regex("0", false), 8),
            (game, kingIdentifier) -> new Castling(
                    game, kingIdentifier, Rook.pieceType, 90, 270,
                    regex("270.270", true), regex("90.90", true)
            )
    )),
    Pawn(PieceType.PAWN, new PieceStore.PieceDefinition(
            "",
            TileExpression.or(
                    TileExpression.filter(TileExpression.neighbor(0), TileExpression.FILTER_EMPTY_TILE),
                    TileExpression.filter2(TileExpression.neighbor(45, 315), TileExpression.FILTER_CAPTURE)
            ),
            (game, pawnIdentifier) -> new SpecialFirstMove(
                    game, pawnIdentifier,
                    TileExpression.filter(regex("0.0", false), TileExpression.FILTER_EMPTY_TILE)
            ),
            (game, pawnIdentifier) -> new EnPassant(game, pawnIdentifier, PieceType.PAWN, new int[]{0}, new int[]{45, 315}),
            (game, pawnIdentifier) -> {
                int owner = pawnIdentifier.ownerId();
                return new PawnPromotion(
                        game, Square2pPieces::isPromotionTile,
                        Stream.of(Rook, Knight, Bishop, Queen).map(type -> getPiece(type, owner)).toArray(Piece[]::new)
                );
            }
    )),
    Archer(PieceType.ARCHER, new PieceStore.PieceDefinition(
            "A",
            TileExpression.filter(
                    rotations(regex("0{1,2}", false), 8),
                    TileExpression.FILTER_EMPTY_TILE
            ),
            (game, archerIdentifier) -> new RangedAttack(
                    game, archerIdentifier,
                    rotations(regex("(0 45){1,3}", false), 8)
            )
    )),

    Pegasus(PieceType.PEGASUS, new PieceStore.PieceDefinition(
            "PE",
            rotations(regex("(0 45){1,2}", true), 8)
    )),
    Catapult(PieceType.CATAPULT, new PieceStore.PieceDefinition(
            "C",
            TileExpression.filter(rotations(neighbor(0), 8), TileExpression.FILTER_EMPTY_TILE),
            (game, catapultId) -> new RangedAttack(
                    game, catapultId,
                    rotations(regex("(0 45){3}", true), 8)
            )
    ));

    private final PieceType pieceType;
    private final PieceStore.PieceDefinition pieceDefinition;

    Square2pPieces(PieceType pieceType, PieceStore.PieceDefinition pieceDefinition) {
        this.pieceType = pieceType;
        this.pieceDefinition = pieceDefinition;
    }

    private static boolean isPromotionTile(Entity tile) {
        if (tile.tile == null) return false;

        Point position = tile.tile.position;
        return position.y == 0 || position.y == 7;
    }

    private static Piece getPiece(Square2pPieces pieceType, int ownerId) {
        Piece result = new Piece();
        result.setPieceTypeId(pieceType.pieceType);
        result.setPlayerIdx(ownerId);
        return result;
    }

    @Override
    public PieceType getPieceType() {
        return pieceType;
    }

    @Override
    public PieceStore.PieceDefinition getPieceDefinition() {
        return pieceDefinition;
    }
}
