package tor.learning.jwtsecurity.controller;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.http.*;
import tor.learning.jwtsecurity.service.MyUserDetailService;
import tor.learning.jwtsecurity.service.UserService;
import tor.learning.jwtsecurity.util.JwtUtil;

import static tor.learning.jwtsecurity.util.AppConstants.*;

import java.util.ArrayList;
import java.util.Collections;

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
        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();
        String TOTP = authenticationRequest.getTwoFactorsTotp();

        //TODO : STEP 0 - Verifier l'existence de l'utilisateur dans LDAP et le créer dans la BDD si c'est le cas
        
        // ###########################################################################
        // STEP 1 - Verify Credentials and 2FA Activation
        // ###########################################################################
        try
        {
            // Verify credentials
            UsernamePasswordAuthenticationToken credentialToken = new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(credentialToken);

        } catch (DisabledException e) {
            // DisabledException : Innactive account, needs to activate with 2FA

            // PATCH : pour les comptes non validés tous les credentitals passent... 
            // Vérification manuelle du password
            try {
                this.userService.verifyPassword(username, password);
            } catch (BadCredentialsException ex) {
                // if password mismatch
                return ResponseEntity.ok(new StandardApiResponse(STATUS_ERROR, MSG_BAD_CREDENTIALS));
            }

            // Si TOTP présent : utilisateur dans le parcours activation du compte
            if (!StringUtils.equals(TOTP, "")) {
                Boolean TOTPSuccess = this.userService.totpVerified(authenticationRequest);
                if (TOTPSuccess) {
                    this.userService.activateAccount(username);
                    return ResponseEntity.ok(new StandardApiResponse(STATUS_SUCCESS, MSG_ACCOUNT_ACTIVATED));
                } else {
                    return ResponseEntity.ok(new StandardApiResponse(STATUS_ERROR, MSG_TOTP_ERROR));
                }
            }

            // Generate 2FA secret in user entity
            this.userService.initializeTwoFactorsSecret(username);
            // build fail response
            StandardApiResponse response = new StandardApiResponse(STATUS_FAIL, MSG_ACTIVATE_TWO_FACTORS);
            // get barCode and set to response
            String barCode = this.userService.get2FactorsBarCode(username);
            response.setData(new ArrayList<>(Collections.singleton(barCode))); // TODO : revoir ça ?
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // wrong password
            StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_BAD_CREDENTIALS);
            return ResponseEntity.ok(response);

        }  catch (InternalAuthenticationServiceException e) {
            // username not found
            StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_BAD_CREDENTIALS);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // TODO : supprimer cette dernière gestion d'erreur ?
            e.printStackTrace();
        }

        // ###########################################################################
        // STEP 2 - Credentials ok, verify TOTP code (Activated account only)
        // ###########################################################################
        // Credentials ok : Check if 2FA code is present in request
        if (StringUtils.equals(TOTP, "")) {
            //  If no TOTP : return message to front-end for opening TOTP form
            StandardApiResponse response = new StandardApiResponse(STATUS_SUCCESS, MSG_ENTER_TOTP);
            return ResponseEntity.ok(response);
        }
        // Check 2FA code validity
        final User user = this.userService.getUserByUsername(username);
        if (user != null && user.isTwoFactorVerified()) {
            Boolean TOPTSuccess = this.userService.totpVerified(authenticationRequest);
            if (!TOPTSuccess) {
                // TOTP code invalid : return error response
                StandardApiResponse response = new StandardApiResponse(STATUS_ERROR, MSG_TOTP_ERROR);
                return ResponseEntity.ok(response);
            }
        }

        // ###########################################################################
        // STEP 3 - Credentials and TOTP ok ==> Authenticate user with JWT
        // ###########################################################################
        // If auth succeed and TOTP valid, build response with JWT token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String jwt = jwtUtil.generateToken(userDetails);
        StandardApiResponse response = new StandardApiResponse(STATUS_SUCCESS, jwt);
        return ResponseEntity.ok(response);
    }

    /**
     * Utility route for checking current user authentication status.
     * Can only beach reached and return SUCCESS if jwt token is present and valid in the request headers
     * @return ResponseEntity
     */
    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth() {
        StandardApiResponse response = new StandardApiResponse(STATUS_SUCCESS);
        return ResponseEntity.ok(response);
    }

    /**
     * Cette route n'est pas utilisée mais pourrait servir pour rajouter une couche de sécurité 
     * dans le back lors du logout (p.ex. mettre le token jwt dans une liste noire pour le rendre invalide)
     * C'est un peu complexe d'après ce que j'ai vu donc j'ai laissé tomber à ce stade.
     * @return
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        StandardApiResponse response = new StandardApiResponse(STATUS_SUCCESS);
        return ResponseEntity.ok(response);
    }

}
