package jchess.gamemode;

import dx.schema.types.PieceType;
import jchess.common.moveset.ISpecialRuleProvider;
import jchess.el.TileExpression;

import java.util.HashMap;
import java.util.Map;

public final class PieceStore {

    private final Map<PieceType, IPieceDefinitionProvider> pieceDefinitions = new HashMap<>();

    public PieceStore(IPieceDefinitionProvider... pieceDefinitions) {
        for (IPieceDefinitionProvider piece : pieceDefinitions) {
            this.pieceDefinitions.put(piece.getPieceType(), piece);
        }
    }

    public IPieceDefinitionProvider getPiece(PieceType pieceType) {
        return pieceDefinitions.get(pieceType);
    }

    public record PieceDefinition(String shortName, TileExpression baseMoves, ISpecialRuleProvider... specialRules) {
    }

    public interface IPieceDefinitionProvider {
        PieceType getPieceType();

        PieceDefinition getPieceDefinition();
    }
}
