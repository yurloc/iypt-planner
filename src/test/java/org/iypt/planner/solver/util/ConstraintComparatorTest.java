package org.iypt.planner.solver.util;

import org.iypt.planner.Constants;
import org.iypt.planner.solver.ConstraintRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jlocker
 */
public class ConstraintComparatorTest {

    @Test
    public void testCompareByType() {
        ConstraintRule hard = new ConstraintRule(null, Constants.CONSTRAINT_TYPE_HARD);
        ConstraintRule soft = new ConstraintRule(null, Constants.CONSTRAINT_TYPE_SOFT);
        ConstraintComparator comp = new ConstraintComparator();
        assertThat(comp.compare(hard, soft)).isLessThan(0);
        assertThat(comp.compare(hard, hard)).isEqualTo(0);
        assertThat(comp.compare(soft, soft)).isEqualTo(0);
    }

    @Test
    public void testCompareByRuleId() {
        ConstraintRule softConstraintA = new ConstraintRule("a", Constants.CONSTRAINT_TYPE_SOFT);
        ConstraintRule softConstraintB = new ConstraintRule("b", Constants.CONSTRAINT_TYPE_SOFT);
        ConstraintRule hardConstraintC = new ConstraintRule("c", Constants.CONSTRAINT_TYPE_HARD);
        ConstraintComparator comp = new ConstraintComparator();
        assertThat(comp.compare(softConstraintA, softConstraintB)).isLessThan(0);
        assertThat(comp.compare(hardConstraintC, softConstraintB)).isLessThan(0);
    }
}
