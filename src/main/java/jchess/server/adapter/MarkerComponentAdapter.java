package jchess.server.adapter;

import jchess.common.components.MarkerComponent;

public enum MarkerComponentAdapter implements IAdapter<MarkerComponent, dx.schema.types.MarkerComponent> {
    Instance;

    @Override
    public dx.schema.types.MarkerComponent convert(MarkerComponent data) {
        if (data == null) return null;

        dx.schema.types.MarkerComponent result = new dx.schema.types.MarkerComponent();
        result.setMarkerType(data.markerType);
        return result;
    }
}
