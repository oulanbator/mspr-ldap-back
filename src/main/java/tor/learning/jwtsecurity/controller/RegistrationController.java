package tor.learning.jwtsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.http.MessageResponse;
import tor.learning.jwtsecurity.model.http.RegistrationRequest;
import tor.learning.jwtsecurity.service.UserService;

@RestController
@CrossOrigin
public class RegistrationController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest) {
        // TODO : backcheck form fields
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(registrationRequest.getPassword());
        this.userService.saveUser(user);
        return ResponseEntity.ok(new MessageResponse("User created successfully !"));
    }
}
