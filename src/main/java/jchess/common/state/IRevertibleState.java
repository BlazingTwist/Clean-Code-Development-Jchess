package jchess.common.state;

public interface IRevertibleState {
    void saveState();

    void revertState();
}
