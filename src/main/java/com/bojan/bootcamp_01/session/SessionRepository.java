package com.bojan.bootcamp_01.session;

import java.util.List;

public interface SessionRepository {
    void createSession(UserSession session);

    UserSession getSession(Long userId, String deviceId);

    void updateActivity(Long userId, String deviceId);

    List<UserSession> getAllUserSessions(Long userId);

    void deleteSession(Long userId, String deviceId);

    void deleteAllUserSessions(Long userId);

    long getActiveSessionCount();
}
