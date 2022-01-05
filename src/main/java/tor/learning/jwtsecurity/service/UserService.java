package tor.learning.jwtsecurity.service;

import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tor.learning.jwtsecurity.model.entity.User;
import tor.learning.jwtsecurity.model.entity.VerificationToken;
import tor.learning.jwtsecurity.model.http.AuthenticationRequest;
import tor.learning.jwtsecurity.model.http.RegistrationRequest;
import tor.learning.jwtsecurity.model.repository.UserRepository;
import tor.learning.jwtsecurity.model.repository.VerificationTokenRepository;
import tor.learning.jwtsecurity.util.exception.UserAlreadyExistException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Objects;

import static tor.learning.jwtsecurity.util.AppConstants.APPLICATION_NAME;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

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

    public User registerNewUserAccount(RegistrationRequest registrationRequest) throws UserAlreadyExistException {
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setUsername(registrationRequest.getUsername());
        user.setTwoFactorEnabled(registrationRequest.getTwoFactorEnabled());

        // TODO : encode password ? Or get encoded from front-end
        user.setPassword(registrationRequest.getPassword());

        user.setTwoFactorSecret(generateSecretKey());
        try {
            return this.saveUser(user);
        } catch (Exception e) {
            throw new UserAlreadyExistException("Email or Username Already taken");
        }
    }

    // EMAIL VERIFICATION

    public VerificationToken createVerificationToken(User user, String token) {
        VerificationToken newToken = new VerificationToken(token, user);
        return this.verificationTokenRepository.save(newToken);
    }

    public VerificationToken getVerificationToken(String VerificationToken) {
        return this.verificationTokenRepository.findByToken(VerificationToken);
    }

    // TWO FACTORS AUTHENTICATION

    public static String generateSecretKey() {
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
            String secretKey = user.getTwoFactorSecret();
            String issuer = APPLICATION_NAME;
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
