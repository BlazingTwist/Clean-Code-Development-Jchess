package jchess.game.server.adapter;

import jchess.ecs.Entity;

public enum EntityAdapter implements IAdapter<Entity, dx.schema.types.Entity> {
    Instance;

    @Override
    public dx.schema.types.Entity convert(Entity data) {
        if (data == null) return null;

        dx.schema.types.Entity result = new dx.schema.types.Entity();

        result.setTile(TileComponentAdapter.Instance.convert(data.tile));
        result.setPiece(PieceComponentAdapter.Instance.convert(data.piece));
        result.setMarker(MarkerComponentAdapter.Instance.convert(data.marker));

        return result;
    }
}
