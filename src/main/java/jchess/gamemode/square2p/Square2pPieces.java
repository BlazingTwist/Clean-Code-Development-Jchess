package jchess.gamemode.square2p;

import dx.schema.message.Piece;
import dx.schema.types.PieceType;
import jchess.common.moveset.special.*;
import jchess.ecs.Entity;
import jchess.el.v2.TileExpression;
import jchess.gamemode.PieceStore;

import java.awt.Point;
import java.util.stream.Stream;

public enum Square2pPieces implements PieceStore.IPieceDefinitionProvider {
    Rook(PieceType.ROOK, new PieceStore.PieceDefinition(
            "R",
            TileExpression.regex("0+ 90+ 180+ 270+", false)
    )),
    Knight(PieceType.KNIGHT, new PieceStore.PieceDefinition(
            "N",
            TileExpression.regex("0.315 0.45 90.45 90.135 180.135 180.225 270.225 270.315", true)
    )),
    Bishop(PieceType.BISHOP, new PieceStore.PieceDefinition(
            "B",
            TileExpression.regex("45+ 135+ 225+ 315+", false)
    )),
    Queen(PieceType.QUEEN, new PieceStore.PieceDefinition(
            "Q",
            TileExpression.or(Rook.pieceDefinition.baseMoves(), Bishop.pieceDefinition.baseMoves())
    )),
    King(PieceType.KING, new PieceStore.PieceDefinition(
            "K",
            TileExpression.regex("0 45 90 135 180 225 270 315", false),
            (game, kingIdentifier) -> new Castling(game, kingIdentifier, Rook.pieceType, 90, 270,
                    TileExpression.regex("270.270", true), TileExpression.regex("90.90", true))
    )),
    Pawn(PieceType.PAWN, new PieceStore.PieceDefinition(
            "",
            TileExpression.or(
                    TileExpression.filter(TileExpression.neighbor(0), TileExpression.FILTER_EMPTY_TILE),
                    TileExpression.filter2(TileExpression.neighbor(45, 315), TileExpression.FILTER_CAPTURE)
            ),
            (game, pawnIdentifier) -> new SpecialFirstMove(
                    game, pawnIdentifier,
                    TileExpression.filter(TileExpression.regex("0.0", false), TileExpression.FILTER_EMPTY_TILE)
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
    Archer(PieceType.ARCHER,new PieceStore.PieceDefinition(
            "A",
            TileExpression.filter(TileExpression.regex("0{1,2} 45{1,2} 90{1,2} 135{1,2} 180{1,2} 225{1,2} 270{1,2} 315{1,2}", false),
                    TileExpression.FILTER_EMPTY_TILE)
            ,
            (game, archerIdentifier) -> new RangedAttack(game, archerIdentifier, 0, 2)
    )),

    Pegasus(PieceType.PEGASUS,new PieceStore.PieceDefinition(
            "PE",
            TileExpression.repeat(TileExpression.regex("0 45 90 135 180 225 270 315",true), 0,3, true)
    )),
    Catapult(PieceType.CATAPULT,new PieceStore.PieceDefinition(
            "A",
            TileExpression.filter(TileExpression.regex("0 45 90 135 180 225 270 315", false),
                    TileExpression.FILTER_EMPTY_TILE)
            ,
            (game, archerIdentifier) -> new RangedAttack(game, archerIdentifier, 3, 3)
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
