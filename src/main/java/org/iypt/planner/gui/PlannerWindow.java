package org.iypt.planner.gui;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Button.State;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.phase.event.SolverPhaseLifecycleListenerAdapter;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurySeat;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.CSVTournamentFactory;
import org.iypt.planner.gui.GroupRoster.JurorRow;
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
    @BXML private PushButton swapButton;
    @BXML private TableView swap1TableView;
    @BXML private TableView swap2TableView;
    @BXML private Checkbox showChangesCheckbox;
    @BXML private BoxPane tournamentScheduleBoxPane;
    @BXML private BoxPane constraintsBoxPane;
    @BXML private ListView causesListView;

    // tournament details
    @BXML private Label totalSeatsLabel;
    @BXML private Label totalMandaysLabel;
    @BXML private Label totalJurorsLabel;
    @BXML private Label optimalLoadLabel;

    // round details
    @BXML private Label optimalIndependentLabel;
    @BXML private Label idleLabel;
    @BXML private Label awayLabel;
    @BXML private TableView idleTableView;
    @BXML private TableView awayTableView;

    // juror details
    @BXML private Border jurorBorder;
    private JurorDetails jurorDetails;

    // other
    private TournamentSchedule tournamentSchedule;
    private SolverTask solverTask;
    private TournamentSolver solver;
    private Round selectedRound;
    private Juror juror1;
    private Juror juror2;

    public PlannerWindow() {
        Action.getNamedActions().put("quit", new Action() {
            @Override
            public void perform(Component source) {
                DesktopApplicationContext.exit();
            }
        });
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        Tournament tournament = null;
        try {
            tournament = getInitialSolutionFromCSV();
        } catch (IOException ex) {
//            Alert.alert(MessageType.ERROR, ex.getMessage(), PlannerWindow.this);
            ex.printStackTrace();
        }

        solver = newSolver();
        solver.setTournament(tournament);

        tournamentSchedule = new TournamentSchedule(solver);
        tournamentScheduleBoxPane.add(tournamentSchedule);
        tournamentSchedule.getTournamentScheduleListeners().add(new TournamentScheduleListener.Adapter() {
            @Override
            public void roundSelected(Round round) {
                updateRoundDetails(round);
            }

            @Override
            public void jurorSelected(Juror juror) {
                showJuror(juror);
                prepareSwap(juror);
            }

            @Override
            public void jurorLocked(JurorRow jurorRow) {
                solver.lockJuror(jurorRow);
            }

            @Override
            public void jurorUnlocked(JurorRow jurorRow) {
                solver.unlockJuror(jurorRow);
            }
        });
        TableViewSelectionListener.Adapter selectedJurorListener = new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                JurorRow row = (JurorRow) tableView.getSelectedRow();
                if (row != null) {
                    showJuror(row.getJuror());
                }
            }
        };
        ComponentStateListener.Adapter focusedJurorListener = new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (component.isFocused()) {
                    JurorRow row = (JurorRow) ((TableView) component).getSelectedRow();
                    if (row != null) {
                        showJuror(row.getJuror());
                    }
                }
            }
        };
        idleTableView.getTableViewSelectionListeners().add(selectedJurorListener);
        idleTableView.getComponentStateListeners().add(focusedJurorListener);
        awayTableView.getTableViewSelectionListeners().add(selectedJurorListener);
        awayTableView.getComponentStateListeners().add(focusedJurorListener);

        idleTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                JurorRow row = (JurorRow) tableView.getSelectedRow();
                if (row != null) {
                    prepareSwap(row.getJuror());
                }
            }
        });

        solveButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
