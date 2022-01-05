package tor.learning.jwtsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.http.*;
import tor.learning.jwtsecurity.service.MyUserDetailService;
import tor.learning.jwtsecurity.service.UserService;
import tor.learning.jwtsecurity.util.JwtUtil;

import static tor.learning.jwtsecurity.util.AppConstants.*;

@RestController
@CrossOrigin
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private MyUserDetailService userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

        } catch (DisabledException e) {
            StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_ACCOUNT_NOT_ACTIVATED);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_BAD_CREDENTIALS);
            return ResponseEntity.ok(response);
        }
        // Check 2FA enabled
        final User user = this.userService.getUserByUsername(authenticationRequest.getUsername());
        if (user != null && user.isTwoFactorEnabled()) {
            if (!this.userService.totpVerified(authenticationRequest)) {
                StandardApiResponse response = new StandardApiResponse(STATUS_FAIL, MSG_TWO_FACTORS_ACCOUNT);
                return ResponseEntity.ok(response);
            }
        }
        // If auth succeed, build response with JWT
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        StandardApiResponse response = new StandardApiResponse(STATUS_SUCCESS, jwt);
        return ResponseEntity.ok(response);
    }

    /**
     * Can only beach reached and send true if jwt token is present and valid
     * @return
     */
    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth() {
        BooleanResponse response = new BooleanResponse(true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        MessageResponse response = new MessageResponse("Logout successfully", true);
        return ResponseEntity.ok(response);
    }

}
