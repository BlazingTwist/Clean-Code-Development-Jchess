package jchess.server.session;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class SessionMgrController {

    private static final Logger logger = LoggerFactory.getLogger(SessionMgrController.class);
    private static final long defaultTimeoutMillis = TimeUnit.MINUTES.toMillis(5);
    private static final Map<Class<?>, SessionManager<?>> sessionManagers = new HashMap<>();

    private static ScheduledExecutorService activeHeartbeatExecutor = null;

    public static void startHeartbeat(long period, TimeUnit timeUnit) {
        if (activeHeartbeatExecutor != null) {
            logger.warn("Heartbeat is already started, cannot start again.");
            return;
        }

        activeHeartbeatExecutor = Executors.newScheduledThreadPool(1, new DaemonThreadFactory());
        activeHeartbeatExecutor.scheduleAtFixedRate(SessionMgrController::heartbeat, 0, period, timeUnit);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (activeHeartbeatExecutor != null) {
                activeHeartbeatExecutor.shutdown();
                activeHeartbeatExecutor = null;
            }
        }));
    }

    public static <T extends ISessionData> void registerSessionManager(
            Class<? extends T> dataClass, long timeout, TimeUnit timeoutUnit
    ) {
        long timeoutMillis = timeoutUnit.toMillis(timeout);
        SessionManager<T> sessionManager = new SessionManager<>(timeoutMillis);
        registerSessionManager(dataClass, sessionManager);
    }

    public static <T extends ISessionData> void registerSessionManager(Class<? extends T> dataClass, SessionManager<T> sessionManager) {
        if (sessionManagers.containsKey(dataClass)) {
            logger.error("sessionManager for dataClass '" + dataClass + "' is already registered!");
            return;
        }

        sessionManagers.put(dataClass, sessionManager);
        Runtime.getRuntime().addShutdownHook(new Thread(sessionManager::releaseAllSessions));
    }

    @SuppressWarnings("unchecked")
    public static <T extends ISessionData> SessionManager<T> lookupSessionManager(Class<? extends T> dataClass) {
        SessionManager<?> sessionManager = sessionManagers.get(dataClass);
        if (sessionManager == null) {
            logger.error("No SessionManager registered for dataClass '" + dataClass + "'");
            registerSessionManager(dataClass, defaultTimeoutMillis, TimeUnit.MILLISECONDS);
            sessionManager = sessionManagers.get(dataClass);
        }

        return (SessionManager<T>) sessionManager;
    }

    private static void heartbeat() {
        try {
            long currentTime = System.currentTimeMillis();
            for (SessionManager<?> sessionManager : sessionManagers.values()) {
                sessionManager.checkSessionsForTimeout(currentTime);
            }
        } catch (Exception e) {
            logger.error("heartbeat caught exception", e);
        }
    }

    /**
     * Ensures that the Heartbeat does not interfere with the normal JVM exit behaviour.
     */
    private static class DaemonThreadFactory implements ThreadFactory {

        private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setDaemon(true);
            return thread;
        }
    }
}
