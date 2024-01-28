package jchess.common.state.impl;

import jchess.common.state.IRevertibleState;

import java.util.function.Function;

public class ArrayState<T> implements IRevertibleState {
    private final T[] current;
    private final T[] saved;

    public ArrayState(int size, Function<Integer, T[]> arrayConstructor) {
        current = arrayConstructor.apply(size);
        saved = arrayConstructor.apply(size);
    }

    public T[] getCurrent() {
        return current;
    }

    @Override
    public void saveState() {
        System.arraycopy(current, 0, saved, 0, current.length);
    }

    @Override
    public void revertState() {
        System.arraycopy(saved, 0, current, 0, saved.length);
    }
}
