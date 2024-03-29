package epsi.mspr.ldapback.utils;

public class AppConstants {
    public static final String APPLICATION_NAME = "MSPR-LDAP";

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    public static final String STATUS_ERROR = "error";

    public static final String MSG_BAD_CREDENTIALS = "Bad credentials, verify username and password";
    public static final String MSG_BLOCKED_ACCOUNT = "Error - Account blocked. Please contact IT services.";
    public static final String MSG_ACTIVATE_TWO_FACTORS = "Activation - You need to activate two factors authentication";
    public static final String MSG_ENTER_TOTP = "TOTP - Please enter your time-based code";
    public static final String MSG_TOTP_ERROR = "Error - Invalid one-time code";
    public static final String MSG_FOREIGNER_ERROR = "Error - This service is unavailable in your country";
    public static final String MSG_ACCOUNT_ACTIVATED = "Success - Two factors authentication activated";
    public static final String MSG_CONFIRM_MAIL = "Suspect Connection - Go onto your mail to confirm your identity";

    public static final String MSG_SERVER_ERROR_IS = "Server error : ";
    public static final String MSG_SERVER_ERROR_UNKNOWN = "Unknown server error";

    public static final String MSG_ERROR_INVALID_TOKEN = "Error : invalid token";
    public static final String MSG_ERROR_EXPIRED_TOKEN = "Error : token expired";
}
