package jchess.game.common.moveset;

import jchess.game.common.IChessGame;
import jchess.game.common.components.PieceIdentifier;

@FunctionalInterface
public interface ISpecialRuleProvider {
    ISpecialRule createRule(IChessGame game, PieceIdentifier piece);
}
