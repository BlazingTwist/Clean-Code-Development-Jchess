package jchess.game.layout.square2p;

import jchess.ecs.Entity;
import jchess.game.common.piece.PieceIdentifier;

import java.awt.Point;

public class MoveInfo {
    // TODO erja

    public final Point fromPos;
    public final Point toPos;
    public final PieceIdentifier piece;

    public MoveInfo(Entity fromTile, Entity toTile) {
        fromPos = fromTile.tile.position;
        toPos = toTile.tile.position;
        piece = toTile.piece.identifier;
    }
}
