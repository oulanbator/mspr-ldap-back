package epsi.mspr.ldapback.utils;

import java.util.List;

public class BrowsersUtils {

    public static String getInitialsFromAgent(String userAgent){
        String[] fullBrowserInfos = userAgent.split(" ");
        String fullBrowserInfo = fullBrowserInfos[fullBrowserInfos.length-1];
        String initials = fullBrowserInfo.substring(0,3);
        return initials;
    }
}
