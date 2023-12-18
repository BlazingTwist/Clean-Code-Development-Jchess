package jchess.server.session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class SessionManagerTest {

    private static ISessionData createMock() {
        ISessionData mock = Mockito.mock(ISessionData.class);
        Mockito.when(mock.isStillUsed()).thenReturn(false);
        return mock;
    }

    @Test
    public void test_sessionsClosedAfterTimeout() throws IOException {
        ISessionData closeable1 = createMock();
        ISessionData closeable2 = createMock();
        ISessionData closeable3 = createMock();

        SessionManager<ISessionData> sessionManager = new SessionManager<>(10);
        SessionManager.Session<ISessionData> session1 = sessionManager.createSession(closeable1);
        SessionManager.Session<ISessionData> session2 = sessionManager.createSession(closeable2);
        SessionManager.Session<ISessionData> session3 = sessionManager.createSession(closeable3);

        sessionManager.renewSession(session1, 9);
        sessionManager.renewSession(session2, 10);
        sessionManager.renewSession(session3, 11);
        sessionManager.checkSessionsForTimeout(20);

        // Schauen, dass die ressourcen geschlossen wurden
        Mockito.verify(closeable1, Mockito.times(1)).close();
        Mockito.verify(closeable2, Mockito.times(1)).close();
        Mockito.verify(closeable3, Mockito.times(0)).close();

        // Schauen, dass die geschlossenen Sessions nicht geholt werden können
        Assertions.assertNull(sessionManager.getSession(session1.sessionId), "get closed session should return null");
        Assertions.assertNull(sessionManager.getSession(session2.sessionId), "get closed session should return null");
        Assertions.assertNotNull(sessionManager.getSession(session3.sessionId), "get active session should return non-null");
    }

    @Test
    public void test_getUnknownSessionReturnsNull() {
        SessionManager<ISessionData> sessionManager = new SessionManager<>(10);
        SessionManager.Session<ISessionData> session = sessionManager.getSession("unknown");
        Assertions.assertNull(session);
    }

}
