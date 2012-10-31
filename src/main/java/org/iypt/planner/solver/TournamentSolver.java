package org.iypt.planner.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.drools.ClassObjectFilter;
import org.drools.KnowledgeBase;
import org.drools.WorkingMemory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.io.ResourceFactory;
import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.solver.util.ConstraintComparator;

/**
 *
 * @author jlocker
 */
public class TournamentSolver {
    private SolverFactory solverFactory;
    private Tournament tournament;
    private WeightConfig weightConfig;
    private List<String> constraints;
//    private KnowledgeBase kbase;
    Solver solver;
    ScoreDirector scoreDirector;

    public TournamentSolver(String solverConfigResource) {
        weightConfig = new DefaultWeightConfig();
        // FIXME temporary solution for persistent weights configuration
        try {
            Properties weightProperties = new Properties();
            weightProperties.load(getClass().getResourceAsStream("/weights.properties"));
            for (String key : weightProperties.stringPropertyNames()) {
                weightConfig.setWeight(key, Integer.parseInt(weightProperties.getProperty(key)));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't read weights.properties", ex);
        }

        solverFactory = new XmlSolverFactory(solverConfigResource);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (String string : solverFactory.getSolverConfig().getScoreDirectorFactoryConfig().getScoreDrlList()) {
            kbuilder.add(ResourceFactory.newClassPathResource(string, getClass()), ResourceType.DRL);
            // TODO process kbuilder errors
        }
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        constraints = new ArrayList<>();
        for (KnowledgePackage pkg : kbase.getKnowledgePackages()) {
            for (Rule rule : pkg.getRules()) {
                constraints.add(rule.getName());
            }
        }

        solver = solverFactory.buildSolver();
        scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
        tournament.setWeightConfig(weightConfig);
    }

    public List<String> getScoreDrlList() {
        return solverFactory.getSolverConfig().getScoreDirectorFactoryConfig().getScoreDrlList();
    }

    public WeightConfig getWeightConfig() {
        return weightConfig;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public Score<?> getScore() {
        scoreDirector.setWorkingSolution(tournament);
        return scoreDirector.calculateScore();
    }

    public List<ConstraintOccurrence> getConstraintOccurences() {
        scoreDirector.setWorkingSolution(tournament);
        scoreDirector.calculateScore(); // TODO revise me?

        WorkingMemory workingMemory = ((DroolsScoreDirector) scoreDirector).getWorkingMemory();
        Iterator<?> it = workingMemory.iterateObjects(new ClassObjectFilter(ConstraintOccurrence.class));
        ArrayList<ConstraintOccurrence> occurences = new ArrayList<>();
        while (it.hasNext()) {
            occurences.add((ConstraintOccurrence) it.next());
        }
        Collections.sort(occurences, new ConstraintComparator());
        return occurences;
    }

    public void addEventListener(SolverEventListener solverListener) {
        solver.addEventListener(solverListener);
    }

    public void solve() {
        solver.setPlanningProblem(tournament);
        solver.solve();
        tournament = (Tournament) solver.getBestSolution();
    }

    public void terminateEarly() {
        solver.terminateEarly();
        tournament = (Tournament) solver.getBestSolution();
    }

    public Tournament getTournament() {
        return tournament;
    }
}
