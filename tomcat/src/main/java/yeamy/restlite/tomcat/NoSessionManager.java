package yeamy.restlite.tomcat;

import org.apache.catalina.*;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleBase;
import org.apache.catalina.util.LifecycleMBeanBase;

import java.beans.PropertyChangeListener;
import java.io.IOException;

public final class NoSessionManager implements Manager {
    //    import org.apache.catalina.session.PersistentManagerBase;

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void setContext(Context context) {
    }

    @Override
    public SessionIdGenerator getSessionIdGenerator() {
        return null;
    }

    @Override
    public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
    }

    @Override
    public long getSessionCounter() {
        return 0L;
    }

    @Override
    public int getMaxActive() {
        return 0;
    }

    @Override
    public void setMaxActive(int maxActive) {
    }

    @Override
    public int getActiveSessions() {
        return 0;
    }

    @Override
    public long getExpiredSessions() {
        return 0;
    }

    @Override
    public void setExpiredSessions(long expiredSessions) {
    }

    @Override
    public int getRejectedSessions() {
        return 0;
    }

    @Override
    public int getSessionMaxAliveTime() {
        return 0;
    }

    @Override
    public void setSessionMaxAliveTime(int sessionMaxAliveTime) {

    }

    @Override
    public int getSessionAverageAliveTime() {
        return 0;
    }

    @Override
    public int getSessionCreateRate() {
        return 0;
    }

    @Override
    public int getSessionExpireRate() {
        return 0;
    }

    @Override
    public void add(Session session) {

    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void changeSessionId(Session session, String newId) {

    }

    @Override
    public Session createEmptySession() {
        return null;
    }

    @Override
    public Session createSession(String sessionId) {
        return null;
    }

    @Override
    public Session findSession(String id) throws IOException {
        return null;
    }

    private static final Session[] sessions = new Session[0];

    @Override
    public Session[] findSessions() {
        return sessions;
    }

    @Override
    public void load() throws ClassNotFoundException, IOException {
    }

    @Override
    public void remove(Session session) {
    }

    @Override
    public void remove(Session session, boolean update) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void unload() throws IOException {
    }

    @Override
    public void backgroundProcess() {
    }

    @Override
    public boolean willAttributeDistribute(String name, Object value) {
        return false;
    }

    @Override
    public void setNotifyBindingListenerOnUnchangedValue(boolean notifyBindingListenerOnUnchangedValue) {
    }

    @Override
    public void setNotifyAttributeListenerOnUnchangedValue(boolean notifyAttributeListenerOnUnchangedValue) {
    }

    @Override
    public void setSessionActivityCheck(boolean sessionActivityCheck) {
    }

    @Override
    public void setSessionLastAccessAtStart(boolean sessionLastAccessAtStart) {
    }
}

