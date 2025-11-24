package se.frisk.edufyrecommendationsservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import se.frisk.edufyrecommendationsservice.security.UserServiceAuthenticationProvider;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserServiceAuthenticationProvider userServiceAuthenticationProvider;

    public SecurityConfig(UserServiceAuthenticationProvider userServiceAuthenticationProvider) {
        this.userServiceAuthenticationProvider = userServiceAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/recommendations/test").permitAll()
                        .requestMatchers("/api/recommendations/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(basic -> {});
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(userServiceAuthenticationProvider)
                .build();
    }
}
