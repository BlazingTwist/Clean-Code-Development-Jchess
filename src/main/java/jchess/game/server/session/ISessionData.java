package jchess.game.server.session;

import java.io.Closeable;

public interface ISessionData extends Closeable {
    boolean isStillUsed();
}
