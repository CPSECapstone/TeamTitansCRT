package base.security;

import base.user.User;
import base.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class BaseUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public BaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override // from UserDetailsService
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if(user == null) {
            throw new UsernameNotFoundException("Could not find user " + username);
        }
        return new BaseUserDetails(user);
    }

    public boolean checkUserIsVerified(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if(user == null) {
            throw new UsernameNotFoundException("Could not find user " + username);
        }
        return user.getIsVerified();
    }

    private final static class BaseUserDetails extends User implements UserDetails {

        private BaseUserDetails(User user) {
            super(user);
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // The GrantedAuthorities, aka Roles, should be stored along    with the user in the DB. This is for Demo purposes.
            return AuthorityUtils.createAuthorityList(this.getRole().toLowerCase().equals("admin") ? "ROLE_ADMIN" : "ROLE_USER");
        }

        @Override
        public String getUsername() {
            return getEmail();
        }

        /********************************************************************
              Lets not worry about these for now. Hard code them to true.
         *******************************************************************/
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        private static final long serialVersionUID = 5639683223516504866L;
    }
}
