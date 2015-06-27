package org.iypt.planner.solver.util;

import org.junit.Test;
import org.optaplanner.core.impl.score.constraint.ConstraintOccurrence;
import org.optaplanner.core.impl.score.constraint.IntConstraintOccurrence;
import org.optaplanner.core.impl.score.constraint.UnweightedConstraintOccurrence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.optaplanner.core.impl.score.constraint.ConstraintType.*;

/**
 *
 * @author jlocker
 */
public class ConstraintComparatorTest {

    @Test
    public void testCompareByType() {
        ConstraintOccurrence hard = new IntConstraintOccurrence(null, HARD);
        ConstraintOccurrence soft = new IntConstraintOccurrence(null, SOFT);
        ConstraintComparator comp = new ConstraintComparator();
        assertThat(comp.compare(hard, soft)).isLessThan(0);
        assertThat(comp.compare(hard, hard)).isEqualTo(0);
        assertThat(comp.compare(soft, soft)).isEqualTo(0);
    }

    @Test
    public void testCompareByRuleId() {
        ConstraintOccurrence softConstraintA = new IntConstraintOccurrence("a", SOFT);
        ConstraintOccurrence softConstraintB = new IntConstraintOccurrence("b", SOFT);
        ConstraintOccurrence hardConstraintC = new IntConstraintOccurrence("c", HARD);
        ConstraintComparator comp = new ConstraintComparator();
        assertThat(comp.compare(softConstraintA, softConstraintB)).isLessThan(0);
        assertThat(comp.compare(hardConstraintC, softConstraintB)).isLessThan(0);
    }

    @Test
    public void testCompareByScore() {
        Object[] causes = new Object[]{};
        ConstraintOccurrence unweighted = new UnweightedConstraintOccurrence("", HARD, causes);
        ConstraintOccurrence hard1 = new IntConstraintOccurrence("", HARD, 1, causes);
        ConstraintOccurrence hard0 = new IntConstraintOccurrence("", HARD, 0, causes);
        ConstraintComparator comp = new ConstraintComparator();
        assertThat(comp.compare(hard1, hard0)).isLessThan(0);
        try {
            comp.compare(unweighted, hard0);
            fail("IllegalArgumentException expected, same rule should not insert different constraint types.");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

}
