package epsi.mspr.ldapback;

import epsi.mspr.ldapback.model.http.AuthenticationRequest;
import epsi.mspr.ldapback.utils.RequestInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrenchIpTest {

    @Test
    public void requestInfoUnitTest() {
        boolean ipLocalHost = RequestInfo.isIpFrench("127.0.0.1");
        boolean ipFr = RequestInfo.isIpFrench("185.125.226.44");
        boolean ipUS = RequestInfo.isIpFrench("209.58.129.89");
        boolean ipPoland = RequestInfo.isIpFrench("188.125.226.44");
        boolean ipPortable = RequestInfo.isIpFrench("80.214.221.17");

        // Assert True : localhost
        assertTrue(ipLocalHost);
        // Assert True : french ip
        assertTrue(ipFr);
        // Assert False : US ip
        assertFalse(ipUS);
        // Assert False : US ip
        assertFalse(ipPoland);
        // Assert True : french portable ip
        assertTrue(ipPortable);
    }

    @Test
    public void test() {
        AuthenticationRequest body = new AuthenticationRequest();
        body.setUsername("tor");
        body.setPassword("tue");
        body.setTwoFactorsTotp("geniale");

        System.out.println(body.toString());
    }
}
