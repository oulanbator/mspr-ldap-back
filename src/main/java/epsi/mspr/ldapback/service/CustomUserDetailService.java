package epsi.mspr.ldapback.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import epsi.mspr.ldapback.model.entity.User;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO : implement these properties in User entity
        String password = "";
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        try {
            // Look for user in database
            User user = userService.getUserByUsername(username);
            if (user == null) {
//                throw new UsernameNotFoundException(
//                        "No user found with username: " + username);
                return null;
            }
            // If user found, return a UserDetails.User object
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    password,
                    user.isTwoFactorVerified(),
                    accountNonExpired,
                    credentialsNonExpired,
                    accountNonLocked,
                    new ArrayList<>());
                    //getAuthorities(user.getRole()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
