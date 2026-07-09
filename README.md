> ⚠️ **Status: untested.** This extension is provided as-is and has **not been tested in production**. Please feel free to fork, modify, improve, and open pull requests.
>
> Licensed under **GNU GPLv3** (see [LICENSE](LICENSE)).

# IP Block — Spring Boot Starter

An auto-configuration starter that registers a servlet `Filter` checking every
request against the [ip-block.com](https://www.ip-block.com) service.

**Targets:** Spring Boot 3.x (Jakarta EE / `jakarta.servlet`), Java 17+.

## Install

Add the dependency (Maven):

```xml
<dependency>
    <groupId>com.ipblock</groupId>
    <artifactId>ip-block-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

The filter is registered automatically — no `@Bean` or `@Import` needed.

## Configure

In `application.yml`:

```yaml
ip-block:
  enabled: true
  site-id: your-site-id
  api-key: your-api-key
  api-url: https://api.ip-block.com/v1/check   # default
  fail-open: true          # allow on error/timeout (default)
  cache-ttl: 300           # seconds (in-memory cache)
  timeout-ms: 1000         # 1 second
  behind-proxy: false      # trust X-Forwarded-For / CF-Connecting-IP
  block-action: "403"      # "403" or "redirect"
  redirect-url: https://www.ip-block.com/blocked.php
  block-message: "Access denied."
  whitelist:
    - 127.0.0.1
    - 10.0.0.0/8
```

Or `application.properties`:

```properties
ip-block.site-id=your-site-id
ip-block.api-key=your-api-key
ip-block.behind-proxy=true
```

## How it works

- Builds `{api_key, site_id, ip, user_agent, referrer}` and `POST`s it with the
  JDK `HttpClient` and a **1 second** (`timeout-ms`) request timeout.
- Blocks only when the response is `{"action":"block"}`.
- **Fails open** on any error/timeout/non-2xx/missing `action`
  (`fail-open: false` to fail closed).
- Caches each decision for `cache-ttl` seconds in a `ConcurrentHashMap`, keyed
  by `md5(ip|user_agent|referrer)`.
- Honours `whitelist` (individual IPs and CIDR ranges).
- Reads the real client IP; with `behind-proxy: true` it trusts
  `CF-Connecting-IP` then the first `X-Forwarded-For` hop.

Set `ip-block.enabled=false` to disable the starter entirely.
