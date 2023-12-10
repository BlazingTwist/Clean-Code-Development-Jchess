package jchess.game.server.adapter;

public interface IAdapter<TFrom, TTo> {
    TTo convert(TFrom data);
}
