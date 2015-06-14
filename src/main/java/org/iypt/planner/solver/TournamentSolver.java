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
import org.drools.planner.config.EnvironmentMode;
import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.constraint.ConstraintType;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.drools.planner.core.score.constraint.UnweightedConstraintOccurrence;
import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
import org.iypt.planner.domain.Absence;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorLoad;
import org.iypt.planner.domain.Lock;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.gui.Constraint;
import org.iypt.planner.gui.JurorAssignment;
import org.iypt.planner.gui.JurorInfo;
import org.iypt.planner.gui.ScheduleModel;
import org.iypt.planner.gui.SeatInfo;
import org.iypt.planner.solver.util.ConstraintComparator;

import static org.iypt.planner.Constants.CONSTRAINT_TYPE_HARD;
import static org.iypt.planner.Constants.CONSTRAINT_TYPE_KEY;
import static org.iypt.planner.Constants.CONSTRAINT_TYPE_SOFT;

/**
 *
 * @author jlocker
 */
public class TournamentSolver {

    private Tournament tournament;
    private WeightConfig weightConfig;
    private List<ConstraintOccurrence> constraintRules;
    private SolverFactory solverFactory;
    private EnvironmentMode environmentMode;
    private Solver solver;
    private SolverEventListener listener;
    private ScoreDirector scoreDirector;
    private boolean solving;

    public TournamentSolver(String solverConfigResource, SolverEventListener listener) {
        this.listener = listener;
        weightConfig = new DefaultWeightConfig();
        // FIXME temporary solution for persistent weights configuration
        try {
            Properties weightProperties = new Properties();
            weightProperties.load(TournamentSolver.class.getResourceAsStream("/weights.properties"));
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
                if (rule.getMetaData().containsKey(CONSTRAINT_TYPE_KEY)) {
                    String type = (String) rule.getMetaData().get(CONSTRAINT_TYPE_KEY);
                    ConstraintOccurrence co;
                    switch (type) {
                        case CONSTRAINT_TYPE_HARD:
                            co = new UnweightedConstraintOccurrence(rule.getName(), ConstraintType.NEGATIVE_HARD);
                            break;
                        case CONSTRAINT_TYPE_SOFT:
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

        environmentMode = solverFactory.getSolverConfig().getEnvironmentMode();
        // initial scoreDirector
        scoreDirector = solverFactory.buildSolver().getScoreDirectorFactory().buildScoreDirector();
    }

    public ScheduleModel setTournament(Tournament tournament) {
        this.tournament = tournament;
        // TODO only set weightConfig when setting a fresh tournament
        this.tournament.setWeightConfig(weightConfig);
        return updateDetails();
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

    public void setEnvironmentMode(String mode) {
        environmentMode = EnvironmentMode.valueOf(mode);
    }

    public EnvironmentMode getEnvironmentMode() {
        return environmentMode;
    }

    public ScheduleModel solve() {
        solving = true;
        scoreDirector = solverFactory.buildSolver().getScoreDirectorFactory().buildScoreDirector();
        solverFactory.getSolverConfig().setEnvironmentMode(environmentMode);
        solver = solverFactory.buildSolver();
        solver.addEventListener(listener);
        solver.setPlanningProblem(tournament);
        solver.solve();
        solving = false;
        return setTournament((Tournament) solver.getBestSolution());
    }

    public void terminateEarly() {
        solver.terminateEarly();
    }

    public Tournament getTournament() {
        return tournament;
    }

    // TODO refactor me, duplicating some code from Tournament.toDisplayString()
    private ScheduleModel updateDetails() {
        scoreDirector.setWorkingSolution(tournament);
        scoreDirector.calculateScore();
        WorkingMemory workingMemory = ((DroolsScoreDirector) scoreDirector).getWorkingMemory();
        Iterator<?> it;

        // collect juror loads (based on jury assignments)
        Map<Juror, JurorLoad> loadMap = new HashMap<>();
        it = workingMemory.iterateObjects(new ClassObjectFilter(JurorLoad.class));
        while (it.hasNext()) {
            JurorLoad load = (JurorLoad) it.next();
            loadMap.put(load.getJuror(), load);
        }

        // prepare ConstraintOccurence map (constraint rule -> occurences)
        HashMap<String, List<Constraint>> coMap = new HashMap<>();
        for (ConstraintOccurrence constraintId : constraintRules) {
            coMap.put(constraintId.getRuleId(), new ArrayList<Constraint>());
        }

        // collect constraint occurences
        it = workingMemory.iterateObjects(new ClassObjectFilter(ConstraintOccurrence.class));
        while (it.hasNext()) {
            ConstraintOccurrence co = (ConstraintOccurrence) it.next();
            coMap.get(co.getRuleId()).add(new Constraint(co));
        }

        return new ScheduleModel(tournament, coMap, loadMap);
    }

    public boolean isSolving() {
        return solving;
    }

    public ScheduleModel applyChanges(JurorInfo jurorInfo) {
        Juror juror = jurorInfo.getJuror();
        for (JurorAssignment assignment : jurorInfo.getSchedule()) {
            if (assignment.isDirty()) {

                // no matter what the change is...
                if (assignment.getOriginalStatus() == JurorAssignment.Status.ASSIGNED) {
                    // empty the seat
                    for (Seat seat : tournament.getSeats()) {
                        if (seat.getJuror() == juror && seat.getJury().getGroup().getRound().equals(assignment.getRound().getRound())) {
                            seat.setJuror(null);
                        }
                    }
                } else if (assignment.getOriginalStatus() == JurorAssignment.Status.AWAY) {
                    // cancel absence
                    ArrayList<Absence> cancelled = new ArrayList<>();
                    for (Absence absence : tournament.getAbsences(juror)) {
                        if (absence.getRound().equals(assignment.getRound().getRound())) {
                            cancelled.add(absence);
                        }
                    }
                    tournament.removeAbsences(cancelled);
                }

                // no matter what the original status is
                if (assignment.getCurrentStatus() == JurorAssignment.Status.AWAY) {
                    // add absence
                    tournament.addAbsences(new Absence(juror, assignment.getRound().getRound()));
                }
            }
        }
        return updateDetails();
    }

    public void lockSeat(SeatInfo seatInfo) {
        tournament.lock(seatInfo.getSeat());
    }

    public void unlockSeat(SeatInfo seatInfo) {
        tournament.unlock(seatInfo.getSeat());
    }

    public ScheduleModel requestRoundLockChange(Round round) {
        if (!tournament.isLocked(round)) {
            // lock rounds up to this
            for (Round r : tournament.getRounds()) {
                if (r.getNumber() <= round.getNumber()) {
                    tournament.lock(r);
                }
            }
            tournament.setOriginal((Tournament) tournament.cloneSolution());
        } else {
            // unlock all rounds
            for (Round r : tournament.getRounds()) {
                tournament.unlock(r);
            }
            tournament.setOriginal(null);
        }
        // FIXME the update is probably not necessary
        return updateDetails();
    }

    public void unlockRound(Round round) {
        tournament.unlock(round);
    }

    // might be used later for soft-locking
    protected int getLockStatus(SeatInfo seatInfo) {
        for (Lock lock : tournament.getLocks()) {
            if (lock.matches(seatInfo.getSeat())) {
                if (lock.getJuror() == seatInfo.getJuror()) {
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
        return tournament.getJuryCapacity();
    }

    public ScheduleModel changeJuryCapacity(Integer capacity) {
        tournament.setJuryCapacity(capacity);
        return updateDetails();
    }

    public ScheduleModel clearSchedule() {
        tournament.clear();
        return updateDetails();
    }
}
