package org.iypt.planner.domain.util;

import com.neovisionaries.i18n.CountryCode;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jlocker
 */
public class CountryCodeIO {

    private static final Map<String, CountryCode> nameToCountryMap = new HashMap<>();
    private static final Map<CountryCode, String> countryToNameMap = new HashMap<>();

    static {
        for (CountryCode cc : CountryCode.values()) {
            nameToCountryMap.put(cc.getName(), cc);
            // default values, will be overriden
            countryToNameMap.put(cc, cc.getName());
        }
        // support custom country names
        nameToCountryMap.put("Chinese Taipei", CountryCode.TW); // until 2013
        nameToCountryMap.put("Taiwan", CountryCode.TW); // 2013
        nameToCountryMap.put("Iran", CountryCode.IR);
        nameToCountryMap.put("Korea", CountryCode.KR);
        nameToCountryMap.put("Russia", CountryCode.RU);
        nameToCountryMap.put("United States of America", CountryCode.US);
        nameToCountryMap.put("USA", CountryCode.US);
    }

    public static CountryCode getByShortName(String name) {
        CountryCode cc = nameToCountryMap.get(name);
        // update the map to contain names from the current input CSV
        countryToNameMap.put(cc, name);
        return cc;
    }

    public static String getShortName(CountryCode cc) {
        return countryToNameMap.get(cc);
    }
}
