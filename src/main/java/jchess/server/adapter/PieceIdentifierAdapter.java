package jchess.server.adapter;

import jchess.common.components.PieceIdentifier;

public enum PieceIdentifierAdapter implements IAdapter<PieceIdentifier, dx.schema.types.PieceIdentifier> {
    Instance;

    @Override
    public dx.schema.types.PieceIdentifier convert(PieceIdentifier data) {
        if (data == null) return null;

        dx.schema.types.PieceIdentifier result = new dx.schema.types.PieceIdentifier();
        result.setPieceTypeId("" + data.pieceType());
        result.setShortName(data.shortName());
        result.setIconId(data.iconKey().getIconId());
        result.setOwnerId(data.ownerId());
        result.setForwardBasis(data.forwardBasis());
        return result;
    }
}
