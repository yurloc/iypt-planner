package org.iypt.planner.gui;

import com.itextpdf.text.DocumentException;
import com.jcabi.manifests.Manifests;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.ApplicationContext.ScheduledCallback;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Button.State;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ListItem;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.iypt.planner.Constants;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.csv.ScheduleWriter;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.pdf.PdfCreator;
import org.iypt.planner.solver.TournamentSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlocker
 */
public class PlannerWindow extends Window implements Bindable {

    private static final Logger log = LoggerFactory.getLogger(PlannerWindow.class);
    private static final long SCORE_CHANGE_DELAY = 5000;
    private static final long SCORE_CHANGE_PERIOD = 5000;
    private final DateFormat SCORE_CHANGE_FORMAT = DateFormat.getTimeInstance();
    private WindowLogger wlog = new WindowLogger(log, this);
    // constraints config tab controls
    @BXML private Label drlLabel;
    @BXML private ConstraintsConfig constraintConfig;
    @BXML private ListButton envListButton;
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
    // details
    @BXML private TournamentDetails tournamentDetails;
    @BXML private RoundDetails roundDetails;
    @BXML private JurorDetails jurorDetails;
    // build info
    @BXML private Label buildInfoLabel;
    // other
    private TournamentSchedule tournamentSchedule;
    private SolverTask solverTask;
    private TournamentSolver solver;
    private Juror juror1;
    private Juror juror2;
    private CSVTournamentFactory factory;
    protected static final Filter<File> CSV_FILE_FILTER = new Filter<File>() {
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
            fileBrowserSheet.setDisabledFileFilter(CSV_FILE_FILTER);
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
    private final LoadFileAction loadTeamsAction = new LoadFileAction("teams") {
        @Override
        void processFile(File f) throws Exception {
            factory.readTeamData(f, StandardCharsets.UTF_8);
            if (factory.canCreateTournament()) {
                tournamentLoaded(factory.newTournament());
            }
            loadScheduleAction.setEnabled(factory.canReadSchedule());
            loadTeamsAction.setEnabled(false);
        }
    };
    private final LoadFileAction loadJurorsAction = new LoadFileAction("jurors") {
        @Override
        void processFile(File f) throws Exception {
            factory.readJuryData(f, StandardCharsets.UTF_8);
            if (factory.canCreateTournament()) {
                tournamentLoaded(factory.newTournament());
            }
            loadScheduleAction.setEnabled(factory.canReadSchedule());
            loadJurorsAction.setEnabled(false);
        }
    };
    private final LoadFileAction loadBiasesAction = new LoadFileAction("biases") {
        @Override
        void processFile(File f) throws Exception {
            factory.readBiasData(f, StandardCharsets.UTF_8);
            if (factory.canCreateTournament()) {
                tournamentLoaded(factory.newTournament());
            }
            loadScheduleAction.setEnabled(factory.canReadSchedule());
            loadBiasesAction.setEnabled(false);
        }
    };
    private final LoadFileAction loadScheduleAction = new LoadFileAction("schedule") {
        @Override
        void processFile(File f) throws Exception {
            factory.readSchedule(f, StandardCharsets.UTF_8);
            tournamentLoaded(factory.newTournament());
        }
    };
    private final Action clearScheduleAction = new Action() {
        @Override
        public void perform(Component source) {
            // FIXME find a way to detect changes in the schedule (better than solutionChanged())
//            saveScheduleAction.setEnabled(false);
            ScheduleModel sm = solver.clearSchedule();
            solutionChanged(sm);
        }
    };
    private final Action saveScheduleAction = new Action() {
        @Override
        public void perform(Component source) {
            // create new FileBrowser to make sure a fresh file list is displayed
            // TODO set root folder
            final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_AS);
            fileBrowserSheet.setDisabledFileFilter(CSV_FILE_FILTER);
            fileBrowserSheet.setSelectedFile(new File(fileBrowserSheet.getRootDirectory(), "schedule.csv"));
            fileBrowserSheet.open(PlannerWindow.this, new SheetCloseListener() {
                @Override
                public void sheetClosed(Sheet sheet) {
                    if (sheet.getResult()) {
                        File f = fileBrowserSheet.getSelectedFile();
                        // TODO check if the file exists and ask to overwrite
                        try {
                            OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
                            new ScheduleWriter(solver.getTournament()).write(os);
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
    private final Action exportPdfAction = new Action() {
        @Override
        public void perform(Component source) {
            // create new FileBrowser to make sure a fresh file list is displayed
            // TODO set root folder
            final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
            fileBrowserSheet.setDisabledFileFilter(new Filter<File>() {
                @Override
                public boolean include(File item) {
                    return !item.isDirectory();
                }
            });
            fileBrowserSheet.open(PlannerWindow.this, new SheetCloseListener() {
                @Override
                public void sheetClosed(Sheet sheet) {
                    if (sheet.getResult()) {
                        File dir = fileBrowserSheet.getSelectedFile();
                        try {
                            Date d = new Date();
                            String time = String.format("%ty%tm%te_%tH%tM%tS_", d, d, d, d, d, d);
                            PdfCreator pdf = new PdfCreator(solver.getTournament());
                            pdf.setOutputDir(dir);
                            pdf.setFilePrefix(time);
                            pdf.printRooms();
                            pdf.printRounds();
                            log.info("Written PDFs with timestamp '{}'.", time);
                        } catch (DocumentException | IOException ex) {
                            log.error("Error while exporting PDFs", ex);
                            Alert.alert(MessageType.ERROR, ex.getMessage(), PlannerWindow.this);
                        }
                    }
                }
            });
        }
    };
    private final Action newTournamentAction = new Action() {
        @Override
        public void perform(Component source) {
            factory = new CSVTournamentFactory();
            loadTeamsAction.setEnabled(true);
            loadJurorsAction.setEnabled(true);
            loadBiasesAction.setEnabled(true);
            loadScheduleAction.setEnabled(false);
            clearScheduleAction.setEnabled(false);
            saveScheduleAction.setEnabled(false);
            exportPdfAction.setEnabled(false);
            computeBiasesAction.setEnabled(true);
            loadExampleAction.setEnabled(true);
            tournamentScheduleBoxPane.removeAll();
        }
    };
    private final Action computeBiasesAction = new Action() {
        @Override
        public void perform(Component source) {
            BXMLSerializer bxmlSerializer = new BXMLSerializer();
            try {
                final BiasComputationWizard wizard = (BiasComputationWizard) bxmlSerializer.readObject(PlannerWindow.class, "bias_wizard.bxml");
                wizard.setLoadEnabled(loadTeamsAction.isEnabled());
                wizard.open(getDisplay(), getWindow(), new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        factory.setBiases(wizard.getBiases());
                    }
                });
            } catch (IOException | SerializationException ex) {
                wlog.error("Cannot open bias computation wizard", ex);
            }
        }
    };
    private final Action loadExampleAction = new Action() {
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
        Action.getNamedActions().put("loadBiases", loadBiasesAction);
        Action.getNamedActions().put("loadSchedule", loadScheduleAction);
        Action.getNamedActions().put("saveSchedule", saveScheduleAction);
        Action.getNamedActions().put("exportPdf", exportPdfAction);
        Action.getNamedActions().put("clearSchedule", clearScheduleAction);
        Action.getNamedActions().put("newTournament", newTournamentAction);
        Action.getNamedActions().put("computeBiases", computeBiasesAction);
        Action.getNamedActions().put("loadExample", loadExampleAction);
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        newTournamentAction.setEnabled(false);
        loadTeamsAction.setEnabled(false);
        loadJurorsAction.setEnabled(false);
        loadBiasesAction.setEnabled(false);
        loadScheduleAction.setEnabled(false);
        saveScheduleAction.setEnabled(false);
        exportPdfAction.setEnabled(false);
        clearScheduleAction.setEnabled(false);
        computeBiasesAction.setEnabled(false);
        loadExampleAction.setEnabled(false);
        solveButton.setEnabled(false);
        tournamentDetails.setEnabled(false);
        TaskListener<TournamentSolver> newSolverTaskListener = new TaskListener<TournamentSolver>() {
            @Override
            public void taskExecuted(Task<TournamentSolver> task) {
                solver = task.getResult();
                drlLabel.setText(solver.getScoreDrlList().get(0));
                for (Object obj : envListButton.getListData()) {
                    ListItem item = (ListItem) obj;
                    if (solver.getEnvironmentMode().toString().equals(item.getText())) {
                        envListButton.setSelectedItem(item);
                    }
                }
                constraintConfig.setSolver(solver);
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

        envListButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
                solver.setEnvironmentMode(((ListItem) listButton.getSelectedItem()).getText());
            }
        });

        roundDetails.getListeners().add(new RoundDetailsListener.Adapter() {
            @Override
            public void seatSelected(SeatInfo seatInfo) {
                if (seatInfo != null) {
                    showJurorDetails(seatInfo.getJuror());
                    // TODO improve this mess
                    JurorInfo jurorInfo = tournamentSchedule.getSchedule().getJurorInfo(seatInfo.getJuror());
                    // FIXME NPE upon selecting juror in round details
                    if (jurorInfo.getSchedule().get(tournamentSchedule.getSelectedRound().getNumber() - 1).getCurrentStatus() == JurorAssignment.Status.IDLE) {
                        prepareSwap(seatInfo.getJuror());
                    }
                }
            }
        });

        solveButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                // TODO better code structure, perhaps move the logic out of event listeners
                button.setEnabled(false);
                terminateButton.setEnabled(true);
                clearSwap();
                scoreChangeBox.setVisible(false);
                solverTask = new SolverTask(solver);
                TaskListener<ScheduleModel> taskListener = new TaskListener<ScheduleModel>() {
                    @Override
                    public void taskExecuted(Task<ScheduleModel> task) {
                        log.debug(solver.getTournament().toDisplayString());
                        solveButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        solutionChanged(task.getResult());
                    }

                    @Override
                    public void executeFailed(Task<ScheduleModel> task) {
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
                    if (seat.getJury().getGroup().getRound() == tournamentSchedule.getSelectedRound().getRound()) {
                        if (seat.getJuror() == juror1) {
                            seat.setJuror(juror2);
                        } else if (seat.getJuror() == juror2) {
                            seat.setJuror(juror1);
                        }
                    }
                }
                solutionChanged(solver.setTournament(t));
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
        jurorDetails.getDetailsListeners().add(new JurorDetailsListener.Adapter() {

            @Override
            public void jurorChangesSaved(JurorDetails details) {
                JurorInfo jurorInfo = details.getJurorInfo();
                // FIXME changing juror assignment not reflected in round details (e.g. idle -> away)
                ScheduleModel sm = solver.applyChanges(jurorInfo);
                jurorDetails.showJuror(sm.getJurorInfo(jurorInfo.getJuror()));
                solutionChanged(sm);
            }

            @Override
            public void jurorAssignmentSelected(JurorAssignment assignment) {
                tournamentSchedule.selectRound(assignment.getRound());
            }
        });
        tournamentDetails.getListeners().add(new TournamentDetailsListener.Adapter() {
            @Override
            public void capacityChanged(int capacity) {
                changeCapacity(capacity);
            }
        });
        scoreChangeBox.setVisible(false);
        scoreChangeDiffLabel.getStyles().put("color", Color.GRAY);
        String version = "development";
        String revision = "unknown";
        if (Manifests.exists(Constants.MANIFEST_BUILD_VERSION)) {
            version = Manifests.read(Constants.MANIFEST_BUILD_VERSION);
            revision = Manifests.read(Constants.MANIFEST_BUILD_REVISION);
        }
        buildInfoLabel.setText(String.format("Version: %s (%s)", version, revision.substring(0, 5)));
        buildInfoLabel.setTooltipText("Revision: " + revision);
    }

    private void tournamentLoaded(Tournament tournament) {
        loadTeamsAction.setEnabled(false);
        loadJurorsAction.setEnabled(false);
        loadScheduleAction.setEnabled(true);
        clearScheduleAction.setEnabled(true);
        saveScheduleAction.setEnabled(true);
        exportPdfAction.setEnabled(true);
        tournamentDetails.setEnabled(true);
        ScheduleModel scheduleModel = solver.setTournament(tournament);
        tournamentSchedule = new TournamentSchedule(scheduleModel);
        tournamentSchedule.getTournamentScheduleListeners().add(new TournamentScheduleListener.Adapter() {
            @Override
            public void roundSelected(RoundModel round) {
                updateRoundDetails(round);
            }

            @Override
            public void seatSelected(SeatInfo seatInfo) {
                if (seatInfo != null) {
                    showJurorDetails(seatInfo.getJuror());
                    prepareSwap(seatInfo.getJuror());
                }
            }

            @Override
            public void seatLocked(SeatInfo seatInfo) {
                solver.lockSeat(seatInfo);
            }

            @Override
            public void seatUnlocked(SeatInfo seatInfo) {
                solver.unlockSeat(seatInfo);
            }

            @Override
            public void roundLockRequested(RoundModel round) {
                ScheduleModel sm = solver.requestRoundLockChange(round.getRound());
                solutionChanged(sm);
            }
        });
        tournamentScheduleBoxPane.removeAll();
        tournamentScheduleBoxPane.add(tournamentSchedule);
        solveButton.setEnabled(true);
        scoreChangeBox.setVisible(false);
        tournamentChanged(scheduleModel);
        solutionChanged(scheduleModel);
        updateRoundDetails(scheduleModel.getRounds().get(0));
        log.info("Tournament loaded\n{}", tournament.toDisplayString());
    }

    private void changeCapacity(int capacity) {
        // TODO schedule the capacity change and apply it only when new solving starts
        ScheduleModel sm = solver.changeJuryCapacity(capacity);
        tournamentChanged(sm);
        solutionChanged(sm);
    }

    // TODO doesn't need to be synchronized, it's always run on the UI thread
    synchronized void solutionChanged(ScheduleModel sm) {
        // show score
        scoreLabel.setText(solver.getScore().toString());
        // reset timer
        if (scoreChangedTimer != null) {
            scoreChangedTimer.cancel();
        }
        if (solver.isSolving()) {
            // show last score change timestamp
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

        // refresh constraints
        constraintsBoxPane.removeAll();
        HashMap<String, List<Constraint>> map = new HashMap<>();
        for (ConstraintOccurrence constraintId : solver.getConstraints()) {
            map.put(constraintId.getRuleId(), new ArrayList<Constraint>());
        }

        for (ConstraintOccurrence co : solver.getConstraintOccurences()) {
            map.get(co.getRuleId()).add(new Constraint(co));
        }

        for (Entry<String, List<Constraint>> entry : map.entrySet()) {
            String coId = entry.getKey();
            List<Constraint> coList = entry.getValue();
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
                if (coList.get(0).isHard()) {
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
        tournamentSchedule.updateSchedule(sm);
    }

    private void tournamentChanged(ScheduleModel sm) {
        Tournament t = solver.getTournament();
        tournamentDetails.setData(t);
        tournamentSchedule.updateSchedule(sm);
    }

    private TournamentSolver newSolver() {
        return new TournamentSolver(Constants.SOLVER_CONFIG_PATH, new SolverListener());
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
            // TODO cancel previous update if it hasn't yet started
            // 1. increment # of changes here
            Tournament better = (Tournament) event.getNewBestSolution();
            final ScheduleModel sm = solver.setTournament(better);
            solver.setTournament(better);
            ApplicationContext.queueCallback(new Runnable() {
                @Override
                // 2. put current # of changes as arg. if arg is < current #chages, the method may return, because
                // a new change has already been scheduled
                public void run() {
                    solutionChanged(sm);
                }
            });
        }
    }

    //=========================================================================================================================
    // tasks
    //=========================================================================================================================
    private static class SolverTask extends Task<ScheduleModel> {

        private final TournamentSolver solver;

        public SolverTask(TournamentSolver solver) {
            this.solver = solver;
        }

        public void terminate() {
            solver.terminateEarly();
        }

        @Override
        public ScheduleModel execute() throws TaskExecutionException {
            return solver.solve();
        }
    }

    private void updateRoundDetails(RoundModel round) {
        if (round != null && solver.isSolving()) {
            clearSwap();
        }
        roundDetails.setData(round);
    }

    private void showJurorDetails(Juror juror) {
        if (juror != null) {
            jurorDetails.showJuror(tournamentSchedule.getSchedule().getJurorInfo(juror));
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
            swap1TableView.setTableData(new ArrayList<>(SeatInfo.newInstance(juror1)));
        }
        if (juror2 != null) {
            swap2TableView.setTableData(new ArrayList<>(SeatInfo.newInstance(juror2)));
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
