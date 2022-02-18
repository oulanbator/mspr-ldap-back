package epsi.mspr.ldapback.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

import epsi.mspr.ldapback.utils.ListUtils;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import de.taimos.totp.TOTP;
import epsi.mspr.ldapback.model.entity.User;
import epsi.mspr.ldapback.model.http.AuthenticationRequest;
import epsi.mspr.ldapback.model.repository.UserRepository;
import epsi.mspr.ldapback.utils.AppConstants;

import javax.transaction.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;


    // Basic CRUD :
    // - Create / Update => saveUser()
    // - Read => getUser() (...)

    public User getUser(Long userid) {
        return this.userRepository.findById(userid).get();
    }

    public User getUserByUsername(String username) {
        return this.userRepository.findUserByUsername(username);
    }

    public User getUserByEmail(String email) {
        return this.userRepository.findUserByEmail(email);
    }

    public User saveUser(User user) {
        return this.userRepository.save(user);
    }

    // TWO FACTORS AUTHENTICATION
    public User loadLdapAuthenticatedUser(String username) {
        User user = getUserByUsername(username);
        if (user != null) {
            return user;
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            return saveUser(newUser);
        }
    }

    public void verifyPassword(String username, String password) throws BadCredentialsException {
        final User user = getUserByUsername(username);
        if (!StringUtils.equals(password, user.getPassword())) {
            throw new BadCredentialsException("BadCredentialsException");
        }
    }

    public User initializeTwoFactorsSecret(String username) {
        User user = getUserByUsername(username);
        user.setTwoFactorSecret(generateSecretKey());
        return this.saveUser(user);
    }

    public void activateAccount(String username) {
        User user = getUserByUsername(username);
        user.setTwoFactorVerified(true);
        saveUser(user);
    }

    private void addIP(String username, String ip){
        User user = getUserByUsername(username);
        String newList = ListUtils.addToList(user.getIpList(), ip);
        user.setIpList(newList);
    }

    public void checkIP(String username, String ip){
        User user = getUserByUsername(username);
        List<String> ips = ListUtils.stringToList(user.getIpList());
        if(!ips.contains(ip)){
            //todo: envoi mail de signalement
            //todo: bloquer si ip etrangere
            addIP(username, ip);
        }
    }

    private void addAgent(String username, String agent){
        User user = getUserByUsername(username);
        String newList = ListUtils.addToList(user.getAgentList(), agent);
        user.setAgentList(newList);
    }

    public void checkAgent(String username, String agent){
        User user = getUserByUsername(username);
        List<String> agents = ListUtils.stringToList(user.getIpList());
        if(!agents.contains(agent)){
            //todo: envoi mail qui vise à confirmer la connexion
            addAgent(username, agent);
        }
    }

    private static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public static String getTOTPCode(String secretKey) {
        System.out.println(secretKey);
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }

    public String get2FactorsBarCode(String username) {
        User user = this.getUserByUsername(username);
        if (user != null) {
            String secretKey = user.getTwoFactorSecret();
            String issuer = AppConstants.APPLICATION_NAME;
            return buildGoogleAuthenticatorBarCode(secretKey, username, issuer);
        } else {
            return null;
        }
    }

    public static String buildGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean totpVerified(AuthenticationRequest authenticationRequest) {
        // Check Totp presence
        if (StringUtils.equals(authenticationRequest.getTwoFactorsTotp(), null)) {
            return false;
        } else {
            // Get user totp
            User user = getUserByUsername(authenticationRequest.getUsername());
            String secretKey = user.getTwoFactorSecret();
            //Verify Totp
            String totpInput = authenticationRequest.getTwoFactorsTotp();
            return Objects.equals(totpInput, getTOTPCode(secretKey));
        }
    }
}
