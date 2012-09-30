package org.iypt.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jlocker
 */
public class Conflicts {

    private static Map<CountryCode, Set<CountryCode>> conflicts = new EnumMap<CountryCode, Set<CountryCode>>(CountryCode.class);
    
    public static boolean addConflict(CountryCode c1, CountryCode c2) {
        if (!conflicts.containsKey(c1) && !conflicts.containsKey(c2)) {
            EnumSet<CountryCode> group = EnumSet.of(c1, c2);
            conflicts.put(c1, group);
            conflicts.put(c2, group);
            return true;
        } else {
            if (conflicts.containsKey(c1)) {
                conflicts.put(c2, conflicts.get(c1));
                return conflicts.get(c1).add(c2);
            } else {
                conflicts.put(c1, conflicts.get(c2));
                return conflicts.get(c1).add(c1);
            }
        }
    }
    
    public static boolean isConflict(CountryCode c1, CountryCode c2) {
        if (c1.equals(c2)) return false;
        if (!conflicts.containsKey(c1)) return false;
        return conflicts.get(c1).contains(c2);
    }
}
