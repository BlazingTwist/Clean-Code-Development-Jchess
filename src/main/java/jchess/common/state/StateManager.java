package jchess.common.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StateManager {
    private final List<IRevertibleState> stateList = new ArrayList<>();

    public void registerState(IRevertibleState... states) {
        Collections.addAll(stateList, states);
    }

    public void saveState() {
        for (IRevertibleState state : stateList) {
            state.saveState();
        }
    }

    public void revertState() {
        for (IRevertibleState state : stateList) {
            state.revertState();
        }
    }
}
