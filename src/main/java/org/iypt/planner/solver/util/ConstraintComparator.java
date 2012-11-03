package org.iypt.planner.solver.util;

import java.util.Comparator;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.constraint.ConstraintType;
import org.drools.planner.core.score.constraint.DoubleConstraintOccurrence;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.drools.planner.core.score.constraint.LongConstraintOccurrence;
import org.drools.planner.core.score.constraint.UnweightedConstraintOccurrence;

/**
 *
 * @author jlocker
 */
public class ConstraintComparator implements Comparator<ConstraintOccurrence> {

    @Override
    public int compare(ConstraintOccurrence o1, ConstraintOccurrence o2) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        if (o1 == o2) {
            return EQUAL;
        }

        // if the constrait types are different, sort as hard < soft < positive
        if (o1.getConstraintType() != o2.getConstraintType()) {
            switch (o1.getConstraintType()) {
                case NEGATIVE_HARD:
                    return BEFORE;
                case NEGATIVE_SOFT:
                    if (o2.getConstraintType() == ConstraintType.NEGATIVE_HARD) {
                        return AFTER;
                    }
                    return BEFORE;
                case POSITIVE:
                    return BEFORE;
                default:
                    throw new AssertionError();
            }
        }

        // same constraint type, now sorting by ruleId natural order (lexicographically)
        if (!o1.getRuleId().equals(o2.getRuleId())) {
            return o1.getRuleId().compareTo(o2.getRuleId());
        }

        // same ruleId, now sorting by weight
        if (o1 instanceof UnweightedConstraintOccurrence) {
            if (!(o2 instanceof UnweightedConstraintOccurrence)) {
                throwClassMismatchException(o1, o2);
            }
            return EQUAL;
        }
        if (o1 instanceof IntConstraintOccurrence) {
            if (!(o2 instanceof IntConstraintOccurrence)) {
                throwClassMismatchException(o1, o2);
            }
            return Integer.compare(((IntConstraintOccurrence) o2).getWeight(), ((IntConstraintOccurrence) o1).getWeight());
        }
        if (o1 instanceof LongConstraintOccurrence) {
            if (!(o2 instanceof LongConstraintOccurrence)) {
                throwClassMismatchException(o1, o2);
            }
            return Long.compare(((LongConstraintOccurrence) o2).getWeight(), ((LongConstraintOccurrence) o1).getWeight());
        }
        if (o1 instanceof DoubleConstraintOccurrence) {
            if (!(o2 instanceof DoubleConstraintOccurrence)) {
                throwClassMismatchException(o1, o2);
            }
            return Double.compare(((DoubleConstraintOccurrence) o2).getWeight(), ((DoubleConstraintOccurrence) o1).getWeight());
        }
        throw new IllegalArgumentException("Unsupported constraint class: " + o1.getClass());
    }

    private void throwClassMismatchException(ConstraintOccurrence co1, ConstraintOccurrence co2) {
        throw new IllegalArgumentException(String.format("Different constraint classes (%s and %s) inserted by the same rule (%s).", co1.getClass(), co2.getClass(), co1.getRuleId()));
    }
}