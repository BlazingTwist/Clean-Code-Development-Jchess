package jchess.common.components;

import jchess.common.theme.IIconKey;

import java.util.function.Function;

/**
 * Marks a Tile or Piece for highlighting
 */
public class MarkerComponent {
    private final Function<MarkerType, IIconKey> iconKeySupplier;
    public MarkerType markerType;
    public Runnable onMarkerClicked;

    public MarkerComponent(Function<MarkerType, IIconKey> iconKeySupplier) {
        this.iconKeySupplier = iconKeySupplier;
    }

    public IIconKey getIconKey() {
        return iconKeySupplier.apply(markerType);
    }
}