//                Alert.alert(MessageType.INFO, "You clicked me!", PlannerWindow.this);
                button.setEnabled(false);
                terminateButton.setEnabled(true);
                clearSwap();
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
                        task.getFault().printStackTrace();
                        scoreLabel.setText(task.getFault().toString());
                    }
                };
                solverTask.execute(new TaskAdapter<>(taskListener));
            }
        });

        swap1TableView.setSelectMode(TableView.SelectMode.NONE);
        swap2TableView.setSelectMode(TableView.SelectMode.NONE);
        swapButton.setEnabled(false);
        swapButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Tournament t = solver.getTournament();
                for (JurySeat seat : t.getJurySeats()) {
                    if (seat.getJury().getGroup().getRound() == selectedRound) {
                        if (seat.getJuror() == juror1) {
                            seat.setJuror(juror2);
                        } else if (seat.getJuror() == juror2) {
                            seat.setJuror(juror1);
                        }
                    }
                }
                solver.setTournament(t);
                clearSwap();
                solutionChanged();
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
        jurorDetails = new JurorDetails(solver);
        jurorDetails.setListener(this);
        jurorBorder.setContent(jurorDetails);

        tournamentChanged();
        solutionChanged();
        updateRoundDetails(tournament.getRounds().get(0));
    }

    void solutionChanged() {
        scoreLabel.setText(solver.getScore().toString());

        constraintsBoxPane.removeAll();
        HashMap<String, List<Constraint>> map = new HashMap<>();
        for (ConstraintOccurrence constraintId : solver.getConstraints()) {
            map.put(constraintId.getRuleId(), new ArrayList<Constraint>());
        }

        for (ConstraintOccurrence co : solver.getConstraintOccurences()) {
            map.get(co.getRuleId()).add(new Constraint(co));
        }

        for (String coId : map.keySet()) {
            List<Constraint> coList = map.get(coId);
            String type = "";
            if (coList.getLength() > 0) {
                type = coList.get(0).getType().toLowerCase();
            }
            int total = 0;
            for (Constraint constraint : coList) {
                total += constraint.getIntWeight();
            }
            Rollup rollup = new Rollup(false);
            rollup.setEnabled(!coList.isEmpty());
            constraintsBoxPane.add(rollup);
            Label heading = new Label(String.format("%s (%d/%d%s)", coId, coList.getLength(), total, type));
            rollup.setHeading(heading);

            TableView tableView = new TableView();
            rollup.setContent(tableView);

            tableView.getColumns().add(new TableView.Column("name", null, -1, false));
            tableView.getColumns().add(new TableView.Column("weight", null, -1, false));

            coList.setComparator(new Comparator<Constraint>() {
                @Override
                public int compare(Constraint o1, Constraint o2) {
                    // sort by wight descending
                    return o2.getIntWeight() - o1.getIntWeight();
                }
            });
            tableView.setTableData(coList);
            tableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
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
        }
        updateRoundDetails(selectedRound);
        tournamentSchedule.updateSchedule();
    }

    private void tournamentChanged() {
        Tournament t = solver.getTournament();
        totalSeatsLabel.setText(Integer.toString(t.getJurySeats().size()));
        totalMandaysLabel.setText(Integer.toString(t.getJurors().size() * t.getRounds().size() - t.getDayOffs().size()));
        totalJurorsLabel.setText(Integer.toString(t.getJurors().size()));
        optimalLoadLabel.setText(String.format("%.4f", t.getStatistics().getOptimalLoad()));
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

    private void updateRoundDetails(Round round) {
        selectedRound = round;
        if (round == null) {
            optimalIndependentLabel.setText("");
            idleLabel.setText("Idle (0)");
            awayLabel.setText("Away (0)");
            idleTableView.getTableData().clear();
            awayTableView.getTableData().clear();
        } else {
            optimalIndependentLabel.setText(String.format("%.4f", round.getOptimalIndependentCount()));
            idleLabel.setText(String.format("Idle (%d)", solver.getIdle(round).size()));
            awayLabel.setText(String.format("Away (%d)", solver.getAway(round).size()));
            idleTableView.setTableData(new ListAdapter<>(solver.getIdleRows(round)));
            awayTableView.setTableData(new ListAdapter<>(solver.getAwayRows(round)));
            clearSwap();
        }
    }

    private void showJuror(Juror juror) {
        if (juror != null) {
            jurorDetails.showJuror(juror);
        }
    }

    private void prepareSwap(Juror juror) {
        if (juror1 == null) {
            juror1 = juror;
        } else if (juror2 == null) {
            if (juror != juror1) {
                juror2 = juror;
            }
        } else if (juror != juror2 && juror != juror1) {
            juror1 = juror2;
            juror2 = juror;
        }

        if (juror1 != null) {
            swap1TableView.setTableData(new ArrayList<>(new JurorRow(juror1)));
        }
        if (juror2 != null) {
            swap2TableView.setTableData(new ArrayList<>(new JurorRow(juror2)));
            if (!solver.isSolving()) {
                swapButton.setEnabled(true);
            }
        }

    }

    private void clearSwap() {
        juror1 = null;
        juror2 = null;
        swap1TableView.getTableData().clear();
        swap2TableView.getTableData().clear();
        swapButton.setEnabled(false);
    }
}
