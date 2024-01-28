package jchess.common.state.impl;

import jchess.common.state.IRevertibleState;

public class BooleanState implements IRevertibleState {
    private boolean current;
    private boolean saved;

    public BooleanState(boolean initValue) {
        this.current = initValue;
    }

    public boolean getValue() {
        return current;
    }

    public void setValue(boolean current) {
        this.current = current;
    }

    @Override
    public void saveState() {
        saved = current;
    }

    @Override
    public void revertState() {
        current = saved;
    }
}
