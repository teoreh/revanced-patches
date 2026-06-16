package app.revanced.extension.redditisfun.oauth;

/**
 * Represents an OAuth token with expiry tracking.
 */
public class OAuthToken {
    private final String accessToken;
    private final long expiresAt; // epoch seconds

    public OAuthToken(String accessToken, long expiresInSeconds) {
        this.accessToken = accessToken;
        this.expiresAt = (System.currentTimeMillis() / 1000) + expiresInSeconds;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() / 1000) >= expiresAt;
    }
}
