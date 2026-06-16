package app.revanced.extension.redditisfun.oauth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import okhttp3.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages OAuth token lifecycle for logged-out Reddit API access.
 * Fetches, caches, and auto-refreshes Bearer tokens using the installed_client grant.
 */
public class OAuthTokenManager {
    private static final String TAG = "OAuthTokenManager";
    private static final String OAUTH_TOKEN_ENDPOINT = "https://www.reddit.com/api/v1/access_token";
    private static final String DEVICE_ID = "DO_NOT_TRACK_THIS_DEVICE";
    private static final String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private static final String PREFS_NAME = "oauth_tokens";
    private static final String TOKEN_KEY_PREFIX = "token_";
    private static final String EXPIRES_KEY_PREFIX = "expires_";
    public static final String PASSWORD = "";

    private static OAuthToken CachedToken = new OAuthToken("", 0);

    public static String getCustomClientId(){
        return "PLACEHOLDER_STRING_FOR_CLIENT_ID";
    }

    public static void attachToken(Object requestBuilder) {
        if(requestBuilder == null) {
            Log.e(TAG, "Request builder is null");
            return;
        }
        try
        {
            Log.i(TAG, "USER = " + getCustomClientId());
            Method headerMethod = null;
            for (Method method : requestBuilder.getClass().getDeclaredMethods())
            {
                Class<?>[] params = method.getParameterTypes();

                // Look for a method that takes (String, String) and returns the Builder type
                if (params.length == 2 &&
                    params[0] == String.class &&
                    params[1] == String.class &&
                    method.getReturnType() == requestBuilder.getClass()) {
                        headerMethod = method;
                        break;
                }
            }

            if (headerMethod != null) {
                headerMethod.setAccessible(true);
                String token = getToken();
                headerMethod.invoke(requestBuilder, "Authorization", "Bearer " + token);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Failed to attach token", e);
        }
    }

    /**
     * Get or fetch a Bearer token for the given User-Agent.
     * Returns null if token cannot be obtained (fails silently).
     */
    private static String getToken() {
        synchronized (CachedToken) {
            // Check in-memory cache first
            Log.i(TAG, "Checking in-memory cache for token");
            if (CachedToken != null && !CachedToken.isExpired()) {
                Log.i(TAG, "Using cached token");
                return CachedToken.getAccessToken();
            }

            Log.i(TAG, "Fetching new token");
            // Fetch new token
            try {
                OAuthToken newToken = fetchNewToken();
                if (newToken != null) {
                    Log.i(TAG, "New token fetched successfully, saving to cache");
                    CachedToken = newToken;
                    return newToken.getAccessToken();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch OAuth token", e);
            }

            return null;
        }
    }

    /**
     * Fetch a new token from Reddit's OAuth endpoint.
     */
    private static OAuthToken fetchNewToken() throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(OAUTH_TOKEN_ENDPOINT).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String auth = getCustomClientId() + ":" + PASSWORD;
        byte[] encodedAuth = android.util.Base64.encode(auth.getBytes(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP);
        String authHeaderValue = "Basic " + new String(encodedAuth);
        connection.setRequestProperty("Authorization", authHeaderValue);
        connection.setDoOutput(true);

        String body = "grant_type=" + URLEncode(GRANT_TYPE) + "&device_id=" + URLEncode(DEVICE_ID);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
            throw new IOException("OAuth token endpoint returned " + responseCode);
        }

        JSONObject responseJson = parseJSONResponse(connection);
        String accessToken = responseJson.getString("access_token");
        int expiresIn = responseJson.getInt("expires_in");

        return new OAuthToken(accessToken, expiresIn);
    }

    /**
     * Parse JSON response from HTTP connection.
     */
    private static JSONObject parseJSONResponse(HttpURLConnection connection) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return new JSONObject(sb.toString());
    }

    /**
     * URL encode a string for form data.
     */
    private static String URLEncode(String str) {
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "URL encoding failed", e);
            return str;
        }
    }
}
