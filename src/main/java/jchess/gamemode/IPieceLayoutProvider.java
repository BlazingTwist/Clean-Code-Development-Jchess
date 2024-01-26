package jchess.gamemode;

import jchess.common.IChessGame;
import jchess.ecs.Entity;

import java.util.function.BiFunction;

@FunctionalInterface
public interface IPieceLayoutProvider {
    void placePieces(IChessGame game, BiFunction<Integer, Integer, Entity> tileProvider);
}
