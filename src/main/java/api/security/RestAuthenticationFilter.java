package api.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

class RestAuthenticationFilter extends GenericFilterBean {

    private final AuthTokenService authTokenService;

    RestAuthenticationFilter(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        initSecurityContextByAuthToken((HttpServletRequest) request);
        filterChain.doFilter(request, response);
    }

    private void initSecurityContextByAuthToken(HttpServletRequest request) {
        UserDetails userDetails = getUserDetailsByToken(request);
        SecurityContext contextBeforeChainExecution = createSecurityContext(userDetails);
        SecurityContextHolder.setContext(contextBeforeChainExecution);
    }

    private SecurityContext createSecurityContext(UserDetails userDetails) {
        if (userDetails == null) {
            return SecurityContextHolder.createEmptyContext();
        }
        SecurityContextImpl securityContext = new SecurityContextImpl();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
        securityContext.setAuthentication(authentication);
        return securityContext;
    }

    private UserDetails getUserDetailsByToken(HttpServletRequest request) {
        AuthToken authToken = AuthTokenExtractor.extractFromRequest(request);
        return authTokenService.getUserDetailsByToken(authToken);
    }

}
