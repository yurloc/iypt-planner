package org.iypt.planner.solver.util;

import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.drools.planner.core.score.constraint.UnweightedConstraintOccurrence;
import org.junit.Test;

import static org.drools.planner.core.score.constraint.ConstraintType.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author jlocker
 */
public class ConstraintComparatorTest {

    @Test
    public void testCompareByType() {
        ConstraintOccurrence hard = new IntConstraintOccurrence(null, NEGATIVE_HARD);
        ConstraintOccurrence soft = new IntConstraintOccurrence(null, NEGATIVE_SOFT);
        ConstraintOccurrence positive = new IntConstraintOccurrence(null, POSITIVE);
        ConstraintComparator comp = new ConstraintComparator();
        assertThat(comp.compare(hard, soft), lessThan(0));
        assertThat(comp.compare(soft, positive), lessThan(0));
        assertThat(comp.compare(hard, positive), lessThan(0));
        assertThat(comp.compare(hard, hard), is(0));
        assertThat(comp.compare(soft, soft), is(0));
        assertThat(comp.compare(positive, positive), is(0));
    }

    @Test
    public void testCompareByRuleId() {
        ConstraintOccurrence softConstraintA = new IntConstraintOccurrence("a", NEGATIVE_SOFT);
        ConstraintOccurrence softConstraintB = new IntConstraintOccurrence("b", NEGATIVE_SOFT);
        ConstraintOccurrence hardConstraintC = new IntConstraintOccurrence("c", NEGATIVE_HARD);
        ConstraintComparator comp = new ConstraintComparator();
        assertThat(comp.compare(softConstraintA, softConstraintB), lessThan(0));
        assertThat(comp.compare(hardConstraintC, softConstraintB), lessThan(0));
    }

    @Test
    public void testCompareByScore() {
        Object[] causes = new Object[]{};
        ConstraintOccurrence unweighted = new UnweightedConstraintOccurrence("", NEGATIVE_HARD, causes);
        ConstraintOccurrence hard1 = new IntConstraintOccurrence("", NEGATIVE_HARD, 1, causes);
        ConstraintOccurrence hard0 = new IntConstraintOccurrence("", NEGATIVE_HARD, 0, causes);
        ConstraintComparator comp = new ConstraintComparator();
        assertThat(comp.compare(hard1, hard0), lessThan(0));
        try {
            comp.compare(unweighted, hard0);
            fail("IllegalArgumentException expected, same rule should not insert different constraint types.");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

}
