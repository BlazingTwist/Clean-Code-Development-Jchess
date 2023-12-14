package jchess.game.server.adapter;

import jchess.game.common.components.MarkerComponent;

public enum MarkerComponentAdapter implements IAdapter<MarkerComponent, dx.schema.types.MarkerComponent> {
    Instance;

    @Override
    public dx.schema.types.MarkerComponent convert(MarkerComponent data) {
        if (data == null) return null;

        dx.schema.types.MarkerComponent result = new dx.schema.types.MarkerComponent();
        result.setMarkerType(dx.schema.types.MarkerComponent.MarkerType.fromValue(data.markerType.name()));
        result.setIconId(data.getIconKey().getIconId());
        return result;
    }
}
