package epsi.mspr.ldapback.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import antlr.StringUtils;
import epsi.mspr.ldapback.utils.BrowsersUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import epsi.mspr.ldapback.model.entity.User;
import static epsi.mspr.ldapback.utils.AppConstants.*;
import epsi.mspr.ldapback.model.http.AuthenticationRequest;
import epsi.mspr.ldapback.model.http.StandardApiResponse;
import epsi.mspr.ldapback.service.CustomUserDetailService;
import epsi.mspr.ldapback.service.UserService;
import epsi.mspr.ldapback.service.jwt.JwtService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailService userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request) {
        String ip = request.getRemoteHost();
        String browser = BrowsersUtils.getInitialsFromAgent(request.getHeader("User-Agent"));

        // TODO : [SECURITY]  Gérer l'injection SQL ici ? Avant de passer à la suite ?
        
        // ###########################################################################
        // STEP 1 - Check LDAP credentials - Check if 2FA is activated
        // ###########################################################################
        try {
            String username = authenticationRequest.getUsername();
            String password = authenticationRequest.getPassword();
            // Verify credentials in LDAP : Bad credentials directly fall in BadCredentialsException
            UsernamePasswordAuthenticationToken credentialToken = new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(credentialToken);
            // If user exists in LDAP, load/create user in backend database
            User user = this.userService.loadLdapAuthenticatedUser(username);
            // If 2FA not activated thow Exception
            if (!user.isTwoFactorVerified()) throw new DisabledException("Two factors must be activated for : " + username);
        
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
        
        // ###########################################################################
        // STEP 2 - Credentials ok, account is activated : handle login with 2FA
        // ###########################################################################
        return handleSecondFactorLogin(authenticationRequest, ip, browser);
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
        if (Objects.equals(TOTP, "")) {
            // Generate 2FA secret in user entity and build fail response
            this.userService.initializeTwoFactorsSecret(username);
            StandardApiResponse response = new StandardApiResponse(STATUS_FAIL, MSG_ACTIVATE_TWO_FACTORS);
            // set barCode to response
            String barCode = this.userService.get2FactorsBarCode(username);
            response.setData(new ArrayList<>(Collections.singleton(barCode))); // TODO : revoir ça ?
            return ResponseEntity.ok(response);
        }

        // Case 2 - User is activating 2FA
        Boolean TOTPSuccess = this.userService.totpVerified(authenticationRequest); //user enter the 6 numbers
        if (TOTPSuccess) {    // TOTP Code is valid : activate account
            this.userService.activateAccount(username);
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
    private ResponseEntity<?> handleSecondFactorLogin(AuthenticationRequest authenticationRequest, String ip, String browser) {
        String username = authenticationRequest.getUsername();
        String TOTP = authenticationRequest.getTwoFactorsTotp();

        // 1 - Credentials ok : Check if 2FA code is present in request
        if (Objects.equals(TOTP, "")) {
            return ResponseEntity.ok(new StandardApiResponse(STATUS_SUCCESS, MSG_ENTER_TOTP));
        }

        // 2 - Check 2FA code validity
        boolean TOPTSuccess = this.userService.totpVerified(authenticationRequest);
        if (TOPTSuccess) {
            //todo: check ip et navigateur
            this.userService.checkIP(username, ip);
            this.userService.checkAgent(username, browser);

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

    /**
     * Utility route for checking current user authentication status.
     * Can only beach reached and return SUCCESS if jwt token is present and valid in the request headers
     * 
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
     * 
     * NOTE : En cas d'utilisation, la route /logout semble réservée par spring et crée 
     * des bugs de redirection au niveau du front. Si on l'implémente, décommenter la ligne concernée 
     * dans WebSecurityConfig.configure(HttpSecurity http)
     * 
     * @return ResponseEntity
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        StandardApiResponse response = new StandardApiResponse(STATUS_SUCCESS);
        return ResponseEntity.ok(response);
    }

    //debug pour mail process
//    @Autowired
//    public JavaMailSender emailSender;
//    @GetMapping("/sendmail")
//    public String send() {
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        message.setTo("i am an email");
//        message.setSubject("Test Simple Email");
//        message.setText("Hello, Im testing Simple Email");
//
//        // Send Message!
//        this.emailSender.send(message);
//
//        return "Email Sent!";
//    }

}
