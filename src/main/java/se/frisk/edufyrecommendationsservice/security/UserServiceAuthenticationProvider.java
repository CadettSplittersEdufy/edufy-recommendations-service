package se.frisk.edufyrecommendationsservice.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import se.frisk.edufyrecommendationsservice.clients.UserClient;
import se.frisk.edufyrecommendationsservice.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserServiceAuthenticationProvider implements AuthenticationProvider {

    private final UserClient userClient;

    public UserServiceAuthenticationProvider(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        Long userId;
        try {
            userId = Long.parseLong(name);
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Invalid user id");
        }
        UserDTO user = userClient.getUserById(userId);
        if (user == null || !user.isActive()) {
            throw new BadCredentialsException("User not found or inactive");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        String role = (user.getRole() != null) ? user.getRole() : "edufy_USER";

        if ("edufy_ADMIN".equalsIgnoreCase(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new UsernamePasswordAuthenticationToken(user.getId().toString(), null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
