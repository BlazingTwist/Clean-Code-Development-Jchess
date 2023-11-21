package jchess.game.common;

import jchess.ecs.EcsEvent;
import jchess.ecs.EntityManager;

import java.awt.Image;
import java.awt.Point;

public class RenderContext {
    public final EntityManager entityManager;
    public final EcsEvent<Void> renderEvent;
    public final BoardCanvas boardCanvas;

    public RenderContext(EntityManager entityManager, EcsEvent<Void> renderEvent, BoardCanvas boardCanvas) {
        this.entityManager = entityManager;
        this.renderEvent = renderEvent;
        this.boardCanvas = boardCanvas;
    }

    public void drawTile(Image image, Point position) {
        boardCanvas.drawTile(image, position);
    }
}
