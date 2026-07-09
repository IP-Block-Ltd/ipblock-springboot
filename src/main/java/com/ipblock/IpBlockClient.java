package com.ipblock;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queries ip-block.com and caches decisions per client fingerprint.
 */
public class IpBlockClient {

    private static final Logger log = LoggerFactory.getLogger(IpBlockClient.class);

    private final IpBlockProperties props;
    private final HttpClient http;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public IpBlockClient(IpBlockProperties props) {
        this.props = props;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .build();
    }

    /** Returns true when the client should be blocked. */
    public boolean isBlocked(String ip, String userAgent, String referrer) {
        String key = fingerprint(ip, userAgent, referrer);

        CacheEntry entry = cache.get(key);
        long now = System.currentTimeMillis();
        if (entry != null && entry.expiresAt > now) {
            return entry.blocked;
        }

        boolean blocked = query(ip, userAgent, referrer);
        cache.put(key, new CacheEntry(blocked, now + props.getCacheTtl() * 1000L));
        return blocked;
    }

    private boolean query(String ip, String userAgent, String referrer) {
        try {
            String body = "{"
                    + "\"api_key\":" + jsonString(props.getApiKey()) + ","
                    + "\"site_id\":" + jsonString(props.getSiteId()) + ","
                    + "\"ip\":" + jsonString(ip) + ","
                    + "\"user_agent\":" + jsonString(userAgent) + ","
                    + "\"referrer\":" + jsonString(referrer)
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(props.getApiUrl()))
                    .timeout(Duration.ofMillis(props.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                return fail();
            }

            String action = extractAction(response.body());
            if (action == null) {
                return fail();
            }
            return action.equals("block");
        } catch (Exception e) {
            log.warn("ip-block check failed: {}", e.getMessage());
            return fail();
        }
    }

    // fail open => allow => not blocked
    private boolean fail() {
        return !props.isFailOpen();
    }

    /** Minimal, dependency-free extraction of the "action" string value. */
    private static String extractAction(String json) {
        if (json == null) {
            return null;
        }
        int idx = json.indexOf("\"action\"");
        if (idx < 0) {
            return null;
        }
        int colon = json.indexOf(':', idx);
        if (colon < 0) {
            return null;
        }
        int firstQuote = json.indexOf('"', colon);
        if (firstQuote < 0) {
            return null;
        }
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }
        return json.substring(firstQuote + 1, secondQuote);
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append("\"").toString();
    }

    private static String fingerprint(String ip, String userAgent, String referrer) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((ip + "|" + userAgent + "|" + referrer).getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest));
        } catch (Exception e) {
            return ip + "|" + userAgent + "|" + referrer;
        }
    }

    private static final class CacheEntry {
        final boolean blocked;
        final long expiresAt;

        CacheEntry(boolean blocked, long expiresAt) {
            this.blocked = blocked;
            this.expiresAt = expiresAt;
        }
    }
}
