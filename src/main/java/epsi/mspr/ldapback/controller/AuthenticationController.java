package epsi.mspr.ldapback.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static epsi.mspr.ldapback.utils.AppConstants.STATUS_SUCCESS;
import epsi.mspr.ldapback.model.http.AuthenticationRequest;
import epsi.mspr.ldapback.model.http.StandardApiResponse;
import epsi.mspr.ldapback.service.AuthenticationService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Route unique pour l'authentification des utilisateurs. Toute requête liée à l'authentification est
     * redirigée et gérée par AuthenticationService qui gère les différents cas de figure.
     * 
     * @param authenticationRequest Body of the request
     * @param request Http request
     * @return ResponseEntity
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request) {
        // TODO : [SECURITY]  Gérer l'injection SQL ici ? Avant de passer à la suite ?

        return authenticationService.authenticate(authenticationRequest, request);
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

}
