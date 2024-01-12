package jchess.common.components;

import dx.schema.types.MarkerType;

/**
 * Marks a Tile or Piece for highlighting
 */
public class MarkerComponent {
    public MarkerType markerType;
    public Runnable onMarkerClicked;
}
