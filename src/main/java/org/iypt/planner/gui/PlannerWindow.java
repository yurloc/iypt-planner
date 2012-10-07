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
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.phase.event.SolverPhaseLifecycleListenerAdapter;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.CSVTournamentFactory;
import org.iypt.planner.domain.util.RoundFactory;

import static org.iypt.planner.domain.util.SampleFacts.*;
/**
 *
 * @author jlocker
 */
public class PlannerWindow extends Window implements Bindable {

    private Label scoreLabel;
    private PushButton nextButton;
    private PushButton terminateButton;
    @BXML private TablePane roundHolder;
    private Tournament tournament;
    private SolverTask solverTask;
    private List<RoundView> roundViews = new ArrayList<>();
    private BlockingQueue<Tournament> betterSolutionQueue = new ArrayBlockingQueue<>(1);

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        scoreLabel = (Label) namespace.get("scoreLabel");
        nextButton = (PushButton) namespace.get("nextButton");
        terminateButton = (PushButton) namespace.get("terminateButton");
        try {
            //        tournament = getInitialSolution();
            tournament = getInitialSolutionFromCSV();
            updateRounds();
        } catch (IOException ex) {
//            Alert.alert(MessageType.ERROR, ex.getMessage(), PlannerWindow.this);
            ex.printStackTrace();
        }

        updateRounds();

        terminateButton.setEnabled(false);

        nextButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
//                Alert.alert(MessageType.INFO, "You clicked me!", PlannerWindow.this);
                button.setEnabled(false);
                terminateButton.setEnabled(true);
                solverTask = new SolverTask(newSolver(), tournament);
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

    private Tournament getInitialSolution() {
        Round r1 = RoundFactory.createRound(1, gABC, gDEF, gGHI);
        Round r2 = RoundFactory.createRound(2, gADG, gBEH, gCFI);
        Round r3 = RoundFactory.createRound(3, gAFH, gBDI, gCEG);
        Tournament t = new Tournament();
        t.setJuryCapacity(6);
        t.addRounds(r1, r2, r3);

        t.addJurors(jA1, jA2, jA3, jA4, jA5, jA6);
        t.addJurors(jB1, jB2, jB3, jB4);
        t.addJurors(jC1, jC2, jC3, jC4);
        t.addJurors(jD1, jD2, jE1, jF1, jG1);
        t.addDayOffs(new DayOff(jE1, r1.getDay()), new DayOff(jE1, r3.getDay()));

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
