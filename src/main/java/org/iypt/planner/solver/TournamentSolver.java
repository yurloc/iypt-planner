package org.iypt.planner.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurySeat;
import org.iypt.planner.domain.Round;
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
    private Solver solver;
    private ScoreDirector scoreDirector;
    // tournament details
    private Map<Round,List<Juror>> idleMap = new HashMap<>();
    private Map<Round,List<Juror>> awayMap = new HashMap<>();

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
        updateDetails();
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

    public List<Juror> getAway(Round round) {
        return awayMap.get(round);
    }

    public List<Juror> getIdle(Round round) {
        return idleMap.get(round);
    }

    private void updateDetails() {
        // TODO refactor me, duplicating some code from Tournament.toDisplayString()
        for (Round r : tournament.getRounds()) {
            List<Juror> idleList = new ArrayList<>();
            List<Juror> awayList = new ArrayList<>();
            idleList.addAll(tournament.getJurors());
            for (Group g : r.getGroups()) {
                for (JurySeat s : tournament.getJurySeats()) {
                    if (s.getJury().equals(g.getJury())) {
                        idleList.remove(s.getJuror());
                    }
                }
            }
            for (DayOff dayOff : tournament.getDayOffs()) {
                if (dayOff.getDay() == r.getDay()) {
                    awayList.add(dayOff.getJuror());
                }
            }
            idleList.removeAll(awayList); // idle = all -busy -away
            awayMap.put(r, awayList);
            idleMap.put(r, idleList);
        }
    }
}
