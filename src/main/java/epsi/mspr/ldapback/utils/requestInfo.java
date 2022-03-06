package epsi.mspr.ldapback.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

import java.io.File;
import java.net.InetAddress;
import java.util.Objects;

public class requestInfo {

    public static String getInitialsFromAgent(String userAgent){
        String[] fullBrowserInfos = userAgent.split(" ");
        String fullBrowserInfo = fullBrowserInfos[fullBrowserInfos.length-1];
        return fullBrowserInfo.substring(0,3);
    }

    public static boolean isIpFrench(String ip) throws Exception {
        //todo: check if IP is french using lib/ip2location/ package
        File database = new File("/resources/GeoLite2-City.mmdb");
        DatabaseReader dbReader = new DatabaseReader.Builder(database).build();
        CityResponse response = dbReader.city(InetAddress.getByName(ip));
        String countryName = response.getCountry().getName();

        return Objects.equals(countryName, "France");
    }
}
