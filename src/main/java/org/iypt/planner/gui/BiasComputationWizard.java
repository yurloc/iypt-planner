package org.iypt.planner.gui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextInput;
import org.iypt.planner.csv.full_data.BiasWriter;
import org.iypt.planner.csv.full_data.Juror;
import org.iypt.planner.csv.full_data.Tournament;
import org.iypt.planner.csv.full_data.TournamentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlocker
 */
public class BiasComputationWizard extends Sheet implements Bindable {

    private static final Logger LOG = LoggerFactory.getLogger(BiasComputationWizard.class);
    private WindowLogger wlog = new WindowLogger(LOG, this);
    @BXML private PushButton browseButton;
    @BXML private PushButton exportButton;
    @BXML private PushButton cancelButton;
    @BXML private PushButton loadButton;
    @BXML private TextInput fileTextInput;
    @BXML private TableView jurorsTableView;
    @BXML private ListView tournamentListView;
    @BXML private ImageView tipImageView;
    private java.util.Map<String, Double> biases = new java.util.HashMap<>();
    private boolean loadEnabled;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        setLoadEnabled(true);
        fileTextInput.setEditable(false);
        exportButton.setEnabled(false);
        loadButton.setEnabled(false);
        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                BiasComputationWizard.this.close();
            }
        });
        browseButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, PlannerWindow.getLastDir());
                fileBrowserSheet.setDisabledFileFilter(PlannerWindow.CSV_FILE_FILTER);
                fileBrowserSheet.open(BiasComputationWizard.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File f = fileBrowserSheet.getSelectedFile();
                            PlannerWindow.setLastDir(f.getParent());
                            try (FileReader fr = new FileReader(f)) {
                                TournamentData data = new TournamentData();
                                data.readData(fr);
                                ArrayList<Tournament> list = new ArrayList<>();
                                list.addAll(data.getTournaments());
                                for (Tournament tournament : list) {
                                    tournament.calculate();
                                }
                                fileTextInput.setText(f.getAbsolutePath());
                                tournamentListView.setListData(new ListAdapter<>(list));
                            } catch (RuntimeException | IOException ex) {
                                wlog.error("Failed to read tournament data", ex);
                            }
                        }
                    }
                });
            }
        });
        tournamentListView.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(ListView listView, Object previousSelectedItem) {
                if (listView.getSelectedIndex() >= 0) {
                    Tournament tmt = (Tournament) listView.getSelectedItem();
                    ArrayList<Juror> list = new ArrayList<>();
                    list.addAll(tmt.getJurors());
                    jurorsTableView.setTableData(new ListAdapter<>(list));
                    exportButton.setEnabled(true);
                    loadButton.setEnabled(loadEnabled);
                } else {
                    exportButton.setEnabled(false);
                    loadButton.setEnabled(false);
                }
            }
        });
        exportButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_AS, PlannerWindow.getLastDir());
                fileBrowserSheet.setDisabledFileFilter(PlannerWindow.CSV_FILE_FILTER);
                fileBrowserSheet.setSelectedFile(new File(fileBrowserSheet.getRootDirectory(), "biases.csv"));
                fileBrowserSheet.open(BiasComputationWizard.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File f = fileBrowserSheet.getSelectedFile();
                            if (f != null) {
                                PlannerWindow.setLastDir(f.getParent());
                                try (FileWriter fw = new FileWriter(f)){
                                    Tournament tmt = (Tournament) tournamentListView.getSelectedItem();
                                    TreeSet<Juror> jurors = new TreeSet<>(new Juror.BiasComparator());
                                    jurors.addAll(tmt.getJurors());
                                    BiasWriter bw = new BiasWriter(jurors);
                                    bw.write(fw);
                                    wlog.info("Biases written to " + f.getAbsolutePath());
                                } catch (Throwable t) {
                                    wlog.error("Error writing schedule file", t);
                                }
                            }
                        }
                    }
                });
            }
        });
        loadButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Tournament tmt = (Tournament) tournamentListView.getSelectedItem();
                for (Juror juror : tmt.getJurors()) {
                    biases.put(juror.getName(), Double.valueOf(juror.getAverageBias()));
                }
                BiasComputationWizard.this.close();
            }
        });
    }

    public java.util.Map<String, Double> getBiases() {
        return Collections.unmodifiableMap(biases);
    }

    public boolean isLoadEnabled() {
        return loadEnabled;
    }

    public void setLoadEnabled(boolean loadEnabled) {
        this.loadEnabled = loadEnabled;
        tipImageView.setVisible(!loadEnabled);
    }

}
