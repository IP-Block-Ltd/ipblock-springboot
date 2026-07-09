package com.ipblock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that blocks clients rejected by ip-block.com.
 */
public class IpBlockFilter extends OncePerRequestFilter {

    private final IpBlockProperties props;
    private final IpBlockClient client;

    public IpBlockFilter(IpBlockProperties props, IpBlockClient client) {
        this.props = props;
        this.client = client;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!props.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        if (ip == null || ip.isEmpty() || isWhitelisted(ip)) {
            chain.doFilter(request, response);
            return;
        }

        String userAgent = orEmpty(request.getHeader("User-Agent"));
        String referrer = orEmpty(request.getHeader("Referer"));

        if (!client.isBlocked(ip, userAgent, referrer)) {
            chain.doFilter(request, response);
            return;
        }

        if ("redirect".equals(props.getBlockAction())) {
            response.sendRedirect(props.getRedirectUrl());
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(props.getBlockMessage());
    }

    private String clientIp(HttpServletRequest request) {
        if (props.isBehindProxy()) {
            String cf = request.getHeader("CF-Connecting-IP");
            if (cf != null && !cf.isBlank()) {
                return cf.trim();
            }
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isWhitelisted(String ip) {
        List<String> whitelist = props.getWhitelist();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        for (String entry : whitelist) {
            if (entry.contains("/")) {
                if (CidrUtil.matches(ip, entry)) {
                    return true;
                }
            } else if (entry.equals(ip)) {
                return true;
            }
        }
        return false;
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
