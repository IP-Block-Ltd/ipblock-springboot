package com.ipblock;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration bound from the {@code ip-block.*} properties.
 */
@ConfigurationProperties(prefix = "ip-block")
public class IpBlockProperties {

    /** Master on/off switch. */
    private boolean enabled = true;

    /** ip-block.com site identifier. */
    private String siteId = "";

    /** ip-block.com API key (sent in the request body). */
    private String apiKey = "";

    /** API endpoint. */
    private String apiUrl = "https://api.ip-block.com/v1/check";

    /** Allow the request when the check errors/times out. */
    private boolean failOpen = true;

    /** Decision cache lifetime, in seconds. */
    private int cacheTtl = 300;

    /** HTTP timeout, in milliseconds. */
    private int timeoutMs = 1000;

    /** Trust X-Forwarded-For / CF-Connecting-IP for the real client IP. */
    private boolean behindProxy = false;

    /** "403" or "redirect". */
    private String blockAction = "403";

    /** Redirect target when blockAction is "redirect". */
    private String redirectUrl = "https://www.ip-block.com/blocked.php";

    /** Body returned on a 403 block. */
    private String blockMessage = "Access denied.";

    /** IPs and CIDR ranges that always bypass the check. */
    private List<String> whitelist = new ArrayList<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getSiteId() { return siteId; }
    public void setSiteId(String siteId) { this.siteId = siteId; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public boolean isFailOpen() { return failOpen; }
    public void setFailOpen(boolean failOpen) { this.failOpen = failOpen; }

    public int getCacheTtl() { return cacheTtl; }
    public void setCacheTtl(int cacheTtl) { this.cacheTtl = cacheTtl; }

    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }

    public boolean isBehindProxy() { return behindProxy; }
    public void setBehindProxy(boolean behindProxy) { this.behindProxy = behindProxy; }

    public String getBlockAction() { return blockAction; }
    public void setBlockAction(String blockAction) { this.blockAction = blockAction; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }

    public String getBlockMessage() { return blockMessage; }
    public void setBlockMessage(String blockMessage) { this.blockMessage = blockMessage; }

    public List<String> getWhitelist() { return whitelist; }
    public void setWhitelist(List<String> whitelist) { this.whitelist = whitelist; }
}
