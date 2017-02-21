package api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
public class DefaultAuthTokenService implements AuthTokenService {
    private final UserDetailsService userDetailsService;
    private final Integer expirationMinutes;

    private final ConcurrentMap<AuthToken, AuthTokenInfo> tokenUserMap = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public DefaultAuthTokenService(UserDetailsService userDetailsService,
                                   @Value("${auth.token.expirationMinutes}") Integer expirationMinutes) {
        this.userDetailsService = userDetailsService;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public AuthToken registerUserToken(String username) {
        removeCurrentToken(username);
        return registerNewToken(username);
    }

    private void removeCurrentToken(String username) {
        AuthTokenInfo usernameTokenInfo = AuthTokenInfo.fromUsername(username);
        tokenUserMap.values().remove(usernameTokenInfo);
    }

    private AuthToken registerNewToken(String username) {
        AuthToken token = new AuthToken(new BigInteger(130, secureRandom).toString(32));
        Instant expiredAt = Instant.now().plusSeconds(expirationMinutes * 60);
        tokenUserMap.put(token, new AuthTokenInfo(username, expiredAt));
        return token;
    }

    @Override
    public UserDetails getUserDetailsByToken(AuthToken authToken) {
        AuthTokenInfo tokenInfo = tokenUserMap.get(authToken);
        if (tokenInfo == null) {
            return null;
        }
        if (tokenInfo.isExpired()) {
            unregisterToken(authToken);
            return null;
        }
        return userDetailsService.loadUserByUsername(tokenInfo.username);
    }

    @Override
    public void unregisterToken(AuthToken authToken) {
        tokenUserMap.remove(authToken);
    }

    private static final class AuthTokenInfo {
        private final String username;
        private final Instant expiredAt;

        AuthTokenInfo(String username, Instant expiredAt) {
            Assert.notNull(username);
            Assert.notNull(expiredAt);
            this.username = username;
            this.expiredAt = expiredAt;
        }

        boolean isExpired() {
            return expiredAt.isBefore(Instant.now());
        }

        static AuthTokenInfo fromUsername(String username) {
            return new AuthTokenInfo(username, Instant.MIN);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final AuthTokenInfo that = (AuthTokenInfo) o;
            return Objects.equals(username, that.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username);
        }

    }

}
