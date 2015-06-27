package org.iypt.planner.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
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
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.solver.util.ConstraintComparator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author jlocker
 */
@Category(SolverTest.class)
public abstract class AbstractSolverTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractSolverTest.class);
    private static WeightConfig weightConfig;
    private String xmlConfig = "/org/iypt/planner/solver/test_config.xml";
    private Tournament solved;
    private List<ConstraintOccurrence> constraintList;

    /**
     * Set a custom solver configuration file. Production configuration is used by default.
     *
     * @param name absolute classpath resource name of the solver configuration file
     */
    void setXmlConfig(String name) {
        xmlConfig = name;
    }

    Tournament getBestSolution() {
        return solved;
    }

    public List<ConstraintOccurrence> getConstraintList() {
        return constraintList;
    }

    abstract TerminationConfig getTerminationConfig();

    abstract Tournament getInitialSolution();

    @BeforeClass
    public static void initWeights() throws IOException {
        weightConfig = new DefaultWeightConfig();
        Properties weightProperties = new Properties();
        weightProperties.load(AbstractSolverTest.class.getResourceAsStream("/weights.properties"));
        for (String key : weightProperties.stringPropertyNames()) {
            weightConfig.setWeight(key, Integer.parseInt(weightProperties.getProperty(key)));
        }
    }

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
        assertThat(unsolved.isFeasibleSolutionPossible()).isTrue();
        unsolved.setWeightConfig(weightConfig);

        // Solve the problem
        solver.setPlanningProblem(unsolved);
        solver.solve();
        solved = (Tournament) solver.getBestSolution();
        constraintList = getConstraintList(solver, solved);
        Collections.sort(constraintList, new ConstraintComparator());

        // Display the result
        log.info("Solved Tournament:\n{}", solved.toDisplayString());
        log.info("Final score: {}", solved.getScore());
        log.info("Explanation:");
        for (ConstraintOccurrence co : constraintList) {
            log.info(co.toString());
        }
    }

    private static List<ConstraintOccurrence> getConstraintList(Solver solver, Solution<?> solution) {
        ScoreDirector scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
        scoreDirector.setWorkingSolution(((Tournament) solution).cloneSolution());
        scoreDirector.calculateScore();

        WorkingMemory workingMemory = ((DroolsScoreDirector) scoreDirector).getWorkingMemory();
        Iterator<?> it = workingMemory.iterateObjects(new ClassObjectFilter(ConstraintOccurrence.class));
        ArrayList<ConstraintOccurrence> arrayList = new ArrayList<>();
        while (it.hasNext()) {
            arrayList.add((ConstraintOccurrence) it.next());
        }
        return arrayList;
    }
}
