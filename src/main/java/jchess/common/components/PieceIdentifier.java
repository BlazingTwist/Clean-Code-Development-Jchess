package jchess.common.components;

import jchess.common.theme.IIconKey;

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
    @Override
    public String toString() {
        return "PieceIdentifier{" +
                "pieceTypeId=" + pieceTypeId +
                ", shortName='" + shortName + '\'' +
                ", iconKey=" + iconKey.getIconId() +
                ", ownerId=" + ownerId +
                ", forwardBasis=" + forwardBasis +
                '}';
    }
}
