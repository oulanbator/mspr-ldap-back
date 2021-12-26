package tor.learning.jwtsecurity.controller;

import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.http.MessageResponse;
import tor.learning.jwtsecurity.model.http.RegistrationRequest;
import tor.learning.jwtsecurity.model.http.SingleStringRequest;
import tor.learning.jwtsecurity.service.UserService;

import java.security.SecureRandom;

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
        user.setTwoFactorSecret(generateSecretKey());
        try {
            this.userService.saveUser(user);
            return ResponseEntity.ok(new MessageResponse("User created successfully !", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new MessageResponse("Server error : cannot create user", false));
        }
    }

    @PostMapping("/email-available")
    public ResponseEntity<?> emailAvailable(@RequestBody SingleStringRequest stringRequest) {
        if (this.userService.getUserByEmail(stringRequest.getData()) == null) {
            return ResponseEntity.ok(new MessageResponse("Email is available !", true));
        } else {
            return ResponseEntity.ok(new MessageResponse("Email is already taken !", false));
        }
    }

    @PostMapping("/username-available")
    public ResponseEntity<?> usernameAvailable(@RequestBody SingleStringRequest stringRequest) {
        if (this.userService.getUserByUsername(stringRequest.getData()) == null) {
            return ResponseEntity.ok(new MessageResponse("Username is available !", true));
        } else {
            return ResponseEntity.ok(new MessageResponse("Username is already taken !", false));
        }
    }

    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }
}
