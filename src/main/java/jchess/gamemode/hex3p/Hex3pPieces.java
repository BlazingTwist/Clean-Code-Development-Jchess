package jchess.gamemode.hex3p;

import dx.schema.types.PieceType;
import jchess.common.moveset.special.Castling;
import jchess.common.moveset.special.EnPassant;
import jchess.common.moveset.special.PawnPromotion;
import jchess.common.moveset.special.SpecialFirstMove;
import jchess.ecs.Entity;
import jchess.el.CompiledTileExpression;
import jchess.el.TileExpression;
import jchess.gamemode.PieceStore;

import java.util.function.Predicate;
import java.util.stream.Stream;

public enum Hex3pPieces implements PieceStore.IPieceDefinitionProvider {
    Rook(PieceType.ROOK, new PieceStore.PieceDefinition(
            "R",
            TileExpression.regex("30+ 90+ 150+ 210+ 270+ 330+", false)
    )),
    Knight(PieceType.KNIGHT, new PieceStore.PieceDefinition(
            "N",
            TileExpression.regex("30.0 30.60 90.60 90.120 150.120 150.180 210.180 210.240 270.240 270.300 330.300 330.0", true)
    )),
    Bishop(PieceType.BISHOP, new PieceStore.PieceDefinition(
            "B",
            TileExpression.regex("0+ 60+ 120+ 180+ 240+ 300+", false)
    )),
    Queen(PieceType.QUEEN, new PieceStore.PieceDefinition(
            "Q",
            TileExpression.or(Rook.pieceDefinition.baseMoves(), Bishop.pieceDefinition.baseMoves())
    )),
    King(PieceType.KING, new PieceStore.PieceDefinition(
            "K",
            TileExpression.regex("0 30 60 90 120 150 180 210 240 270 300 330", false),
            (game, kingIdentifier) -> new Castling(game, kingIdentifier, Rook.pieceType, 90, 270,
                    TileExpression.regex("270.270.270", true), TileExpression.regex("90.90", true))
    )),
    Pawn(PieceType.PAWN, new PieceStore.PieceDefinition(
            "",
            TileExpression.or(
                    TileExpression.filter(TileExpression.neighbor(330, 30), TileExpression.FILTER_EMPTY_TILE),
                    TileExpression.filter(TileExpression.neighbor(300, 60), TileExpression.FILTER_CAPTURE)
            ),
            (game, pawnIdentifier) -> new SpecialFirstMove(
                    game, pawnIdentifier,
                    TileExpression.filter(TileExpression.regex("330.330 30.30", false), TileExpression.FILTER_EMPTY_TILE)
            ),
            (game, pawnId) -> new EnPassant(game, pawnId, PieceType.PAWN, new int[]{330, 30}, new int[]{300, 60}),
            (game, pawnId) -> {
                int owner = pawnId.ownerId();
                return new PawnPromotion(
                        game, getPromotionTilePredicate(TileExpression.neighbor(330, 30).compile(pawnId)),
                        Stream.of(Rook, Knight, Bishop, Queen).map(type -> getPiece(type, owner)).toArray(dx.schema.message.Piece[]::new)
                );
            }
    ));

    private final PieceType pieceType;
    private final PieceStore.PieceDefinition pieceDefinition;

    Hex3pPieces(PieceType pieceType, PieceStore.PieceDefinition pieceDefinition) {
        this.pieceType = pieceType;
        this.pieceDefinition = pieceDefinition;
    }

    private static Predicate<Entity> getPromotionTilePredicate(CompiledTileExpression forwardTiles) {
        return tile -> {
            if (tile.tile == null) return false;

            // if both forward tiles exit the board bounds (= empty result) -> pawn can promote
            return forwardTiles.findTiles(tile).findAny().isEmpty();
        };
    }

    private static dx.schema.message.Piece getPiece(Hex3pPieces pieceType, int ownerId) {
        Theme.PieceColor ownerColor = switch (ownerId) {
            case 1 -> Theme.PieceColor.medium;
            case 2 -> Theme.PieceColor.dark;
            default -> Theme.PieceColor.light;
        };

        dx.schema.message.Piece result = new dx.schema.message.Piece();
        result.setPieceTypeId(pieceType.pieceType);
        // TODO erja, obtain themeProvider from game??
        // result.setIconId(pieceType.icon.getIconKey(ownerColor));
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