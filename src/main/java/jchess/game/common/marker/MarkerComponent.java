package jchess.game.common.marker;

import java.util.function.Function;

/**
 * Marks a Tile or Piece for highlighting
 */
public class MarkerComponent {
    private final Function<MarkerType, String> iconIdSupplier;
    public MarkerType markerType;
    public Runnable onMarkerClicked;

    public MarkerComponent(Function<MarkerType, String> iconIdSupplier) {
        this.iconIdSupplier = iconIdSupplier;
    }

    public String getIconId() {
        return iconIdSupplier.apply(markerType);
    }
}
