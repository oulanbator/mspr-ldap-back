package tor.learning.jwtsecurity.util;

public class AppConstants {
    public static final String APPLICATION_NAME = "MyApp";

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    public static final String STATUS_ERROR = "error";

    public static final String MSG_BAD_CREDENTIALS = "Bad credentials, verify username and password";
    public static final String MSG_ACTIVATE_TWO_FACTORS = "Activation - You need to activate two factors authentication";
    public static final String MSG_ENTER_TOTP = "TOTP - Please enter your time-based code";
    public static final String MSG_TOTP_ERROR = "Error - Invalid one-time code";
    public static final String MSG_ACCOUNT_ACTIVATED = "Success - Two factors authentication activated";

    public static final String MSG_SERVER_ERROR_IS = "Server error : ";
    public static final String MSG_SERVER_ERROR_UNKNOWN = "Unknown server error";

    public static final String MSG_ERROR_INVALID_TOKEN = "Error : invalid token";
    public static final String MSG_ERROR_EXPIRED_TOKEN = "Error : token expired";
}
