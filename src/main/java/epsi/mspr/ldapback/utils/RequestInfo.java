package epsi.mspr.ldapback.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.util.Objects;

@Component
public class RequestInfo {

    public static String getInitialsFromAgent(String userAgent){
        String[] fullBrowserInfos = userAgent.split(" ");
        String fullBrowserInfo = fullBrowserInfos[fullBrowserInfos.length-1];
        return fullBrowserInfo.substring(0,3);
    }

    public static boolean isIpFrench(String ip) {

        if (StringUtils.equals(ip, "127.0.0.1") || StringUtils.equals(ip, "0:0:0:0:0:0:0:1")) {
            System.out.println("Connecting from localhost");
            return true;
        }

        try {
            File database = new ClassPathResource("GeoLite2-City.mmdb").getFile();
            DatabaseReader dbReader = new DatabaseReader.Builder(database).build();
            CityResponse response = dbReader.city(InetAddress.getByName(ip));
            String countryName = response.getCountry().getName();
            System.out.println("Country : " + countryName);
            return StringUtils.equals(countryName, "France");
        } catch (Exception e) {
            System.out.println("Error while checking IP address in local database");
            e.printStackTrace();
            return false;
        }
    }
}
