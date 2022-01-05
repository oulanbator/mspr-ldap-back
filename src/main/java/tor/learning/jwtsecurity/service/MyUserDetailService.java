package tor.learning.jwtsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tor.learning.jwtsecurity.model.entity.User;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class MyUserDetailService implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO : implement these properties in User entity
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        try {
            // Look for user in database
            User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException(
                        "No user found with username: " + username);
            }


            // If user found, return a UserDetails.User
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.isEnabled(),
                    accountNonExpired,
                    credentialsNonExpired,
                    accountNonLocked,
                    new ArrayList<>());
                    //getAuthorities(user.getRole()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Supposed to return the user based on arguments
//        String db_username = this.userService.getUserByUsername(username).getUsername();
//        String db_password = this.userService.getUserByUsername(username).getPassword();
//
//        return new User(db_username, db_password, new ArrayList<>());
    }
}
