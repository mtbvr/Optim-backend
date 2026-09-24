package com.opt.backend.common.security;

import com.opt.backend.common.config.SecurityProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    private final SecurityProperties securityProperties;
    private final JwtService jwtService;

    public AuthCookieService(SecurityProperties securityProperties, JwtService jwtService) {
        this.securityProperties = securityProperties;
        this.jwtService = jwtService;
    }

    public ResponseCookie buildAuthCookie(String token) {
        SecurityProperties.Cookie cookieProps = securityProperties.getCookie();
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName(), token)
                .httpOnly(true)
                .secure(cookieProps.isSecure())
                .sameSite(cookieProps.getSameSite())
                .path("/")
                .maxAge(jwtService.getExpirationSeconds());

        if (cookieProps.getDomain() != null && !cookieProps.getDomain().isBlank()) {
            builder.domain(cookieProps.getDomain());
        }
        return builder.build();
    }

    public ResponseCookie buildExpiredCookie() {
        SecurityProperties.Cookie cookieProps = securityProperties.getCookie();
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName(), "")
                .httpOnly(true)
                .secure(cookieProps.isSecure())
                .sameSite(cookieProps.getSameSite())
                .path("/")
                .maxAge(0);

        if (cookieProps.getDomain() != null && !cookieProps.getDomain().isBlank()) {
            builder.domain(cookieProps.getDomain());
        }
        return builder.build();
    }

    public String cookieName() {
        return securityProperties.getJwt().getCookieName();
    }
}
