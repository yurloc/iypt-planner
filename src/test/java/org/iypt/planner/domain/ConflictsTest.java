package org.iypt.planner.domain;

import org.iypt.planner.domain.Conflicts;
import org.junit.Test;

import static org.iypt.planner.domain.CountryCode.CZ;
import static org.iypt.planner.domain.CountryCode.DE;
import static org.iypt.planner.domain.CountryCode.SK;
import static org.junit.Assert.*;

/**
 *
 * @author jlocker
 */
public class ConflictsTest {

    /**
     * Test of addConflict method, of class Conflicts.
     */
    @Test
    public void testAddConflict() {
        assertFalse("Empty conflicts test failed", Conflicts.isConflict(CZ, SK));

        assertTrue("Adding cz-sk conflict should succeed", Conflicts.addConflict(CZ, SK));
        assertFalse("Adding cz-sk conflict should not succeed", Conflicts.addConflict(CZ, SK));
        assertFalse("Adding sk-cz conflict should not succeed", Conflicts.addConflict(SK, CZ));

        // conflict is symmetric
        assertTrue(Conflicts.isConflict(CZ, SK));
        assertTrue(Conflicts.isConflict(SK, CZ));

        // conflict is not reflexive
        assertFalse(Conflicts.isConflict(CZ, CZ));
        assertFalse(Conflicts.isConflict(DE, DE));

        // countries not in conflict
        assertFalse(Conflicts.isConflict(CZ, DE));
        assertFalse(Conflicts.isConflict(DE, SK));

        assertTrue(Conflicts.addConflict(SK, DE));

        // countries in conflict (and transitivity)
        assertTrue(Conflicts.isConflict(DE, SK));
        assertTrue(Conflicts.isConflict(DE, CZ));
        assertTrue(Conflicts.isConflict(SK, DE));
        assertTrue(Conflicts.isConflict(CZ, DE));
    }
}
