package org.iypt.planner.opta.drools.solver;

/**
 * This interface marks tests that use real Solver to test scoring rules, which makes them slow.
 * It should be used as a JUnit Category to make it possible to exclude this tests in Maven Surefire Plugin configuration.
 */
public interface SolverTest {

}
