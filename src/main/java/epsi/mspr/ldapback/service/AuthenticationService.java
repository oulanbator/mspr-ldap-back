package epsi.mspr.ldapback.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static epsi.mspr.ldapback.utils.AppConstants.*;

import java.util.ArrayList;
import java.util.Collections;

import epsi.mspr.ldapback.model.entity.User;
import epsi.mspr.ldapback.model.http.AuthenticationRequest;
import epsi.mspr.ldapback.model.http.StandardApiResponse;
import epsi.mspr.ldapback.service.jwt.JwtService;

@Service
public class AuthenticationService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailService userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    public ResponseEntity<?> authenticate(AuthenticationRequest authenticationRequest, HttpServletRequest request) {
        
        System.out.println(request.getHeader("User-Agent"));
        System.out.println(request.getRemoteAddr());

        // ###########################################################################
        // STEP 1 - Check LDAP credentials - Check if 2FA is activated
        // ###########################################################################

        try {
            verifyCredentialsAndActivatedAccount(authenticationRequest);    
            //Check ip and address        
        
        } catch (DisabledException e) {
            return handleDisabledException(authenticationRequest);
        
        } catch (BadCredentialsException e) {
            // TODO : [SECURITY] Gérer ici les tentatives successives avec mauvais credentials pour un utilisateur existant ?
            return ResponseEntity.ok(new StandardApiResponse(STATUS_ERROR, MSG_BAD_CREDENTIALS));
        
        } catch (Exception e) {
            // TODO : [CODE MORT] Supprimer ?
            e.printStackTrace();
            return ResponseEntity.ok(new StandardApiResponse(STATUS_ERROR, MSG_SERVER_ERROR_UNKNOWN));
        }

        // check ip nav

        // ###########################################################################
        // STEP 2 - If credentials ok and account is activated : handle 2nd factor (TOTP)
        // ###########################################################################
        return handleSecondFactorLogin(authenticationRequest);

    }
    
    private void verifyCredentialsAndActivatedAccount(AuthenticationRequest authenticationRequest) throws DisabledException, BadCredentialsException {
        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();
        // Verify credentials in LDAP : Bad credentials directly fall in BadCredentialsException
        UsernamePasswordAuthenticationToken credentialToken = new UsernamePasswordAuthenticationToken(username, password);
        authenticationManager.authenticate(credentialToken);
        // If user exists in LDAP, load/create user in backend database
        User user = this.userService.loadLdapAuthenticatedUser(username);
        // If 2FA not activated thows DisabledException
        if (!user.isTwoFactorVerified()) throw new DisabledException("Two factors must be activated for : " + username);
    }

    /**
     * Handles DisabledException. If credentials are authenticated in LDAP, 
     * two user paths can throw this exception :
     * 
     * 1 - First login of the user : needs to activate 2FA
     *      - User has just been created in backend database
     *      - User is disabled
     *      - User just sent credentials and there is no TOTP in the request
     * 
     * 2 - User is activating 2FA
     *      - User is still disabled as 2FA is not activated yet
     *      - User is validating TOTP code, therefore it is present in the request
     * 
     * @param authenticationRequest
     * @return ResponseEntity
     */
    private ResponseEntity<?> handleDisabledException(AuthenticationRequest authenticationRequest) {
        String username = authenticationRequest.getUsername();
        String TOTP = authenticationRequest.getTwoFactorsTotp();
        
        // Case 1 - First login for this user : needs to activate 2FA
        if (StringUtils.equals(TOTP, "")) {
            // Generate 2FA secret in user entity and build fail response
            this.userService.initializeTwoFactorsSecret(username);
            StandardApiResponse response = new StandardApiResponse(STATUS_FAIL, MSG_ACTIVATE_TWO_FACTORS);
            // set barCode to response
            String barCode = this.userService.get2FactorsBarCode(username);
            response.setData(new ArrayList<>(Collections.singleton(barCode))); // TODO : revoir ça ?
            return ResponseEntity.ok(response);
        }

        // Case 2 - User still disabled but activating 2FA
        Boolean TOTPSuccess = this.userService.totpVerified(authenticationRequest);
        if (TOTPSuccess) {        
            // TOTP Code is valid : activate account 
            this.userService.activateAccount(username);
            // recupere navigateur et ip
            return ResponseEntity.ok(new StandardApiResponse(STATUS_SUCCESS, MSG_ACCOUNT_ACTIVATED));
        } else {
            return ResponseEntity.ok(new StandardApiResponse(STATUS_ERROR, MSG_TOTP_ERROR));
        }
    }

    /**
     * Handles login : once credentials are ok, and we checked that account is activated with 2FA. 
     * Front-end will make two successive calls here :
     * 
     * 1 - After User send valid credentials for login :
     *      - no TOTP in the request
     *      - send response back with message asking for opening TOTP form
     * 
     * 2 - After User give TOTP code :
     *      - TOTP valid => send success response with JWT token
     *      - TOTP incorrect => send error response
     * 
     * @param authenticationRequest
     * @return ResponseEntity
     */
    private ResponseEntity<?> handleSecondFactorLogin(AuthenticationRequest authenticationRequest) {
        String username = authenticationRequest.getUsername();
        String TOTP = authenticationRequest.getTwoFactorsTotp();

        // 1 - Credentials ok : Check if 2FA code is present in request
        if (StringUtils.equals(TOTP, "")) {
            // No TOTP : user goes to TOTP popup
            return ResponseEntity.ok(new StandardApiResponse(STATUS_SUCCESS, MSG_ENTER_TOTP));
        }

        // 2 - Check 2FA code validity
        Boolean TOPTSuccess = this.userService.totpVerified(authenticationRequest);
        if (TOPTSuccess) {
            // If auth succeed and TOTP valid, build response with JWT token
            // TODO : [SECURITY] vérifier les infos passées dans ce token dans customUserDetailsService
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            final String jwt = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new StandardApiResponse(STATUS_SUCCESS, jwt));
        } else {
            // TOTP code incorrect : return error response
            // TODO : [SECURITY] Gérer ici les tentatives successives avec erreur de TOTP code ?
            return ResponseEntity.ok(new StandardApiResponse(STATUS_ERROR, MSG_TOTP_ERROR));
        }
    }
    
}
