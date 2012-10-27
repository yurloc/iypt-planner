package org.iypt.planner.gui;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.phase.event.SolverPhaseLifecycleListenerAdapter;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.CSVTournamentFactory;
import org.iypt.planner.solver.DefaultWeightConfig;

/**
 *
 * @author jlocker
 */
public class PlannerWindow extends Window implements Bindable {

    // constraints config tab controls
    @BXML private Label drlLabel;
    @BXML private ConstraintsConfig constraintConfig;

    // planning tab controls
    @BXML private Label scoreLabel;
    @BXML private PushButton nextButton;
    @BXML private PushButton terminateButton;
    @BXML private TablePane roundHolder;

    // other
    private Tournament tournament;
    private SolverTask solverTask;
    private List<RoundView> roundViews = new ArrayList<>();
    private BlockingQueue<Tournament> betterSolutionQueue = new ArrayBlockingQueue<>(1);

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        try {
            tournament = getInitialSolutionFromCSV();
            updateRounds();
        } catch (IOException ex) {
//            Alert.alert(MessageType.ERROR, ex.getMessage(), PlannerWindow.this);
            ex.printStackTrace();
        }

        final Solver solver = newSolver();
        tournament.setWeightConfig(constraintConfig.getWeightConfig());

        terminateButton.setEnabled(false);

        nextButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
//                Alert.alert(MessageType.INFO, "You clicked me!", PlannerWindow.this);
                button.setEnabled(false);
                terminateButton.setEnabled(true);
                solverTask = new SolverTask(solver, tournament);
                TaskListener<String> taskListener = new TaskListener<String>() {
                    @Override
                    public void taskExecuted(Task<String> task) {
//                        activityIndicator.setActive(false);
                        nextButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        scoreLabel.setText(task.getResult());
                    }

                    @Override
                    public void executeFailed(Task<String> task) {
//                        activityIndicator.setActive(false);
                        nextButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        scoreLabel.setText(task.getFault().toString());
                    }
                };
                pullScore();
                solverTask.execute(new TaskAdapter<>(taskListener));
            }
        });

        terminateButton.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                solverTask.terminate();
            }
        });
    }

    private void pullScore() {
        TaskListener<Tournament> taskListener = new TaskListener<Tournament>() {
            @Override
            public void taskExecuted(Task<Tournament> task) {
                Tournament t = task.getResult();
                if (t != null) {
                    scoreLabel.setText(t.getScore().toString());
                    tournament = t;
                    updateRounds();
                }
            }

            @Override
            public void executeFailed(Task<Tournament> task) {
                scoreLabel.setText(task.getFault().toString());
            }
        };
        // TODO maintain list of running tasks?
        new PullSolutionTask().execute(new TaskAdapter<>(taskListener));
    }

    private Tournament getInitialSolutionFromCSV() throws IOException {
        String path = "/org/iypt/planner/csv/";
        CSVTournamentFactory factory = new CSVTournamentFactory(PlannerWindow.class,
                path + "team_data.csv",
                path + "jury_data.csv",
                path + "schedule2012.csv");
        Tournament t = factory.newTournament();
        t.setJuryCapacity(6);
        return t;
    }

    private void updateRounds() {
        if (roundHolder.getRows().getLength() > 0)
        roundHolder.getRows().remove(0, roundHolder.getRows().getLength());
        for (Round round : tournament.getRounds()) {
            RoundView roundView = new RoundView();
            TablePane.Row row = new TablePane.Row();
            row.add(roundView);
            roundHolder.getRows().add(row);
            roundViews.add(roundView);
            roundView.update(tournament, round);
        }
    }

    private Solver newSolver() {
        SolverFactory solverFactory = new XmlSolverFactory("/org/iypt/planner/solver/config.xml");
        drlLabel.setText(solverFactory.getSolverConfig().getScoreDirectorFactoryConfig().getScoreDrlList().get(0));
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//        kbuilder.add(ResourceFactory.newClassPathResource(drlLabel.getText()), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newClassPathResource("org/iypt/planner/solver/score_rules.drl"), ResourceType.DRL);
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();

        KnowledgePackage pkg = kbase.getKnowledgePackages().iterator().next();

        constraintConfig.setWconfig(new DefaultWeightConfig());
        constraintConfig.addConstraintsFromRules(pkg.getRules());

        // build a solver
        Solver solver = solverFactory.buildSolver();
        solver.addEventListener(new SolverListener());
        return solver;
    }

    //=========================================================================================================================
    // listeners
    //=========================================================================================================================

    private class SolverListener implements SolverEventListener {

        @Override
        public void bestSolutionChanged(BestSolutionChangedEvent event) {
            tournament = (Tournament) event.getNewBestSolution();
            betterSolutionQueue.offer(tournament);
        }

    }

    private class PhaseListener extends SolverPhaseLifecycleListenerAdapter {

        @Override
        public void stepTaken(AbstractStepScope stepScope) {
            super.stepTaken(stepScope);
        }

    }

    //=========================================================================================================================
    // tasks
    //=========================================================================================================================

    private class PullSolutionTask extends Task<Tournament> {

        @Override
        public Tournament execute() throws TaskExecutionException {
            try {
                return betterSolutionQueue.take();
            } catch (InterruptedException ex) {
                // TODO process the exception
            } finally {
                pullScore();
            }
            return null;
        }

    }
    private class SolverTask extends Task<String> {

        private final Solver solver;
        private final Tournament tournament;

        public SolverTask(Solver solver, Tournament tournament) {
            this.solver = solver;
            this.tournament = tournament;
        }

        public void terminate() {
            solver.terminateEarly();
        }

        @Override
        public String execute() throws TaskExecutionException {
            solver.setPlanningProblem(tournament);
            solver.solve();
            Tournament solved = (Tournament) solver.getBestSolution();
            return solved.getScore().toString();
        }

    }
}
