package emptyvessel.worklist.config;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import emptyvessel.worklist.model.User;
import emptyvessel.worklist.repository.UserRepository;

@Service
public class LocalUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public LocalUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 根据邮箱加载用户
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("DEBUG: Authentication attempt for email: " + email);
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    System.out.println("DEBUG: Authentication failed - User not found: " + email);
                    return null;
                });

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        System.out.println("DEBUG: Authentication success - Found user: " + user.getUsername() + " (Email: "
                + user.getEmail() + ")");
        String authority = user.getRole() != null ? user.getRole().name() : "ROLE_USER";
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
