package jchess.game.common.marker;

import jchess.ecs.Entity;
import jchess.ecs.ISystem;
import jchess.game.common.RenderContext;

import java.awt.Image;

public class MarkerRenderSystem implements ISystem<Void> {
    private final RenderContext context;
    private Image marker_noAction;
    private Image marker_yesAction;
    private Image marker_selected;

    public MarkerRenderSystem(RenderContext context) {
        this.context = context;
    }

    public void setMarkerImages(Image noAction, Image yesAction, Image selected) {
        marker_noAction = noAction;
        marker_yesAction = yesAction;
        marker_selected = selected;
    }

    @Override
    public boolean acceptsEntity(Entity entity) {
        return entity.marker != null && entity.tile != null;
    }

    @Override
    public void update(Entity entity, Void unused) {
        context.drawTile(getMarkerIcon(entity.marker.markerType), entity.tile.position);
    }

    private Image getMarkerIcon(MarkerType type) {
        return switch (type) {
            case NoAction -> marker_noAction;
            case YesAction -> marker_yesAction;
            case Selection -> marker_selected;
        };
    }
}
