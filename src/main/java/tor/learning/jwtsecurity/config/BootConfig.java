package tor.learning.jwtsecurity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.service.UserService;

@Configuration
public class BootConfig {

   private UserService userService;

   @Autowired
   public BootConfig(UserService userService) {
       this.userService = userService;
   }

   @Bean
   public void createUsers() {
       User user = new User();
       user.setUsername("admin");
       user.setPassword("test123");
       user.setEmail("admin@test.com");
       this.userService.saveUser(user);
   }
}
