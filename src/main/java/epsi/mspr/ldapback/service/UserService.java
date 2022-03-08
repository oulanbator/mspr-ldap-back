package epsi.mspr.ldapback.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import epsi.mspr.ldapback.model.entity.MailVerificationToken;
import epsi.mspr.ldapback.model.repository.MailVerificationTokenRepository;
import epsi.mspr.ldapback.utils.ListUtils;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import de.taimos.totp.TOTP;
import epsi.mspr.ldapback.model.entity.User;
import epsi.mspr.ldapback.model.http.AuthenticationRequest;
import epsi.mspr.ldapback.model.repository.UserRepository;
import epsi.mspr.ldapback.utils.AppConstants;

import javax.transaction.Transactional;

@Service
public class UserService {
//    @Value("${SPRINGBOOT_SECRET}") // décommenter pour récupérer le secret depuis une variable d'environnement
    private String appSecret = "mspr&infrastructure";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MailVerificationTokenRepository mailVerificationTokenRepository;


    // ---------- Basic CRUD ----------

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
            user.setBadCredentialsAttempts(0);
            return saveUser(user);
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setBadCredentialsAttempts(0);
            return saveUser(newUser);
        }
    }

    // ---------- 2FA secret & activation ----------

    public User initializeTwoFactorsSecret(String username) {
        User user = getUserByUsername(username);
        if (user == null) return null;
        // Generate Secret
        String userSecret = generateSecretKey();
        // Encrypt secret
        String salt = KeyGenerators.string().generateKey();
        String uniqueSecretEncoder = appSecret + user.getUsername();
        TextEncryptor encryptor = Encryptors.text(uniqueSecretEncoder, salt);
        String encryptedSecret = encryptor.encrypt(userSecret);
        // Persist encrypted secret and salt in database
        user.setTwoFactorSecret(encryptedSecret);
        user.setSecretSalt(salt);
        return this.saveUser(user);
    }
    public String decryptTwoFactorsSecret(User user) {
        // Get database variables
        String encryptedSecret = user.getTwoFactorSecret();
        String salt = user.getSecretSalt();
        String uniqueSecretEncoder = appSecret + user.getUsername();
        // Get encryptor
        TextEncryptor encryptor = Encryptors.text(uniqueSecretEncoder, salt);
        String userSecret = encryptor.decrypt(encryptedSecret);
        return userSecret;
    }

    public void activateAccount(String username, String ip, String agent) {
        activateUser(username);
        addAgent(username, agent);
        addIP(username, ip);
    }

    public void activateUser(String username) {
        User user = getUserByUsername(username);
        user.setTwoFactorVerified(true);
        saveUser(user);
    }

    // ---------- IP and UserAgent ----------

    private void addIP(String username, String ip){
        User user = getUserByUsername(username);
        String newList = ListUtils.addToList(user.getIpList(), ip);
        user.setIpList(newList);
        saveUser(user);
    }

    public void addAgent(String username, String agent){
        User user = getUserByUsername(username);
        String newList = ListUtils.addToList(user.getAgentList(), agent);
        user.setAgentList(newList);
        saveUser(user);
    }

    public boolean checkIfIpExists(String username, String ip){
        User user = getUserByUsername(username);
        List<String> ips = ListUtils.stringToList(user.getIpList());
        if(!ips.contains(ip)){
            addIP(username, ip);
            return false;
        }
        return true;
    }

    public boolean checkAgent(String username, String agent){
        User user = getUserByUsername(username);
        List<String> agents = ListUtils.stringToList(user.getAgentList());
        if(!agents.contains(agent)){
            return false;
        }
        return true;
    }

    // ---------- TOTP ----------

    private static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public static String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }

    public String get2FactorsBarCode(String username) {
        User user = this.getUserByUsername(username);
        if (user != null) {
            String secretKey = decryptTwoFactorsSecret(user);
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

    public boolean totpVerified(String totp, String username) {
        // Check Totp presence
        if (StringUtils.equals(totp, null)) {
            return false;
        } else {
            // Get user
            User user = getUserByUsername(username);
            if (user == null) return false;
            //Verify Totp
            String secretKey = decryptTwoFactorsSecret(user);
            String totpInput = totp;
            return Objects.equals(totpInput, getTOTPCode(secretKey));
        }
    }

    // ---------- EMAIL VERIFICATION ----------

    public MailVerificationToken createVerificationToken(String token, String username, String browser) {
        MailVerificationToken newToken = new MailVerificationToken(token, username, browser);
        return this.mailVerificationTokenRepository.save(newToken);
    }

    public MailVerificationToken getVerificationToken(String VerificationToken) {
        Optional<MailVerificationToken> token = this.mailVerificationTokenRepository.findByToken(VerificationToken);
        if (token.isPresent()) {
            return token.get();
        } else {
            return null;
        }
    }

    // ---------- BRUTE FORCE GUARD ----------

    public void badCredentialAttempt(String username) {
        User user = getUserByUsername(username);
        if (user != null) {
            int attempts = user.getBadCredentialsAttempts();
            user.setBadCredentialsAttempts(attempts + 1);
            if (user.getBadCredentialsAttempts() > 5) {
                user.setBlocked(true);
            }
            userRepository.save(user);
        }
    }
}
