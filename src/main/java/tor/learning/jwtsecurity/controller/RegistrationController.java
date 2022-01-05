package tor.learning.jwtsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.entity.VerificationToken;
import tor.learning.jwtsecurity.model.http.MessageResponse;
import tor.learning.jwtsecurity.model.http.RegistrationRequest;
import tor.learning.jwtsecurity.model.http.SingleStringRequest;
import tor.learning.jwtsecurity.model.http.StandardApiResponse;
import tor.learning.jwtsecurity.service.UserService;
import tor.learning.jwtsecurity.process.registration.OnRegistrationCompleteEvent;
import tor.learning.jwtsecurity.util.exception.UserAlreadyExistException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import static tor.learning.jwtsecurity.util.AppConstants.*;

@RestController
@CrossOrigin
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest,
                                      HttpServletRequest request) {
        // TODO : backcheck form fields ??
        try {
            // Register new user
            User newUser = this.userService.registerNewUserAccount(registrationRequest);
            // Send email event
            // TODO : simplify OnRegistrationCompleteEvent (Locale and context path needed ?)
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(
                    newUser,
                    request.getLocale(),
                    request.getContextPath()));
            // Return response to front-end
            StandardApiResponse response = new StandardApiResponse(STATUS_SUCCESS, MSG_USER_CREATED);
            if (newUser.isTwoFactorEnabled()) {
                String barCode = this.userService.get2FactorsBarCode(newUser.getUsername());
                response.setData(new ArrayList<>(Collections.singleton(barCode)));
            }
            return ResponseEntity.ok(response);

        } catch (UserAlreadyExistException e) {
            e.printStackTrace();
            StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_SERVER_ERROR_IS + e.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_SERVER_ERROR_UNKNOWN);
            return ResponseEntity.ok(response);
        }
    }

//TODO : implement 2FA after login

//    @PostMapping("/generate-two-factor")
//    public ResponseEntity<?> generate2FA(@RequestBody RegistrationRequest registrationRequest) {
//        String barCode = this.userService.get2FactorsBarCode(registrationRequest.getUsername());
//    }

    @GetMapping("/confirm-registration")
    public ResponseEntity<?> confirmRegistration
            (WebRequest request, @RequestParam("token") String token) {

        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_ERROR_INVALID_TOKEN);
            return ResponseEntity.ok(response);
        }
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_ERROR_EXPIRED_TOKEN);
            return ResponseEntity.ok(response);
        }
        // Token valid : activate account
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userService.saveUser(user);
        StandardApiResponse response = new StandardApiResponse(STATUS_SUCCESS, MSG_ACCOUNT_VERIFIED);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email-available")
    public ResponseEntity<?> emailAvailable(@RequestBody SingleStringRequest stringRequest) {
        User userExists = this.userService.getUserByEmail(stringRequest.getData());
        StandardApiResponse response;
        if (userExists == null) {
            response = new StandardApiResponse(STATUS_SUCCESS);
        } else {
            response = new StandardApiResponse(STATUS_FAIL, MSG_EMAIL_TAKEN);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/username-available")
    public ResponseEntity<?> usernameAvailable(@RequestBody SingleStringRequest stringRequest) {
        User userExists = this.userService.getUserByUsername(stringRequest.getData());
        StandardApiResponse response;
        if (userExists == null) {
            response = new StandardApiResponse(STATUS_SUCCESS);
        } else {
            response = new StandardApiResponse(STATUS_FAIL, MSG_USERNAME_TAKEN);
        }
        return ResponseEntity.ok(response);
    }


}
