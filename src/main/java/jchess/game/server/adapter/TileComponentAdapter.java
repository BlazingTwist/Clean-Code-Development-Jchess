package jchess.game.server.adapter;

import jchess.game.common.tile.TileComponent;

public enum TileComponentAdapter implements IAdapter<TileComponent, dx.schema.types.TileComponent> {
    Instance;

    @Override
    public dx.schema.types.TileComponent convert(TileComponent data) {
        if (data == null) return null;

        dx.schema.types.TileComponent result = new dx.schema.types.TileComponent();
        result.setPosition(Vector2IAdapter.Instance.convert(data.position));
        result.setIconId(data.iconId);
        return result;
    }
}
