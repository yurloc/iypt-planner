package org.iypt.planner.gui;

import java.io.IOException;
import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Button.State;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.phase.event.SolverPhaseLifecycleListenerAdapter;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.CSVTournamentFactory;
import org.iypt.planner.solver.TournamentSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlocker
 */
public class PlannerWindow extends Window implements Bindable {

    private static final Logger log = LoggerFactory.getLogger(PlannerWindow.class);

    // constraints config tab controls
    @BXML private Label drlLabel;
    @BXML private ConstraintsConfig constraintConfig;

    // planning tab controls
    @BXML private Label scoreLabel;
    @BXML private PushButton solveButton;
    @BXML private PushButton terminateButton;
    @BXML private Checkbox showChangesCheckbox;
    @BXML private BoxPane tournamentScheduleBoxPane;
    @BXML private TableView constraintsTableView;
    @BXML private ListView causesListView;

    // other
    private TournamentSchedule tournamentSchedule;
    private SolverTask solverTask;
    private TournamentSolver solver;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        Tournament tournament = null;
        try {
            tournament = getInitialSolutionFromCSV();
        } catch (IOException ex) {
//            Alert.alert(MessageType.ERROR, ex.getMessage(), PlannerWindow.this);
            ex.printStackTrace();
        }
        tournamentSchedule = new TournamentSchedule(tournament);
        tournamentScheduleBoxPane.add(tournamentSchedule);

        solver = newSolver();
        solver.setTournament(tournament);

        solveButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
//                Alert.alert(MessageType.INFO, "You clicked me!", PlannerWindow.this);
                button.setEnabled(false);
                terminateButton.setEnabled(true);
                solverTask = new SolverTask(solver);
                TaskListener<Void> taskListener = new TaskListener<Void>() {
                    @Override
                    public void taskExecuted(Task<Void> task) {
//                        activityIndicator.setActive(false);
                        solveButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        log.debug(solver.getTournament().toDisplayString());
                        solutionChanged();
                    }

                    @Override
                    public void executeFailed(Task<Void> task) {
//                        activityIndicator.setActive(false);
                        solveButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        scoreLabel.setText(task.getFault().toString());
                    }
                };
                solverTask.execute(new TaskAdapter<>(taskListener));
            }
        });

        terminateButton.setEnabled(false);
        terminateButton.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                solverTask.terminate();
            }
        });

        showChangesCheckbox.setState(Button.State.SELECTED);

        constraintsTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {

            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                int selectedIndex = tableView.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Constraint constraint = (Constraint) tableView.getTableData().get(selectedIndex);
                    causesListView.setListData(constraint.getCauses());
                } else {
                    causesListView.getListData().clear();
                }
            }

        });
        solutionChanged();
    }

    private void solutionChanged() {
        scoreLabel.setText(solver.getScore().toString());
        List<Constraint> constraints = new ArrayList<>();
        for (ConstraintOccurrence co : solver.getConstraintOccurences()) {
            constraints.add(new Constraint(co));
        }
        constraintsTableView.setTableData(constraints);
        tournamentSchedule.updateSchedule(solver.getTournament());
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

    private TournamentSolver newSolver() {
        TournamentSolver solver = new TournamentSolver("/org/iypt/planner/solver/config.xml");
        drlLabel.setText(solver.getScoreDrlList().get(0));
        constraintConfig.setSolver(solver);

        // build a solver
        solver.addEventListener(new SolverListener());
        return solver;
    }

    //=========================================================================================================================
    // listeners
    //=========================================================================================================================

    private class SolverListener implements SolverEventListener {

        @Override
        public void bestSolutionChanged(BestSolutionChangedEvent event) {
            if (showChangesCheckbox.getState() == State.UNSELECTED) {
                return;
            }
            Tournament better = (Tournament) event.getNewBestSolution();
            solver.setTournament(better);
            ApplicationContext.queueCallback(new Runnable() {

                @Override
                public void run() {
                    solutionChanged();
                }
            });
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

    private class SolverTask extends Task<Void> {

        private final TournamentSolver solver;

        public SolverTask(TournamentSolver solver) {
            this.solver = solver;
        }

        public void terminate() {
            solver.terminateEarly();
        }

        @Override
        public Void execute() throws TaskExecutionException {
            solver.solve();
//            Tournament solved = (Tournament) solver.solve();
//            return solved;
            return null;
        }

    }
}
