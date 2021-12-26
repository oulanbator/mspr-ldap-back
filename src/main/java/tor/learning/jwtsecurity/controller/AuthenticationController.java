package tor.learning.jwtsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import tor.learning.jwtsecurity.model.http.AuthenticationRequest;
import tor.learning.jwtsecurity.model.http.AuthenticationResponse;
import tor.learning.jwtsecurity.model.http.BooleanResponse;
import tor.learning.jwtsecurity.model.http.MessageResponse;
import tor.learning.jwtsecurity.util.JwtUtil;

@RestController
@CrossOrigin
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
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
        } catch (Exception e) { //BadCredentialsException
            AuthenticationResponse response = new AuthenticationResponse(false, "Incorrect username or password");
            return ResponseEntity.ok(response);
        }
        // If auth succeed, build response with JWT
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        AuthenticationResponse response = new AuthenticationResponse(true, jwt);
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
