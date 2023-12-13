package jchess.game.server.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.UUID;

/**
 * Hält generische Sitzungsinformationen mit Timeout.
 */
public class SessionManager<T extends ISessionData> {

    public static class Session<T> {

        public final String sessionId;
        public final T sessionData;
        private long lastRenewalTime;

        public Session(String sessionId, T sessionData) {
            this.sessionId = sessionId;
            this.sessionData = sessionData;
            lastRenewalTime = System.currentTimeMillis();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final long sessionTimeoutMillis;
    private final Object sessionMapSynchronizationLock = new Object();
    private final Map<String, Session<T>> sessionsById = new HashMap<>();
    private final BucketTreeMap<Long, Session<T>> sessionsByLastRenewalTime = new BucketTreeMap<>();

    public SessionManager(long sessionTimeoutMillis) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
    }

    /**
     * Creates and stores a Session
     * @return the created Session
     */
    public Session<T> createSession(T data) {
        String sessionId = UUID.randomUUID().toString();
        return createSession(sessionId, data);
    }

    /**
     * Creates and stores a Session with a custom ID
     * @return the created Session
     */
    public Session<T> createSession(String sessionId, T data) {
        Session<T> session = new Session<>(sessionId, data);

        synchronized (sessionMapSynchronizationLock) {
            sessionsById.put(sessionId, session);
            sessionsByLastRenewalTime.put(session.lastRenewalTime, session);
        }

        return session;
    }

    /**
     * Retrieves and renews a session
     * @param sessionId id of the session
     * @return {@link Session} associated with the id
     */
    public Session<T> getSession(String sessionId) {
        Session<T> session = sessionsById.get(sessionId);
        renewSession(session);
        return session;
    }

    /**
     * Retrieves a Session without renewing it
     * @param sessionId id of the session
     * @return {@link Session} associated with the id
     */
    public Session<T> peekSession(String sessionId) {
        return sessionsById.get(sessionId);
    }

    /**
     * Renews the session, resetting the timeout until the session is discarded
     * @param session the session to renew
     */
    public void renewSession(Session<T> session) {
        renewSession(session, System.currentTimeMillis());
    }

    /**
     * Renews the session, resetting the timeout until the session is discarded
     * @param session           the session to renew
     * @param renewalTimeMillis timestamp in milliseconds to use for renewal
     */
    public void renewSession(Session<T> session, long renewalTimeMillis) {
        if (session == null) {
            return;
        }

        synchronized (sessionMapSynchronizationLock) {
            if (!sessionsByLastRenewalTime.remove(session.lastRenewalTime, session)) {
                logger.error("Unable to find session with id '{}' and renewalTime '{}' in sessionsByLastRenewalTime.", session.sessionId, session.lastRenewalTime);
            }

            session.lastRenewalTime = renewalTimeMillis;
            sessionsByLastRenewalTime.put(renewalTimeMillis, session);
        }
    }

    /**
     * Releases (deletes) a single Session, even if it is not expired yet.
     * @param sessionId ID of the Session to release
     */
    public void releaseSession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        Session<T> session = sessionsById.get(sessionId);
        if (session == null) {
            return;
        }

        closeSessionData(session);
        synchronized (sessionMapSynchronizationLock) {
            // rückgabewert kann ignoriert werden, wenn nichts gefunden wird ist das Ziel ("session aus der map nehmen") genauso erreicht.
            sessionsById.remove(session.sessionId);
            sessionsByLastRenewalTime.remove(session.lastRenewalTime, session);
        }
    }

    /**
     * Check if any session(s) exceed the {@link #sessionTimeoutMillis}.
     * <p> Closes and removes any Sessions that apply.
     */
    public void checkSessionsForTimeout(long currentTime) {
        // alle Einträge, die älter als dieser Zeitstempel sind (oder gleich sind), sind ausgelaufen.
        long latestExpiredTime = currentTime - sessionTimeoutMillis;

        synchronized (sessionMapSynchronizationLock) {
            List<Session<T>> sessionsToRenew = new ArrayList<>();
            NavigableMap<Long, Set<Session<T>>> expiredSessionBuckets = sessionsByLastRenewalTime.getUnderlyingMap().headMap(latestExpiredTime, true);

            for (Set<Session<T>> expiredBucket : expiredSessionBuckets.values()) {
                Iterator<Session<T>> expiredSessionIterator = expiredBucket.iterator();
                while (expiredSessionIterator.hasNext()) {
                    Session<T> expiredSession = expiredSessionIterator.next();
                    if (expiredSession.sessionData != null && expiredSession.sessionData.isStillUsed()) {
                        sessionsToRenew.add(expiredSession);
                        continue;
                    }

                    sessionsById.remove(expiredSession.sessionId);
                    closeSessionData(expiredSession);
                    expiredSessionIterator.remove();
                }
            }

            long renewalTime = System.currentTimeMillis();
            for (Session<T> session : sessionsToRenew) {
                renewSession(session, renewalTime);
            }
        }
    }

    public void releaseAllSessions() {
        synchronized (sessionMapSynchronizationLock) {
            for (Session<T> session : sessionsById.values()) {
                closeSessionData(session);
            }
            sessionsById.clear();
            sessionsByLastRenewalTime.getUnderlyingMap().clear();
        }
    }

    private void closeSessionData(Session<T> session) {
        logger.debug("closing sessionData {}", session.sessionId);
        if (session.sessionData != null) {
            try {
                session.sessionData.close();
            } catch (IOException e) {
                logger.error(
                        "Unable to close sessionData for sessionId '{}' and lastRenewTime '{}'",
                        session.sessionId, session.lastRenewalTime
                );
            }
        }
    }

}
