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
                    System.out.println("\n***** FIRST CALL (no totp) *****");
                    System.out.println("Route : /api/authenticate");
                    System.out.println("Status : " + result.getResponse().getStatus());
                    System.out.println("Result : Got secret QRCode !");
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
                    System.out.println("\n***** SECOND CALL (with totp) *****");
                    System.out.println("Route : /api/authenticate");
                    System.out.println("Status : " + result.getResponse().getStatus());
                    System.out.println("Result : Account is activated !");
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
                    System.out.println("\n***** THIRD CALL (final connection with totp) *****");
                    System.out.println("Route : /api/authenticate");
                    System.out.println("Status : " + result.getResponse().getStatus());
                    System.out.println("Result : User allowed to connect (or not), regarding IP address");
                    System.out.println("IP Address : " + ipAddress);
                    RequestInfo.isIpFrench(ipAddress);
                    System.out.println("Response body : ");
                    System.out.println(result.getResponse().getContentAsString());
                    System.out.println("");
                });
    }
}
