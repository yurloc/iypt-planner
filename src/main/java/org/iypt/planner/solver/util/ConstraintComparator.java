package org.iypt.planner.solver.util;

import java.io.Serializable;
import java.util.Comparator;
import org.iypt.planner.solver.ConstraintRule;

/**
 *
 * @author jlocker
 */
public class ConstraintComparator implements Comparator<ConstraintRule>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ConstraintRule o1, ConstraintRule o2) {
        if (o1 == o2) {
            return 0;
        }

        // if the constrait types are different, sort by type
        if (!o1.getType().equals(o2.getType())) {
            return o1.getType().compareTo(o2.getType());
        }

        // same constraint type, now sorting by ruleId natural order (lexicographically)
        return o1.getName().compareTo(o2.getName());
    }
}
