package api.security;

import api.security.handler.RestAuthenticationFailureHandler;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService restUserDetailsService;

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private PasswordEncoder restPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(restAuthenticationFilter(), LogoutFilter.class)
                .formLogin()
                .loginProcessingUrl("/login")
                .failureHandler(authenticationFailureHandler())
                .successHandler(authenticationSuccessHandler)

                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler)
                .logoutSuccessUrl("/logout")

                .and()
                .exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint())

                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .enableSessionUrlRewriting(false)

                .and()
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .authenticationProvider(restAuthenticationProvider());
    }

    private Filter restAuthenticationFilter() {
        return new RestAuthenticationFilter(authTokenService);
    }

    private AuthenticationFailureHandler authenticationFailureHandler() {
        return new RestAuthenticationFailureHandler();
    }

    private AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    private AuthenticationProvider restAuthenticationProvider() {
        RestAuthenticationProvider authenticationProvider = new RestAuthenticationProvider();
        authenticationProvider.setUserDetailsService(restUserDetailsService);
        authenticationProvider.setPasswordEncoder(restPasswordEncoder);
        return authenticationProvider;
    }

    @Bean(name = "sha256Encryptor")
    public StrongPasswordEncryptor sha256Encryptor() {
        return new StrongPasswordEncryptor();
    }
}
