package jchess.game.common.piece;

/**
 * @param pieceTypeId
 * @param shortName
 * @param iconId
 * @param ownerId
 * @param forwardBasis A number in range [0, 360) indicating the forwards-direction of this piece. (With 0 being 'North')
 */
public record PieceIdentifier(
        int pieceTypeId,
        String shortName,
        String iconId,
        int ownerId,
        int forwardBasis
) {
}
