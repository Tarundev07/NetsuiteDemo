package com.atomicnorth.hrm.configuration.security.jwt;

import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
public class JWTFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenProvider tokenProvider;

    public JWTFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /*@Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);
        if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt)) {
            UserLoginDetail userLoginDetail = this.tokenProvider.parse(jwt);
            Authentication authentication = this.tokenProvider.getAuthentication(userLoginDetail, jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            SessionHolder.setUserLoginDetail(userLoginDetail);
        }
        filterChain.doFilter(servletRequest, servletResponse);
        SessionHolder.clear();
    }
*/
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String path = httpServletRequest.getRequestURI();
        if (path.endsWith("/api/verify-otp")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String jwt = resolveToken(httpServletRequest);
        if (StringUtils.hasText(jwt)) {
            String extracted = this.tokenProvider.extractTenantId(jwt);
            if (StringUtils.hasText(extracted)) {
                TenantContextHolder.setTenantId(extracted);
            }
        }

        if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt)) {
            UserLoginDetail userLoginDetail = null;
            try {
                userLoginDetail = this.tokenProvider.parse(jwt);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Authentication authentication = this.tokenProvider.getAuthentication(userLoginDetail, jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            SessionHolder.setUserLoginDetail(userLoginDetail);
        }
        filterChain.doFilter(servletRequest, servletResponse);
        SessionHolder.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
