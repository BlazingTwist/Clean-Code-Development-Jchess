package jchess.game.common.piece;

import jchess.ecs.Entity;
import jchess.ecs.ISystem;
import jchess.game.common.RenderContext;

public class PieceRenderSystem implements ISystem<Void> {
    private final RenderContext context;

    public PieceRenderSystem(RenderContext context) {
        this.context = context;
    }

    @Override
    public boolean acceptsEntity(Entity entity) {
        return entity.tile != null && entity.piece != null;
    }

    @Override
    public void update(Entity entity, Void unused) {
        context.drawTile(entity.piece.identifier.icon(), entity.tile.position);
    }
}
