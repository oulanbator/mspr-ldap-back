package epsi.mspr.ldapback.utils;

import org.apache.commons.codec.binary.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListUtils {
    public static List<String> stringToList(String string) {
        List<String> finalList = new ArrayList<>();
        Arrays.asList(string.split(",")).forEach(e -> {
            if (!StringUtils.equals(e, "")) {
                finalList.add(e.trim());
            }
        });
        return finalList;
    }

    public static String listToString(List<String> liste){
        return String.join(",", liste);
    }

    public static String addToList(String stringList, String item){
        List<String> list = stringToList(stringList);
        list.add(item);
        return listToString(list);
    }
}
