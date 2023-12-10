package jchess.game.server.adapter;

import jchess.game.common.marker.MarkerComponent;

public enum MarkerComponentAdapter implements IAdapter<MarkerComponent, dx.schema.types.MarkerComponent> {
    Instance;

    @Override
    public dx.schema.types.MarkerComponent convert(MarkerComponent data) {
        if (data == null) return null;

        dx.schema.types.MarkerComponent result = new dx.schema.types.MarkerComponent();
        result.setMarkerType(dx.schema.types.MarkerComponent.MarkerType.fromValue(data.markerType.name()));
        return result;
    }
}
