package xyz.funtimes909.serverseekerv2.types;

public enum LoginAttempt {
    UNKNOWN(null, null),

    INSECURE(false, false),
    SECURE(true, true),

    ONLY_WHITELIST(false, true),
    ONLY_ONLINE(true, false),

    OFFLINE(false, null),
    ONLINE(true, null);

    public final Boolean online;
    public final Boolean whitelist;

    LoginAttempt(Boolean online, Boolean whitelist) {
        this.online = online;
        this.whitelist = whitelist;
    }
}
