package jchess.gamemode.square2p;

import dx.schema.message.Piece;
import jchess.common.moveset.ISpecialRuleProvider;
import jchess.common.moveset.special.Castling;
import jchess.common.moveset.special.EnPassant;
import jchess.common.moveset.special.PawnPromotion;
import jchess.common.moveset.special.SpecialFirstMove;
import jchess.ecs.Entity;
import jchess.el.TileExpression;

import java.awt.*;

public enum PieceType {
    Rook(
            0, "R", Theme.PieceIcons.rook,
            TileExpression.regex("0+ 90+ 180+ 270+", false)
    ),
    Knight(
            1, "N", Theme.PieceIcons.knight,
            TileExpression.regex("0.315 0.45 90.45 90.135 180.135 180.225 270.225 270.315", true)
    ),
    Bishop(
            2, "B", Theme.PieceIcons.bishop,
            TileExpression.regex("45+ 135+ 225+ 315+", false)
    ),
    Queen(
            3, "Q", Theme.PieceIcons.queen,
            TileExpression.or(Rook.baseMoves, Bishop.baseMoves)
    ),
    King(
            4, "K", Theme.PieceIcons.king,
            TileExpression.regex("0 45 90 135 180 225 270 315", false),
            (game, kingIdentifier) -> new Castling(game, kingIdentifier, Rook.id, 90, 270,
                    TileExpression.regex("270.270", true), TileExpression.regex("90.90", true))
    ),
    Pawn(
            5, "", Theme.PieceIcons.pawn,
            TileExpression.or(
                    TileExpression.filter(TileExpression.neighbor(0), TileExpression.FILTER_EMPTY_TILE),
                    TileExpression.filter(TileExpression.neighbor(45, 315), TileExpression.FILTER_CAPTURE)
            ),
            (game, pawnIdentifier) -> new SpecialFirstMove(
                    game, pawnIdentifier,
                    TileExpression.filter(TileExpression.regex("0.0", false), TileExpression.FILTER_EMPTY_TILE)
            ),
            (game, pawnIdentifier) -> new EnPassant(game, pawnIdentifier, 5, new int[]{0}, new int[]{45, 315}),
            (game, pawnIdentifier) -> {
                int owner = pawnIdentifier.ownerId();
                return new PawnPromotion(
                        game, PieceType::isPromotionTile,
                        getPiece(Rook, owner), getPiece(Knight, owner), getPiece(Bishop, owner), getPiece(Queen, owner)
                );
            }
    );

    private final int id;
    private final String shortName;
    private final Theme.PieceIcons icon;
    private final TileExpression baseMoves;
    private final ISpecialRuleProvider[] specialRules;

    PieceType(int id, String shortName, Theme.PieceIcons icon, TileExpression baseMoves, ISpecialRuleProvider... specialRules) {
        this.id = id;
        this.shortName = shortName;
        this.icon = icon;
        this.baseMoves = baseMoves;
        this.specialRules = specialRules;
    }

    public int getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public Theme.PieceIcons getIcon() {
        return icon;
    }

    public TileExpression getBaseMoves() {
        return baseMoves;
    }

    public ISpecialRuleProvider[] getSpecialRules() {
        return specialRules;
    }

    private static boolean isPromotionTile(Entity tile) {
        if (tile.tile == null) return false;

        Point position = tile.tile.position;
        return position.y == 0 || position.y == 7;
    }

    private static Piece getPiece(PieceType pieceType, int ownerId) {
        Piece result = new Piece();
        result.setPieceTypeId("" + pieceType.getId());
        result.setIconId(pieceType.icon.getIconKey(ownerId == 0 ? Theme.PieceColor.light : Theme.PieceColor.dark));
        return result;
    }
}
