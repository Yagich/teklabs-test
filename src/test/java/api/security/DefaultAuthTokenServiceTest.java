package api.security;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultAuthTokenServiceTest {
    private final String testUsername = "testUser";
    private DefaultAuthTokenService authTokenService;
    private UserDetails mockedUserDetails;

    @Before
    public void init() {
        mockedUserDetails = mock(UserDetails.class);
        authTokenService = new DefaultAuthTokenService(mockUserDetailsService(), 10);
    }

    private UserDetailsService mockUserDetailsService() {
        UserDetailsService mockedUserDetailsService = mock(UserDetailsService.class);
        when(mockedUserDetailsService.loadUserByUsername(testUsername)).thenReturn(mockedUserDetails);
        return mockedUserDetailsService;
    }

    @Test
    public void registerUserToken() throws Exception {
        AuthToken authToken = authTokenService.registerUserToken(testUsername);

        assertEquals(mockedUserDetails, authTokenService.getUserDetailsByToken(authToken));
    }

    @Test
    public void unregisterToken() throws Exception {
        AuthToken authToken = authTokenService.registerUserToken(testUsername);
        authTokenService.unregisterToken(authToken);

        assertNull(authTokenService.getUserDetailsByToken(authToken));
    }

    @Test
    public void lastRegisteredTokenIsValid() {
        AuthToken authToken1 = authTokenService.registerUserToken(testUsername);
        AuthToken authToken2 = authTokenService.registerUserToken(testUsername);

        assertNull(authTokenService.getUserDetailsByToken(authToken1));
        assertEquals(mockedUserDetails, authTokenService.getUserDetailsByToken(authToken2));
    }

}