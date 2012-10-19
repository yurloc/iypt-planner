package org.iypt.planner.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurySeat;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.solver.util.ConstraintComparator;
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
    private String xmlConfig = "/org/iypt/planner/solver/test_config.xml";
    private Tournament solved;
    private List<ConstraintOccurrence> constraintList;
    
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

    public List<ConstraintOccurrence> getConstraintList() {
        return constraintList;
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
        constraintList = getConstraintList(solver, solved);
        Collections.sort(constraintList, new ConstraintComparator());

        // Display the result
        log.info("Solved Tournament:\n{}", toDisplayString(solved));
        log.info("Final score: {}", solved.getScore());
        log.info("Explanation:");
        for (ConstraintOccurrence co : constraintList) {
            log.info(co.toString());
        }
    }

    private static String toDisplayString(Tournament tournament) {
        StringBuilder sb = new StringBuilder(1024);
        for (Round r : tournament.getRounds()) {
            List<Juror> idle = new ArrayList<>();
            List<Juror> away = new ArrayList<>();
            idle.addAll(tournament.getJurors());
            sb.append('\n').append(r).append("\n=========\n");
            sb.append(" Group         |  Jury\n");
            //         A: AA BB CC DD | ...
            for (Group g : r.getGroups()) {
                sb.append(g.getName()).append(": ");
                for (Team t : g.getTeams()) {
                    sb.append(t.getCountry()).append(' ');
                }
                if (g.getSize() == 3) sb.append("   ");
                sb.append("| ");
                for (JurySeat s : tournament.getJurySeats()) {
                    if (s.getJury().equals(g.getJury())) {
                        idle.remove(s.getJuror());
                        Juror juror = s.getJuror();
                        if (s.isChair()) sb.append('[');
                        sb.append(juror == null ? "----" : juror);
                        if (s.isChair()) sb.append(']');
                        sb.append(',');
                    }
                }
                sb.replace(sb.length() - 1, sb.length(), "\n");
            }
            for (DayOff dayOff : tournament.getDayOffs()) {
                if (dayOff.getDay() == r.getDay()) {
                    away.add(dayOff.getJuror());
                }
            }
            idle.removeAll(away); // idle = all -busy -away

            sb.append(String.format("Jurors away (%2d): ", away.size()));
            for (Juror juror : away) {
                sb.append(juror).append(',');
            }
            sb.replace(sb.length() - 1, sb.length(), "\n");

            sb.append(String.format("Jurors idle (%2d): ", idle.size()));
            for (Juror juror : idle) {
                sb.append(juror).append(',');
            }
            sb.replace(sb.length() - 1, sb.length(), "\n");
            sb.append(String.format("Optimal number of independent jurors: %.4f%n", r.getOptimalIndependentCount()));
        }
        int md = tournament.getJurors().size() * tournament.getRounds().size() - tournament.getDayOffs().size();
        sb.append('\n');
        sb.append("Total jury seats:    ").append(tournament.getJurySeats().size()).append('\n');
        sb.append("Total juror mandays: ").append(md).append('\n');
        sb.append(String.format("Optimal juror load:  %.4f%n", tournament.getStatistics().getOptimalLoad()));
        return sb.toString();
    }
    
    private static List<ConstraintOccurrence> getConstraintList(Solver solver, Solution<?> solution) {
        ScoreDirector scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
        scoreDirector.setWorkingSolution(solution.cloneSolution());
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
