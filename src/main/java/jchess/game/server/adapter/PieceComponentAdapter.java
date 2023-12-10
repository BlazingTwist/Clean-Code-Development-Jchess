package jchess.game.server.adapter;

import jchess.game.common.piece.PieceComponent;

public enum PieceComponentAdapter implements IAdapter<PieceComponent, dx.schema.types.PieceComponent> {
    Instance;

    @Override
    public dx.schema.types.PieceComponent convert(PieceComponent data) {
        if (data == null) return null;

        dx.schema.types.PieceComponent result = new dx.schema.types.PieceComponent();
        result.setIdentifier(PieceIdentifierAdapter.Instance.convert(data.identifier));
        return result;
    }
}
