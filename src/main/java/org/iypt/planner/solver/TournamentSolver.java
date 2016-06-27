package org.iypt.planner.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.iypt.planner.domain.Absence;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorLoad;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.gui.Constraint;
import org.iypt.planner.gui.JurorAssignment;
import org.iypt.planner.gui.JurorInfo;
import org.iypt.planner.gui.ScheduleModel;
import org.iypt.planner.gui.SeatInfo;
import org.iypt.planner.solver.util.ConstraintComparator;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

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
    private List<ConstraintRule> constraintRules;
    private SolverFactory solverFactory;
    private EnvironmentMode environmentMode;
    private Solver solver;
    private SolverEventListener<Tournament> listener;
    private ScoreDirector scoreDirector;
    private boolean solving;

    public TournamentSolver(String solverConfigResource, SolverEventListener<Tournament> listener) {
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

        solverFactory = SolverFactory.createFromXmlResource(solverConfigResource);
        KieServices kieServices = KieServices.Factory.get();
        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.writeKModuleXML(kieModuleModel.toXML());
        for (String drlResource : solverFactory.getSolverConfig().getScoreDirectorFactoryConfig().getScoreDrlList()) {
            kfs.write(kieServices.getResources().newClassPathResource(drlResource).setResourceType(ResourceType.DRL));
        }
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            throw new IllegalStateException(errors.toString());
        }
        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        constraintRules = new ArrayList<>();
        for (KiePackage pkg : kieContainer.getKieBase().getKiePackages()) {
            for (Rule rule : pkg.getRules()) {
                if (rule.getMetaData().containsKey(CONSTRAINT_TYPE_KEY)) {
                    String type = (String) rule.getMetaData().get(CONSTRAINT_TYPE_KEY);
                    if (!CONSTRAINT_TYPE_HARD.equals(type) && !CONSTRAINT_TYPE_SOFT.equals(type)) {
                        throw new IllegalStateException("Unexpected constraint type: " + type);
                    }
                    constraintRules.add(new ConstraintRule(rule.getName(), type));
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

    public List<ConstraintRule> getConstraints() {
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
        solver.solve(tournament);
        solving = false;
        return setTournament((Tournament) solver.getBestSolution());
    }

    public void terminateEarly() {
        solver.terminateEarly();
    }

    public Tournament getTournament() {
        return tournament;
    }

    @SuppressWarnings("unchecked")
    private Collection<JurorLoad> getLoads(DroolsScoreDirector scoreDirector) {
        return (Collection<JurorLoad>) scoreDirector.getKieSession().getObjects(new ClassObjectFilter(JurorLoad.class));
    }

    // TODO refactor me, duplicating some code from Tournament.toDisplayString()
    private ScheduleModel updateDetails() {
        scoreDirector.setWorkingSolution(tournament);
        scoreDirector.calculateScore();

        // collect juror loads (based on jury assignments)
        Map<Juror, JurorLoad> loadMap = new HashMap<>();
        for (JurorLoad load : getLoads((DroolsScoreDirector) scoreDirector)) {
            loadMap.put(load.getJuror(), load);
        }

        // prepare ConstraintOccurence map (constraint rule -> occurences)
        HashMap<String, List<Constraint>> coMap = new HashMap<>();
        for (ConstraintRule constraintRule : constraintRules) {
            coMap.put(constraintRule.getName(), new ArrayList<Constraint>());
        }

        for (ConstraintMatchTotal cmt : scoreDirector.getConstraintMatchTotals()) {
            for (ConstraintMatch cm : cmt.getConstraintMatchSet()) {
                coMap.get(cm.getConstraintName()).add(new Constraint(cm));
            }
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
            tournament.setOriginal((Tournament) tournament.makeBackup());
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

    public Round getRound(int roundNumber) {
        return tournament.getRounds().get(roundNumber);
    }

    public ScheduleModel changeJurySize(Round round, int newSize) {
        tournament.changeJurySize(round, newSize);
        return updateDetails();
    }

    public ScheduleModel clearSchedule() {
        tournament.clear();
        return updateDetails();
    }
}
