package jchess.gamemode.square2p;

import jchess.common.moveset.special.Castling;
import jchess.common.moveset.special.EnPassant;
import jchess.common.moveset.ISpecialRuleProvider;
import jchess.common.moveset.special.SpecialFirstMove;
import jchess.el.TileExpression;

public enum PieceType {
    Rook(
            0, "R",
            TileExpression.regex("0+ 90+ 180+ 270+", false)
    ),
    Knight(
            1, "N",
            TileExpression.regex("0.315 0.45 90.45 90.135 180.135 180.225 270.225 270.315", true)
    ),
    Bishop(
            2, "B",
            TileExpression.regex("45+ 135+ 225+ 315+", false)
    ),
    Queen(
            3, "Q",
            TileExpression.or(Rook.baseMoves, Bishop.baseMoves)
    ),
    King(
            4, "K",
            TileExpression.regex("0 45 90 135 180 225 270 315", false),
            (game, kingIdentifier) -> new Castling(game, kingIdentifier, Rook.id, 90, 270,
                    TileExpression.regex("270.270", true), TileExpression.regex("90.90", true))
    ),
    Pawn(
            5, "",
            TileExpression.or(
                    TileExpression.filter(TileExpression.neighbor(0), TileExpression.FILTER_EMPTY_TILE),
                    TileExpression.filter(TileExpression.neighbor(45, 315), TileExpression.FILTER_CAPTURE)
            ),
            (game, pawnIdentifier) -> new SpecialFirstMove(
                    game, pawnIdentifier,
                    TileExpression.filter(TileExpression.regex("0.0", false), TileExpression.FILTER_EMPTY_TILE)
            ),
            (game, pawnIdentifier) -> new EnPassant(game, pawnIdentifier, 5, new int[]{0}, new int[]{45, 315})
    );

    private final int id;
    private final String shortName;
    private final TileExpression baseMoves;
    private final ISpecialRuleProvider[] specialRules;

    PieceType(int id, String shortName, TileExpression baseMoves, ISpecialRuleProvider... specialRules) {
        this.id = id;
        this.shortName = shortName;
        this.baseMoves = baseMoves;
        this.specialRules = specialRules;
    }

    public int getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public TileExpression getBaseMoves() {
        return baseMoves;
    }

    public ISpecialRuleProvider[] getSpecialRules() {
        return specialRules;
    }
}
