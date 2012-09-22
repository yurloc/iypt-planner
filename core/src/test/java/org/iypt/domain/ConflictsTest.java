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
        System.out.println("addConflict");
//        Country CZE = new Country("CZE", "Czech Republic");
//        Country SVK = new Country("SVK", "Slovakia");
//        Country GER = new Country("GER", "Germany");
        
        assertFalse("Empty conflicts test failed", Conflicts.isConflict(Country.CZE, Country.SVK));
        
        assertTrue("Adding cze-svk conflict should succeed", Conflicts.addConflict(Country.CZE, Country.SVK));
        assertFalse("Adding cze-svk conflict should not succeed", Conflicts.addConflict(Country.CZE, Country.SVK));
        assertFalse("Adding svk-cze conflict should not succeed", Conflicts.addConflict(Country.SVK, Country.CZE));
        
        assertTrue(Conflicts.isConflict(Country.CZE, Country.SVK));
        assertTrue(Conflicts.isConflict(Country.SVK, Country.CZE));
        
        assertFalse(Conflicts.isConflict(Country.CZE, Country.CZE));
        assertFalse(Conflicts.isConflict(Country.CZE, Country.GER));
        assertFalse(Conflicts.isConflict(Country.SVK, Country.GER));
        assertFalse(Conflicts.isConflict(Country.GER, Country.GER));
        
        assertTrue(Conflicts.addConflict(Country.GER, Country.SVK));
        
        assertTrue(Conflicts.isConflict(Country.GER, Country.SVK));
        assertTrue(Conflicts.isConflict(Country.GER, Country.CZE));
        assertTrue(Conflicts.isConflict(Country.SVK, Country.GER));
        assertTrue(Conflicts.isConflict(Country.CZE, Country.GER));
    }

    /**
     * Test of isConflict method, of class Conflicts.
     */
    public void xtestIsConflict() {
        System.out.println("isConflict");
        Country c1 = null;
        Country c2 = null;
        boolean expResult = false;
        boolean result = Conflicts.isConflict(c1, c2);
        assertEquals(expResult, result);
        // Country.TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
