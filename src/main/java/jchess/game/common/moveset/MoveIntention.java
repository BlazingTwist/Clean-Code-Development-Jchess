package jchess.game.common.moveset;

import jchess.ecs.Entity;

public record MoveIntention(Entity displayTile, Runnable onClick) {
}
