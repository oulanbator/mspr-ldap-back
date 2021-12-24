package tor.learning.jwtsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MyUserDetailService implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Supposed to return the user based on arguments
        String db_username = this.userService.getUserByUsername(username).getUsername();
        String db_password = this.userService.getUserByUsername(username).getPassword();

        return new User("foo", "foo", new ArrayList<>());
    }
}
