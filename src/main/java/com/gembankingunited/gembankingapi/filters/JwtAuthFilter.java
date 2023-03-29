package com.gembankingunited.gembankingapi.filters;

import com.gembankingunited.gembankingapi.configs.JwtUtils;
import com.gembankingunited.gembankingapi.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserService userDetailsService;
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        List<String> allowedUris = List.of("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/logout", "/api/v1/auth/refresh-token");

        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.isTokenValid(jwt)) {
                String username = jwtUtils.extractUsername(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            if (allowedUris.contains(request.getRequestURI()) || (jwt != null && jwtUtils.isTokenValid(jwt))) {
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}