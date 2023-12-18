package jchess.server.adapter;

public interface IAdapter<TFrom, TTo> {
    TTo convert(TFrom data);
}
