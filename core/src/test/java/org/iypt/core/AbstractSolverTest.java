package org.iypt.core;

import java.util.Iterator;
import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.drools.planner.config.EnvironmentMode;
import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.config.solver.SolverConfig;
import org.drools.planner.config.termination.TerminationConfig;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
import org.drools.planner.core.solution.Solution;
import org.iypt.domain.Group;
import org.iypt.domain.Juror;
import org.iypt.domain.JuryMembership;
import org.iypt.domain.Round;
import org.iypt.domain.Team;
import org.iypt.domain.Tournament;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlocker
 */
public abstract class AbstractSolverTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractSolverTest.class);
    private String xmlConfig = "/org/iypt/core/test_config.xml";
    private Tournament solved;
    
    /**
     * Set a custom solver configuration file. Production configuration is used by default.
     * @param name absolute classpath resource name of the solver configuration file
     */
    void setXmlConfig(String name) {
        xmlConfig = name;
    }
    
    Tournament getBestSolution() {
        return solved;
    }
    
    abstract TerminationConfig getTerminationConfig();

    abstract Tournament getInitialSolution();
    
    @Before
    public void solveInitialSolution() {
        // Build the Solver
        SolverFactory solverFactory = new XmlSolverFactory(xmlConfig);
        SolverConfig solverConfig = solverFactory.getSolverConfig();
        solverConfig.setTerminationConfig(getTerminationConfig());
        // Do not allow production environment for tests
        if (EnvironmentMode.PRODUCTION == solverConfig.getEnvironmentMode()) {
            solverConfig.setEnvironmentMode(EnvironmentMode.REPRODUCIBLE);
        }
        log.info("EnvironmentMode: {}", solverConfig.getEnvironmentMode());
        Solver solver = solverFactory.buildSolver();

        // Get the initial solution
        Tournament unsolved = getInitialSolution();
        Assert.assertTrue(unsolved.isFeasibleSolutionPossible());

        // Solve the problem
        solver.setPlanningProblem(unsolved);
        solver.solve();
        solved = (Tournament) solver.getBestSolution();

        // Display the result
        log.info("Solved Tournament:\n{}", toDisplayString(solved));
        log.info("Final score: {}", solved.getScore());
        log.info("Explanation:\n{}", getConstraintsAsLines(solver, solved));
        
    }

    private static String toDisplayString(Tournament tournament) {
        StringBuilder sb = new StringBuilder(1024);
        for (Round r : tournament.getRounds()) {
            sb.append(r).append("\n=========\n");
            sb.append(" group     | jury\n");
            //         A: A B C D | ...
            for (Group g : r.getGroups()) {
                sb.append(g.getName()).append(": ");
                for (Team t : g.getTeams()) {
                    sb.append(t.getCountry()).append(' ');
                }
                if (g.getSize() == 3) sb.append("  ");
                sb.append("| ");
                for (JuryMembership m : tournament.getJuryMemberships()) {
                    if (m.getJury().equals(g.getJury())) {
                        Juror juror = m.getJuror();
                        sb.append(juror == null ? "[---]" : juror.getCountry());
                    }
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }
    
    private static String getConstraintsAsLines(Solver solver, Solution<?> solution) {
        ScoreDirector scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
        scoreDirector.setWorkingSolution(solution.cloneSolution());
        scoreDirector.calculateScore();
        
        WorkingMemory workingMemory = ((DroolsScoreDirector) scoreDirector).getWorkingMemory();
        Iterator<?> it = workingMemory.iterateObjects(new ClassObjectFilter(ConstraintOccurrence.class));
        StringBuilder sb = new StringBuilder(1024);
        while (it.hasNext()) {
            sb.append(it.next()).append('\n');
        }
        return sb.toString();
    }
}
