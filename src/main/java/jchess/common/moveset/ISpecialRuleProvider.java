package jchess.common.moveset;

import jchess.common.IChessGame;
import jchess.common.components.PieceIdentifier;

@FunctionalInterface
public interface ISpecialRuleProvider {
    ISpecialRule createRule(IChessGame game, PieceIdentifier piece);
}
