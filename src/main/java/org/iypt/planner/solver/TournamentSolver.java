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
import org.drools.planner.core.score.constraint.ConstraintType;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.drools.planner.core.score.constraint.UnweightedConstraintOccurrence;
import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
import org.iypt.planner.domain.Conflict;
import org.iypt.planner.domain.CountryCode;
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorLoad;
import org.iypt.planner.domain.JurySeat;
import org.iypt.planner.domain.Lock;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.gui.GroupRoster;
import org.iypt.planner.gui.GroupRoster.JurorRow;
import org.iypt.planner.gui.JurorDay;
import org.iypt.planner.solver.util.ConstraintComparator;

/**
 *
 * @author jlocker
 */
public class TournamentSolver {

    private SolverFactory solverFactory;
    private Tournament tournament;
    private WeightConfig weightConfig;
    private List<ConstraintOccurrence> constraintRules;
    private List<ConstraintOccurrence> constraintOccurences;
    private Solver solver;
    private ScoreDirector scoreDirector;
    private boolean solving;
    // tournament details
    private Map<Round, List<Juror>> idleMap = new HashMap<>();
    private Map<Round, List<Juror>> awayMap = new HashMap<>();
    private Map<Juror, List<CountryCode>> conflictMap = new HashMap<>();
    private Map<Juror, JurorLoad> loadMap = new HashMap<>();
    private Map<Juror, List<JurorDay>> jurorDayMap = new HashMap<>();

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
        constraintRules = new ArrayList<>();
        for (KnowledgePackage pkg : kbase.getKnowledgePackages()) {
            for (Rule rule : pkg.getRules()) {
                String key = "ConstraintType";
                if (rule.getMetaData().containsKey(key)) {
                    String type = (String) rule.getMetaData().get(key);
                    ConstraintOccurrence co;
                    switch (type) {
                        case "hard":
                            co = new UnweightedConstraintOccurrence(rule.getName(), ConstraintType.NEGATIVE_HARD);
                            break;
                        case "soft":
                            co = new IntConstraintOccurrence(rule.getName(), ConstraintType.NEGATIVE_SOFT);
                            break;
                        default:
                            throw new AssertionError();
                    }
                    constraintRules.add(co);
                }
            }
        }
        Collections.sort(constraintRules, new ConstraintComparator());

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

    public List<ConstraintOccurrence> getConstraints() {
        return Collections.unmodifiableList(constraintRules);
    }

    public Score<?> getScore() {
        if (tournament.getScore() != null) {
            return tournament.getScore();
        }
        scoreDirector.setWorkingSolution(tournament.cloneSolution());
        return scoreDirector.calculateScore();
    }

    public List<ConstraintOccurrence> getConstraintOccurences() {
        return Collections.unmodifiableList(constraintOccurences);
    }

    public void addEventListener(SolverEventListener solverListener) {
        solver.addEventListener(solverListener);
    }

    public void solve() {
        solving = true;
        solver.setPlanningProblem(tournament);
        solver.solve();
        solving = false;
        setTournament((Tournament) solver.getBestSolution());
    }

