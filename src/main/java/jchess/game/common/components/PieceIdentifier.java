package jchess.game.common.components;

import jchess.game.common.theme.IIconKey;

/**
 * @param pieceTypeId
 * @param shortName
 * @param iconKey
 * @param ownerId
 * @param forwardBasis A number in range [0, 360) indicating the forwards-direction of this piece. (With 0 being 'North')
 */
public record PieceIdentifier(
        int pieceTypeId,
        String shortName,
        IIconKey iconKey,
        int ownerId,
        int forwardBasis
) {
}