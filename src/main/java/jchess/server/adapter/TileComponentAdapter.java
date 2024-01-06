package jchess.server.adapter;

import jchess.common.components.TileComponent;

public enum TileComponentAdapter implements IAdapter<TileComponent, dx.schema.types.TileComponent> {
    Instance;

    @Override
    public dx.schema.types.TileComponent convert(TileComponent data) {
        if (data == null) return null;

        dx.schema.types.TileComponent result = new dx.schema.types.TileComponent();
        result.setTileId(TileComponent.getTileId(data));
        result.setDisplayPos(Vector2IAdapter.Instance.convert(data.position));
        result.setIconId(data.iconKey.getIconId());
        return result;
    }
}
