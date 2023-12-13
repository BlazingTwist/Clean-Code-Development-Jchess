package jchess.game.server.adapter;

import dx.schema.types.Vector2I;

import java.awt.Point;

public enum Vector2IAdapter implements IAdapter<Point, Vector2I> {
    Instance;

    @Override
    public Vector2I convert(Point data) {
        if (data == null) return null;

        Vector2I result = new Vector2I();
        result.setX(data.x);
        result.setY(data.y);
        return result;
    }

    public static Point toPoint(Vector2I vector) {
        if(vector == null) return new Point(0, 0);
        return new Point(vector.getX(), vector.getY());
    }
}