    public void terminateEarly() {
        solver.terminateEarly();
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

    public List<GroupRoster.JurorRow> getAwayRows(Round round) {
        // FIXME temporary solution
        List<GroupRoster.JurorRow> list = new ArrayList<>();
        for (Juror j : getAway(round)) {
            list.add(new GroupRoster.JurorRow(j));
        }
        return list;
    }

    public List<GroupRoster.JurorRow> getIdleRows(Round round) {
        // FIXME temporary solution
        List<GroupRoster.JurorRow> list = new ArrayList<>();
        for (Juror j : getIdle(round)) {
            list.add(new GroupRoster.JurorRow(j));
        }
        return list;
    }

    public List<CountryCode> getConflicts(Juror juror) {
        return conflictMap.get(juror);
    }

    public JurorLoad getLoad(Juror juror) {
        return loadMap.get(juror);
    }

    public List<JurorDay> getJurorDays(Juror juror) {
        return jurorDayMap.get(juror);
    }

    // TODO refactor me, duplicating some code from Tournament.toDisplayString()
    private void updateDetails() {
        for (Juror juror : tournament.getJurors()) {
            ArrayList<JurorDay> days = new ArrayList<>(tournament.getRounds().size());
            jurorDayMap.put(juror, days);
            for (Round round : tournament.getRounds()) {
                // idle all days by default
                days.add(round.getNumber() - 1, new JurorDay(round, true));
            }
        }
        // collect the lists of idle and away jurors per round
        for (Round round : tournament.getRounds()) {
            List<Juror> idleList = new ArrayList<>();
            List<Juror> awayList = new ArrayList<>();
            idleList.addAll(tournament.getJurors());
            for (JurySeat seat : tournament.getJurySeats()) {
                if (seat.getJuror() != Juror.NULL && seat.getJury().getGroup().getRound().equals(round)) {
                    idleList.remove(seat.getJuror());
                    jurorDayMap.get(seat.getJuror()).set(round.getNumber() - 1, new JurorDay(seat.getJury().getGroup()));
                }
            }
            for (DayOff dayOff : tournament.getDayOffs()) {
                if (dayOff.getDay() == round.getDay()) {
                    awayList.add(dayOff.getJuror());
                    jurorDayMap.get(dayOff.getJuror()).set(round.getNumber() - 1, new JurorDay(round, false));
                }
            }
            idleList.removeAll(awayList); // idle = all -busy -away
            awayMap.put(round, awayList);
            idleMap.put(round, idleList);
        }

        // collect conflicts per juror
        conflictMap.clear();
        for (Conflict conflict : tournament.getConflicts()) {
            List<CountryCode> ccList = conflictMap.get(conflict.getJuror());
            if (ccList == null) {
                // most jurors have exactly 1 conflict country
                ccList = new ArrayList<>(1);
                conflictMap.put(conflict.getJuror(), ccList);
            }
            ccList.add(conflict.getCountry());
        }

        scoreDirector.setWorkingSolution(tournament.cloneSolution());
        scoreDirector.calculateScore();
        WorkingMemory workingMemory = ((DroolsScoreDirector) scoreDirector).getWorkingMemory();
        Iterator<?> it;

        // collect juror loads (based on jury assignments)
        it = workingMemory.iterateObjects(new ClassObjectFilter(JurorLoad.class));
        while (it.hasNext()) {
            JurorLoad load = (JurorLoad) it.next();
            loadMap.put(load.getJuror(), load);
        }

        // collect constraint occurences
        it = workingMemory.iterateObjects(new ClassObjectFilter(ConstraintOccurrence.class));
        constraintOccurences = new ArrayList<>();
        while (it.hasNext()) {
            constraintOccurences.add((ConstraintOccurrence) it.next());
        }
    }

    public boolean isSolving() {
        return solving;
    }

    public void applyChanges(Juror juror) {
        for (JurorDay day : jurorDayMap.get(juror)) {
            if (day.isDirty()) {

                // no matter what the change is...
                if (day.getStatus() == JurorDay.Status.ASSIGNED) {
                    // empty the seat
                    for (JurySeat seat : tournament.getJurySeats()) {
                        if (seat.getJuror() == juror && seat.getJury().getGroup().getRound().getDay() == day.getRound().getDay()) {
                            seat.setJuror(Juror.NULL);
                        }
                    }
                } else if (day.getStatus() == JurorDay.Status.AWAY) {
                    // cancel day off
                    ArrayList<DayOff> cancelled = new ArrayList<>();
                    for (DayOff dayOff : tournament.getDayOffs()) {
                        // FIXME this would cancel multiple day offs if there were multiple rounds in one day
                        if (dayOff.getJuror() == juror && dayOff.getDay() == day.getRound().getDay()) {
                            cancelled.add(dayOff);
                        }
                    }
                    tournament.getDayOffs().removeAll(cancelled);
                }

                // no matter what the original state is
                if (day.getChange() == JurorDay.Status.AWAY) {
                    // add day off
                    tournament.addDayOffs(new DayOff(juror, day.getRound().getDay()));
                }

                day.applyChange();
            }
        }
        updateDetails();
    }

    public void lockJuror(JurorRow row) {
        tournament.addLock(new Lock(row.getJuror(), row.getSeat().getJury(), row.getSeat().getPosition()));
    }

    public void unlockJuror(JurorRow row) {
        Lock removed = null;
        for (Lock lock : tournament.getLocks()) {
            if (lock.matches(row.getSeat())) {
                removed = lock;
            }
        }
        if (removed != null) {
            tournament.removeLock(removed);
        }
    }

    public int getLockStatus(JurorRow row) {
        for (Lock lock : tournament.getLocks()) {
            if (lock.matches(row.getSeat())) {
                if (lock.getJuror() == row.getJuror()) {
                    return 1;
                } else {
                    return 2;
                }
            }
        }
        return 0;
    }

    public Round getRound(int roundNumber) {
        return tournament.getRounds().get(roundNumber);
    }

    public int getJuryCapacity() {
        return tournament.getJuries().get(0).getCapacity();
    }
}
