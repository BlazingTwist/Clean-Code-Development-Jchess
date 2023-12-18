package jchess.common.moveset;

import jchess.ecs.Entity;

import java.util.List;

/**
 * This interface can be used to define special rules.
 * <p>For example:</p>
 * <ul>
 *     <li>Castling</li>
 *     <li>Pawn Promotion</li>
 *     <li>Pawn double move for its first move only</li>
 *     <li>En Passant</li>
 * </ul>
 */
public interface ISpecialRule {

    List<MoveIntention> getSpecialMoves(Entity movingPiece);

}
