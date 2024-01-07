package jchess.common.components;

import dx.schema.types.PieceType;
import jchess.common.theme.IIconKey;

/**
 * @param pieceType
 * @param shortName
 * @param iconKey
 * @param ownerId
 * @param forwardBasis A number in range [0, 360) indicating the forwards-direction of this piece. (With 0 being 'North')
 */
public record PieceIdentifier(
        PieceType pieceType,
        String shortName,
        IIconKey iconKey,
        int ownerId,
        int forwardBasis
) {
    @Override
    public String toString() {
        return "PieceIdentifier{" +
                "pieceType=" + pieceType +
                ", shortName='" + shortName + '\'' +
                ", iconKey=" + iconKey.getIconId() +
                ", ownerId=" + ownerId +
                ", forwardBasis=" + forwardBasis +
                '}';
    }
}
