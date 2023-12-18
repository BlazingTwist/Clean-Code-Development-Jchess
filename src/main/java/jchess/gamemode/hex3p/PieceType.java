package jchess.gamemode.hex3p;

import jchess.common.moveset.special.Castling;
import jchess.common.moveset.ISpecialRuleProvider;
import jchess.common.moveset.special.EnPassant;
import jchess.common.moveset.special.SpecialFirstMove;
import jchess.el.TileExpression;

public enum PieceType {
    Rook(
            0, "R",
            TileExpression.regex("30+ 90+ 150+ 210+ 270+ 330+", false)
    ),
    Knight(
            1, "N",
            TileExpression.regex("30.0 30.60 90.60 90.120 150.120 150.180 210.180 210.240 270.240 270.300 330.300 330.0", true)
    ),
    Bishop(
            2, "B",
            TileExpression.regex("0+ 60+ 120+ 180+ 240+ 300+", false)
    ),
    Queen(
            3, "Q",
            TileExpression.or(Rook.baseMoves, Bishop.baseMoves)
    ),
    King(
            4, "K",
            TileExpression.regex("0 30 60 90 120 150 180 210 240 270 300 330", false),
            (game, kingIdentifier) -> new Castling(game, kingIdentifier, Rook.id, 90, 270,
                    TileExpression.regex("270.270.270", true), TileExpression.regex("90.90", true))
    ),
    Pawn(
            5, "",
            TileExpression.or(
                    TileExpression.filter(TileExpression.neighbor(330, 30), TileExpression.FILTER_EMPTY_TILE),
                    TileExpression.filter(TileExpression.neighbor(300, 60), TileExpression.FILTER_CAPTURE)
            ),
            (game, pawnIdentifier) -> new SpecialFirstMove(
                    game, pawnIdentifier,
                    TileExpression.filter(TileExpression.regex("330.330 30.30", false), TileExpression.FILTER_EMPTY_TILE)
            ),
            (game, pawnId) -> new EnPassant(game, pawnId, 5, new int[]{330, 30}, new int[]{300, 60})
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
