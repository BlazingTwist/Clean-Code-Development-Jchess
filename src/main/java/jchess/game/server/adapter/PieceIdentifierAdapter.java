package jchess.game.server.adapter;

import jchess.game.common.piece.PieceIdentifier;

public enum PieceIdentifierAdapter implements IAdapter<PieceIdentifier, dx.schema.types.PieceIdentifier> {
    Instance;

    @Override
    public dx.schema.types.PieceIdentifier convert(PieceIdentifier data) {
        if (data == null) return null;

        dx.schema.types.PieceIdentifier result = new dx.schema.types.PieceIdentifier();
        result.setPieceTypeId("" + data.pieceTypeId());
        result.setShortName(data.shortName());
        result.setIconId(data.iconId());
        result.setOwnerId(data.ownerId());
        result.setForwardBasis(data.forwardBasis());
        return result;
    }
}
