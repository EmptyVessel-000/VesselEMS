package vesselems.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import vesselems.model.User;
import vesselems.repository.RoleRepository;
import vesselems.repository.UserRepository;
import vesselems.repository.UserRoleRepository;

@Service
public class LocalUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final RoleRepository roleRepository;

        public LocalUserDetailsService(UserRepository userRepository,
                        UserRoleRepository userRoleRepository,
                        RoleRepository roleRepository) {
                this.userRepository = userRepository;
                this.userRoleRepository = userRoleRepository;
                this.roleRepository = roleRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

                List<SimpleGrantedAuthority> authorities = userRoleRepository.findByUserId(user.getId()).stream()
                                .map(ur -> roleRepository.findById(ur.getRoleId()).orElse(null))
                                .filter(r -> r != null)
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleName().toUpperCase()))
                                .collect(Collectors.toList());

                return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                                .password(user.getPassword())
                                .authorities(authorities)
                                .accountExpired(false)
                                .accountLocked(false)
                                .credentialsExpired(false)
                                .disabled(false)
                                .build();
        }
}
