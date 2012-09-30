package org.iypt.domain;

import junit.framework.TestCase;

/**
 *
 * @author jlocker
 */
public class ConflictsTest extends TestCase {
    
    public ConflictsTest(String testName) {
        super(testName);
    }

    /**
     * Test of addConflict method, of class Conflicts.
     */
    public void testAddConflict() {
        assertFalse("Empty conflicts test failed", Conflicts.isConflict(CountryCode.CZ, CountryCode.SK));
        
        assertTrue("Adding cz-sk conflict should succeed", Conflicts.addConflict(CountryCode.CZ, CountryCode.SK));
        assertFalse("Adding cz-sk conflict should not succeed", Conflicts.addConflict(CountryCode.CZ, CountryCode.SK));
        assertFalse("Adding sk-cz conflict should not succeed", Conflicts.addConflict(CountryCode.SK, CountryCode.CZ));
        
        assertTrue(Conflicts.isConflict(CountryCode.CZ, CountryCode.SK));
        assertTrue(Conflicts.isConflict(CountryCode.SK, CountryCode.CZ));
        
        assertFalse(Conflicts.isConflict(CountryCode.CZ, CountryCode.CZ));
        assertFalse(Conflicts.isConflict(CountryCode.CZ, CountryCode.DE));
        assertFalse(Conflicts.isConflict(CountryCode.SK, CountryCode.DE));
        assertFalse(Conflicts.isConflict(CountryCode.DE, CountryCode.DE));
        
        assertTrue(Conflicts.addConflict(CountryCode.DE, CountryCode.SK));
        
        assertTrue(Conflicts.isConflict(CountryCode.DE, CountryCode.SK));
        assertTrue(Conflicts.isConflict(CountryCode.DE, CountryCode.CZ));
        assertTrue(Conflicts.isConflict(CountryCode.SK, CountryCode.DE));
        assertTrue(Conflicts.isConflict(CountryCode.CZ, CountryCode.DE));
    }

    /**
     * Test of isConflict method, of class Conflicts.
     */
    public void xtestIsConflict() {
        System.out.println("isConflict");
        CountryCode c1 = null;
        CountryCode c2 = null;
        boolean expResult = false;
        boolean result = Conflicts.isConflict(c1, c2);
        assertEquals(expResult, result);
        // CountryCode.TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
