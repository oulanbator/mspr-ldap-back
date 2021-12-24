package tor.learning.jwtsecurity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.service.UserService;

@Configuration
public class BootConfig {

    private UserService userService;

    @Autowired
    public BootConfig(UserService userService) {
        this.userService = userService;
        this.createUsers();
    }

    private void createUsers() {
        User user = new User();
        user.setUsername("tor");
        user.setPassword("tue");
        this.userService.saveUser(user);
    }
}
