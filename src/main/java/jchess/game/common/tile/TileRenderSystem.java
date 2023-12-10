package jchess.game.common.tile;

import jchess.ecs.Entity;
import jchess.ecs.ISystem;
import jchess.game.common.RenderContext;

public class TileRenderSystem implements ISystem<Void> {
    private final RenderContext context;

    public TileRenderSystem(RenderContext context) {
        this.context = context;
    }

    @Override
    public boolean acceptsEntity(Entity entity) {
        return entity.tile != null;
    }

    @Override
    public void update(Entity entity, Void unused) {
        // TODO erja RenderSystem is dead.
        //context.drawTile(entity.tile.iconId, entity.tile.position);
    }
}
