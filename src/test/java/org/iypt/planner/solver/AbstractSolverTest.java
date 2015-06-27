package org.iypt.planner.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.solver.util.ConstraintComparator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.XmlSolverFactory;
import org.optaplanner.core.config.termination.TerminationConfig;
import org.optaplanner.core.impl.score.constraint.ConstraintOccurrence;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;
import org.optaplanner.core.impl.solution.Solution;
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

        KieSession kieSession = ((DroolsScoreDirector) scoreDirector).getKieSession();
        Collection<ConstraintOccurrence> constraintOccurrences
                = (Collection<ConstraintOccurrence>) kieSession.getObjects(new ClassObjectFilter(ConstraintOccurrence.class));
        ArrayList<ConstraintOccurrence> arrayList = new ArrayList<>();
        for (ConstraintOccurrence co : constraintOccurrences) {
            arrayList.add(co);
        }
        return arrayList;
    }
}
