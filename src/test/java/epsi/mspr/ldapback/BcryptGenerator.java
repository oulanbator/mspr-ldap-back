package epsi.mspr.ldapback;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenerator {
    @Test
    public void bcryptPasswordGenerator() {
		// Choisir un psword à encoder puis run le test
		String passwordToEncode = "password";
		BCryptPasswordEncoder pencoder = new BCryptPasswordEncoder();

		String encoded = pencoder.encode(passwordToEncode);
		System.out.println("Password: " + passwordToEncode);
		System.out.println("Encodé BCrypt: " + encoded);
    }
    
}
