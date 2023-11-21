package jchess.game.common.piece;

import java.awt.Image;

/**
 * @param pieceTypeId
 * @param shortName
 * @param icon
 * @param ownerId
 * @param forwardBasis A number in range [0, 360) indicating the forwards-direction of this piece. (With 0 being 'North')
 */
public record PieceIdentifier(
        int pieceTypeId,
        String shortName,
        Image icon,
        int ownerId,
        int forwardBasis
) {
}
