package epsi.mspr.ldapback.integration;

import epsi.mspr.ldapback.LdapBackApplication;
import epsi.mspr.ldapback.model.entity.User;
import epsi.mspr.ldapback.model.http.AuthenticationRequest;
import epsi.mspr.ldapback.service.UserService;
import epsi.mspr.ldapback.utils.RequestInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LdapBackApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class ForeignIpAddress {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService userService;

    @Test
    public void creationEtActivationCompteUtilisateur() throws Exception {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
        String ipAddress = "185.125.226.44"; // FR
//        String ipAddress = "209.58.129.89"; // US
        String username = "TARACE@chatelet.local";
        String password = "Test&123";

        // Prépare premier call
        AuthenticationRequest body = new AuthenticationRequest();
        body.setUsername(username);
        body.setPassword(password);
        body.setTwoFactorsTotp("");

        mvc.perform(MockMvcRequestBuilders.post("/api/authenticate")
                        .with(request -> {
                            request.setRemoteHost(ipAddress);
                            request.addHeader("User-Agent", userAgent);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().is2xxSuccessful())
                .andDo(result -> {
                    System.out.println("\n***** PREMIER APPEL (pas de totp) : AUTHENTIFICATION LDAP *****");
                    System.out.println("Route : /api/authenticate");
                    System.out.println("Status : " + result.getResponse().getStatus());
                    System.out.println("Expected result : réception du QRCode");
                    System.out.println("Response body : ");
                    System.out.println(result.getResponse().getContentAsString());
                });

        User user = userService.getUserByUsername(username);

        // Premières assertions
        assertFalse(user.isTwoFactorVerified()); // Compte non vérifié
        assertNotNull(user.getTwoFactorSecret()); // Secret 2FA non null
        assertNotNull(user.getSecretSalt()); // Salt non null

        // Prépare le deuxième call
        String secretKey = userService.decryptTwoFactorsSecret(user);
        String totp = UserService.getTOTPCode(secretKey);
        body.setTwoFactorsTotp(totp);

        mvc.perform(MockMvcRequestBuilders.post("/api/authenticate")
                        .with(request -> {
                            request.setRemoteHost(ipAddress);
                            request.addHeader("User-Agent", userAgent);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().is2xxSuccessful())
                .andDo(result -> {
                    System.out.println("\n***** SECOND APPEL (avec totp) : ACTIVATION 2FA *****");
                    System.out.println("Route : /api/authenticate");
                    System.out.println("Status : " + result.getResponse().getStatus());
                    System.out.println("Expected result : activation du compte utilisateur");
                    System.out.println("Response body : ");
                    System.out.println(result.getResponse().getContentAsString());
                });

        user = userService.getUserByUsername(username);

        // Deuxièmes assertions
        assertTrue(user.isTwoFactorVerified()); // Compte vérifié

        // Prépare le troisième call
        totp = UserService.getTOTPCode(secretKey);
        body.setTwoFactorsTotp(totp);
        String ipAddressPtb = "80.214.221.17";

        mvc.perform(MockMvcRequestBuilders.post("/api/authenticate")
                        .with(request -> {
                            request.setRemoteHost(ipAddressPtb);
                            request.addHeader("User-Agent", userAgent);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().is2xxSuccessful())
                .andDo(result -> {
                    System.out.println("\n***** TROISIEME APPEL (avec totp) : CONNEXION A l'APPLICATION *****");
                    System.out.println("Route : /api/authenticate");
                    System.out.println("Status : " + result.getResponse().getStatus());
                    System.out.println("Expected result : Utilisateur connecté (ou non), selon l'adresse IP");
                    System.out.println("IP Address : " + ipAddress);
                    RequestInfo.isIpFrench(ipAddress);
                    System.out.println("Response body : ");
                    System.out.println(result.getResponse().getContentAsString());
                    System.out.println("");
                });
    }
    
    @Test
    public void checkBruteForce3Times() throws Exception {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
        String ipAddress = "185.125.226.44"; // FR
        String username = "CHECKBRUTE1@chatelet.local";
        String password = "BadPassword";
        
        AuthenticationRequest body = new AuthenticationRequest();
        body.setUsername(username);
        body.setPassword(password);
        body.setTwoFactorsTotp("");
        User user = userService.getUserByUsername(username);
        for(int i = 1; i <= 3; i++){
            
            mvc.perform(MockMvcRequestBuilders.post("/api/authenticate")
                        .with(request -> {
                            request.setRemoteHost(ipAddress);
                            request.addHeader("User-Agent", userAgent);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().is2xxSuccessful())
                .andDo(result -> {
                    System.out.println("\nTrying with bad password.");
                    System.out.println("Status : " + result.getResponse().getStatus());
                    System.out.println("Expected result : Not banned");
                    System.out.println("Response body : ");
                    System.out.println(result.getResponse().getContentAsString());
                });    
        }
        user = userService.getUserByUsername(username);
        assertFalse(user.isBlocked());
        userService.resetAndClearAttempt(username);
        

    }
    
    @Test
    public void checkBruteForce10Times() throws Exception {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
        String ipAddress = "185.125.226.44"; // FR
        String username = "CHECKBRUTE1@chatelet.local";
        String password = "BadPassword";
        
        AuthenticationRequest body = new AuthenticationRequest();
        body.setUsername(username);
        body.setPassword(password);
        body.setTwoFactorsTotp("");
        User user = userService.getUserByUsername(username);
        for(int i = 1; i <= 10; i++){
            
            mvc.perform(MockMvcRequestBuilders.post("/api/authenticate")
                        .with(request -> {
                            request.setRemoteHost(ipAddress);
                            request.addHeader("User-Agent", userAgent);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().is2xxSuccessful())
                .andDo(result -> {
                    System.out.println("\nTrying with bad password.");
                    System.out.println("Status : " + result.getResponse().getStatus());
                    System.out.println("Expected result : Banned");
                    System.out.println("Response body : ");
                    System.out.println(result.getResponse().getContentAsString());
                });    
        }
        System.out.println(user.isBlocked());
        user = userService.getUserByUsername(username);
        assertTrue(user.isBlocked());
        userService.resetAndClearAttempt(username);
        

    }
}
