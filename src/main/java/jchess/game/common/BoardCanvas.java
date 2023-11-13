package jchess.game.common;

import jchess.ecs.EcsEvent;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class BoardCanvas extends Canvas {

    private final List<IDrawCall> drawCalls = new ArrayList<>();

    private final IBoardTransform transform;

    public BoardCanvas(EcsEvent<Void> renderEvent, IBoardTransform transform) {
        renderEvent.addPreEventListener(unused -> this.drawCalls.clear());
        renderEvent.addPostEventListener(unused -> this.repaint());
        this.transform = transform;
    }

    public void drawTile(Image image, Point position) {
        drawCalls.add(new DrawCall(image, position));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int canvasWidth = this.getWidth();
        int canvasHeight = this.getHeight();
        for (IDrawCall drawCall : drawCalls) {
            drawCall.draw(g, canvasWidth, canvasHeight);
        }
    }

    private class DrawCall implements IDrawCall {
        private final Image image;
        private final Point pos;
        private final int sourceWidth;
        private final int sourceHeight;

        public DrawCall(Image image, Point pos) {
            this.image = image;
            this.pos = pos;
            this.sourceWidth = image.getWidth(null);
            this.sourceHeight = image.getHeight(null);
        }

        @Override
        public void draw(Graphics g, int canvasWidth, int canvasHeight) {
            TransformInfo transformInfo = transform.transformTile(image, pos, canvasWidth, canvasHeight);

            int tileX0 = transformInfo.destX;
            int tileY0 = transformInfo.destY;
            int tileX1 = transformInfo.destX + transformInfo.destWidth;
            int tileY1 = transformInfo.destY + transformInfo.destHeight;

            g.drawImage(image, tileX0, tileY0, tileX1, tileY1, 0, 0, sourceWidth, sourceHeight, null);
        }
    }

    public interface IBoardTransform {
        TransformInfo transformTile(Image icon, Point position, int canvasWidth, int canvasHeight);
    }

    public record TransformInfo(int destX, int destY, int destWidth, int destHeight) {
    }

    private interface IDrawCall {
        void draw(Graphics g, int canvasWidth, int canvasHeight);
    }
}
