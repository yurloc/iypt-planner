package org.iypt.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jlocker
 */
public class Conflicts {

    private static Map<Country, Set<Country>> conflicts = new HashMap<Country, Set<Country>>();
    
    public static boolean addConflict(Country c1, Country c2) {
        if (!conflicts.containsKey(c1) && !conflicts.containsKey(c2)) {
            HashSet<Country> group = new HashSet<Country>();
            group.add(c1);
            group.add(c2);
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
    
    public static boolean isConflict(Country c1, Country c2) {
        if (c1.equals(c2)) return false;
        if (!conflicts.containsKey(c1)) return false;
        return conflicts.get(c1).contains(c2);
    }
}
