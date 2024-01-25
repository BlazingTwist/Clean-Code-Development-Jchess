package jchess.gamemode;

import dx.schema.types.PieceType;
import jchess.common.moveset.ISpecialRuleProvider;
import jchess.el.v2.ExpressionCompiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PieceStore {

    private final Map<PieceType, IPieceDefinitionProvider> pieceDefinitions = new HashMap<>();

    public PieceStore(IPieceDefinitionProvider... pieceDefinitions) {
        for (IPieceDefinitionProvider piece : pieceDefinitions) {
            this.pieceDefinitions.put(piece.getPieceType(), piece);
        }
    }

    public Set<PieceType> getPieces() {
        return pieceDefinitions.keySet();
    }

    public IPieceDefinitionProvider getPiece(PieceType pieceType) {
        return pieceDefinitions.get(pieceType);
    }

    public record PieceDefinition(String shortName, ExpressionCompiler baseMoves, ISpecialRuleProvider... specialRules) {
    }

    public interface IPieceDefinitionProvider {
        PieceType getPieceType();

        PieceDefinition getPieceDefinition();
    }
}
