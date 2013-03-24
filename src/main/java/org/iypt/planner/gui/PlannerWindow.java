package org.iypt.planner.gui;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.ApplicationContext.ScheduledCallback;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Button.State;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.csv.ScheduleWriter;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
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
    private static final DateFormat SCORE_CHANGE_FORMAT = DateFormat.getTimeInstance();
    private static final long SCORE_CHANGE_DELAY = 5000;
    private static final long SCORE_CHANGE_PERIOD = 5000;
    // constraints config tab controls
    @BXML private Label drlLabel;
    @BXML private ConstraintsConfig constraintConfig;
    // planning tab controls
    @BXML private Label scoreLabel;
    @BXML private BoxPane scoreChangeBox;
    @BXML private Label scoreChangeLabel;
    @BXML private Label scoreChangeDiffLabel;
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
    @BXML private Label totalJurorsLabel;
    @BXML private Spinner juryCapacitySpinner;
    @BXML private Label totalSeatsLabel;
    @BXML private Label totalMandaysLabel;
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
    private CSVTournamentFactory factory;
    private Filter<File> csvFileFilter = new Filter<File>() {
        @Override
        public boolean include(File item) {
            return !(item.isDirectory() || item.getName().toLowerCase().endsWith(".csv"));
        }
    };
    private ScheduledCallback scoreChangedTimer;

    private abstract class LoadFileAction extends Action {

        abstract void processFile(File f) throws Exception;
        private final String fileType;

        public LoadFileAction(String fileType) {
            this.fileType = fileType;
        }

        @Override
        public void perform(Component source) {
            // TODO set root folder to last selected file parent
            final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
            fileBrowserSheet.setDisabledFileFilter(csvFileFilter);
            fileBrowserSheet.open(PlannerWindow.this, new SheetCloseListener() {
                @Override
                public void sheetClosed(Sheet sheet) {
                    if (sheet.getResult()) {
                        File f = fileBrowserSheet.getSelectedFile();
                        try {
                            processFile(f);
                        } catch (Exception ex) {
                            log.error("Error reading data file", ex);
                            String message = String.format("%s. Perhaps this is not a %s data file?", ex.getMessage(), fileType);
                            Alert.alert(MessageType.ERROR, message, PlannerWindow.this);
                        }
                    }
                }
            });
        }
    }
    private LoadFileAction loadTeamsAction = new LoadFileAction("teams") {
        @Override
        void processFile(File f) throws Exception {
            factory.readTeamData(f);
            loadScheduleAction.setEnabled(factory.canReadSchedule());
        }
    };
    private LoadFileAction loadJurorsAction = new LoadFileAction("jurors") {
        @Override
        void processFile(File f) throws Exception {
            factory.readJuryData(f);
            loadScheduleAction.setEnabled(factory.canReadSchedule());
        }
    };
    private LoadFileAction loadScheduleAction = new LoadFileAction("schedule") {
        @Override
        void processFile(File f) throws Exception {
            factory.readSchedule(f);
            tournamentLoaded(factory.newTournament());
        }
    };
    private Action clearScheduleAction = new Action() {
        @Override
        public void perform(Component source) {
            saveScheduleAction.setEnabled(false);
            solver.clearSchedule();
            solutionChanged();
        }
    };
    private Action saveScheduleAction = new Action() {
        @Override
        public void perform(Component source) {
            // create new FileBrowser to make sure a fresh file list is displayed
            // TODO set root folder
            final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_AS);
            fileBrowserSheet.setDisabledFileFilter(csvFileFilter);
            fileBrowserSheet.setSelectedFile(new File(fileBrowserSheet.getRootDirectory(), "schedule.csv"));
            fileBrowserSheet.open(PlannerWindow.this, new SheetCloseListener() {
                @Override
                public void sheetClosed(Sheet sheet) {
                    if (sheet.getResult()) {
                        File f = fileBrowserSheet.getSelectedFile();
                        // TODO check if the file exists and ask to overwrite
                        try {
                            new ScheduleWriter(solver.getTournament()).write(new FileWriter(f));
                            log.info("Schedule written to '{}'", f.getAbsolutePath());
                        } catch (Exception ex) {
                            log.error("Error writing schedule file", ex);
                            Alert.alert(MessageType.ERROR, ex.getMessage(), PlannerWindow.this);
                        }
                    }
                }
            });
        }
    };
    private Action newTournamentAction = new Action() {
        @Override
        public void perform(Component source) {
            factory = new CSVTournamentFactory();
            loadTeamsAction.setEnabled(true);
            loadJurorsAction.setEnabled(true);
            loadScheduleAction.setEnabled(false);
            clearScheduleAction.setEnabled(false);
            saveScheduleAction.setEnabled(false);
            loadExampleAction.setEnabled(true);
            tournamentScheduleBoxPane.removeAll();
        }
    };
    private Action loadExampleAction = new Action() {
        @Override
        public void perform(Component source) {
            try {
                factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jury_data.csv", "bias_IYPT2012.csv", "schedule2012.csv");
                tournamentLoaded(factory.newTournament());
            } catch (Exception ex) {
                log.error("Failed to load example", ex);
                Alert.alert(MessageType.ERROR, "Failed to load example: " + ex.getMessage(), PlannerWindow.this);
            }
        }
    };

    public PlannerWindow() {
        Action.getNamedActions().put("quit", new Action() {
            @Override
            public void perform(Component source) {
                DesktopApplicationContext.exit();
            }
        });
        Action.getNamedActions().put("loadTeams", loadTeamsAction);
        Action.getNamedActions().put("loadJurors", loadJurorsAction);
        Action.getNamedActions().put("loadSchedule", loadScheduleAction);
        Action.getNamedActions().put("saveSchedule", saveScheduleAction);
        Action.getNamedActions().put("clearSchedule", clearScheduleAction);
        Action.getNamedActions().put("newTournament", newTournamentAction);
        Action.getNamedActions().put("loadExample", loadExampleAction);
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        newTournamentAction.setEnabled(false);
        loadTeamsAction.setEnabled(false);
        loadJurorsAction.setEnabled(false);
        loadScheduleAction.setEnabled(false);
        saveScheduleAction.setEnabled(false);
        clearScheduleAction.setEnabled(false);
        loadExampleAction.setEnabled(false);
        solveButton.setEnabled(false);
        TaskListener<TournamentSolver> newSolverTaskListener = new TaskListener<TournamentSolver>() {
            @Override
            public void taskExecuted(Task<TournamentSolver> task) {
                solver = task.getResult();
                drlLabel.setText(solver.getScoreDrlList().get(0));
                constraintConfig.setSolver(solver);
                jurorDetails.setSolver(solver);
                newTournamentAction.setEnabled(true);
                newTournamentAction.perform(PlannerWindow.this);
                log.info("Solver initialized");
            }

            @Override
            public void executeFailed(Task<TournamentSolver> task) {
                log.error("Error during solution", task.getFault());
                Alert.alert(MessageType.ERROR, task.getFault().getMessage(), PlannerWindow.this);
            }
        };

        new Task<TournamentSolver>() {
            @Override
            public TournamentSolver execute() throws TaskExecutionException {
                return newSolver();
            }
        }.execute(new TaskAdapter<>(newSolverTaskListener));

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
                button.setEnabled(false);
                terminateButton.setEnabled(true);
                clearSwap();
                scoreChangeBox.setVisible(false);
                solverTask = new SolverTask(solver);
                TaskListener<Void> taskListener = new TaskListener<Void>() {
                    @Override
                    public void taskExecuted(Task<Void> task) {
                        log.debug(solver.getTournament().toDisplayString());
                        solveButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        solutionChanged();
                    }

                    @Override
                    public void executeFailed(Task<Void> task) {
                        log.error("Error during solution", task.getFault());
                        solveButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        scoreLabel.setText(task.getFault().toString());
                        scoreChangedTimer.cancel();
                        scoreChangeLabel.setText("");
                        Alert.alert(MessageType.ERROR, task.getFault().getMessage(), PlannerWindow.this);
                    }
                };
                // TaskAdapter forwards task events to the UI thread
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
                for (Seat seat : t.getSeats()) {
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
        jurorDetails = new JurorDetails();
        jurorDetails.setListener(PlannerWindow.this);
        jurorBorder.setContent(jurorDetails);
        juryCapacitySpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
                Integer capacity = (Integer) spinner.getSelectedItem();
                // TODO schedule the capacity change and apply it only when new solving starts
                solver.getTournament().setJuryCapacity(capacity);
                tournamentChanged();
            }
        });
        scoreChangeBox.setVisible(false);
        scoreChangeDiffLabel.getStyles().put("color", Color.GRAY);
    }

    private void tournamentLoaded(Tournament tournament) {
        loadTeamsAction.setEnabled(false);
        loadJurorsAction.setEnabled(false);
        loadScheduleAction.setEnabled(true);
        clearScheduleAction.setEnabled(true);
        saveScheduleAction.setEnabled(true);
        solver.setTournament(tournament);
        tournamentSchedule = new TournamentSchedule(solver);
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
        tournamentScheduleBoxPane.removeAll();
        tournamentScheduleBoxPane.add(tournamentSchedule);
        solveButton.setEnabled(true);
        scoreChangeBox.setVisible(false);
        tournamentChanged();
        solutionChanged();
        updateRoundDetails(solver.getTournament().getRounds().get(0));
        log.info("Tournament loaded\n{}", tournament.toDisplayString());
    }

    void solutionChanged() {
        scoreLabel.setText(solver.getScore().toString());
        if (scoreChangedTimer != null) {
            scoreChangedTimer.cancel();
        }
        if (solver.isSolving()) {
            final Date lastScoreChange = new Date();
            scoreChangeBox.setVisible(true);
            scoreChangeLabel.setText(SCORE_CHANGE_FORMAT.format(lastScoreChange));
            scoreChangeDiffLabel.setText("");
            scoreChangedTimer = ApplicationContext.scheduleRecurringCallback(new Runnable() {
                @Override
                public void run() {
                    long diff = new Date().getTime() - lastScoreChange.getTime();
                    String s = DurationFormatUtils.formatDurationWords(diff, true, false);
                    scoreChangeDiffLabel.setText(String.format("(%s ago)", s));
                }
            }, SCORE_CHANGE_DELAY, SCORE_CHANGE_PERIOD);
        } else {
            // hide if not solving
            scoreChangeDiffLabel.setText("");
        }

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
            constraintsBoxPane.add(rollup);
            Label heading = new Label(String.format("%s (%d/%d%s)", coId, coList.getLength(), total, type));
            rollup.setHeading(heading);
            if (coList.isEmpty()) {
                rollup.setEnabled(false);
                rollup.getHeading().setEnabled(false);
            } else {
                if (map.get(coId).get(0).isHard()) {
                    heading.getStyles().put("color", Color.RED.darker());
                }
            }

            TableView tableView = new TableView();
            rollup.setContent(tableView);

            tableView.getColumns().add(new TableView.Column("name", null, -1, false));
            tableView.getColumns().add(new TableView.Column("weight", null, -1, false));

            coList.setComparator(new Comparator<Constraint>() {
                @Override
                public int compare(Constraint o1, Constraint o2) {
                    // sort by weight descending
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
        totalJurorsLabel.setText(Integer.toString(t.getJurors().size()));
        juryCapacitySpinner.setSelectedItem(Integer.valueOf(t.getJuryCapacity()));
        totalSeatsLabel.setText(Integer.toString(t.getSeats().size()));
        totalMandaysLabel.setText(Integer.toString(t.getJurors().size() * t.getRounds().size() - t.getDayOffs().size()));
        optimalLoadLabel.setText(String.format("%.4f", t.getStatistics().getOptimalLoad()));
        tournamentSchedule.updateSchedule();
    }

    private TournamentSolver newSolver() {
        TournamentSolver solver = new TournamentSolver("/org/iypt/planner/solver/config.xml");
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
            swap1TableView.setTableData(new ArrayList<>(JurorRow.newInstance(juror1)));
        }
        if (juror2 != null) {
            swap2TableView.setTableData(new ArrayList<>(JurorRow.newInstance(juror2)));
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
