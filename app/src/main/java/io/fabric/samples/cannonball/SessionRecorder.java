package io.fabric.samples.cannonball;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.core.Session;

public class SessionRecorder {
    public static Session recordInitialSessionState(Session twitterSession,
                                                    Session digitsSession) {
        if (twitterSession != null) {
            recordSessionActive("Splash: user with active Twitter session", twitterSession);
            return twitterSession;
        } else if (digitsSession != null) {
            recordSessionActive("Splash: user with active Digits session", digitsSession);
            return digitsSession;
        } else {
            recordSessionInactive("Splash: anonymous user");
            return null;
        }
    }

    public static void recordSessionActive(String message, Session session) {
        recordSessionActive(message, String.valueOf(session.getId()));
    }

    public static void recordSessionInactive(String message) {
        recordSessionState(message, null, false);
    }

    private static void recordSessionActive(String message, String userIdentifier) {
        recordSessionState(message, userIdentifier, true);
    }

    private static void recordSessionState(String message,
                                           String userIdentifier,
                                           boolean active) {
        Crashlytics.log(message);
        Crashlytics.setUserIdentifier(userIdentifier);
        Crashlytics.setBool(App.CRASHLYTICS_KEY_SESSION_ACTIVATED, active);
    }
}
