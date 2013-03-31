package org.iypt.planner.gui;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextInput;
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
    @BXML private PushButton selectButton;
    @BXML private PushButton unselectButton;
    @BXML private PushButton cancelButton;
    @BXML private PushButton okButton;
    @BXML private ListView selectedListView;
    @BXML private ListView unselectedListView;
    @BXML private TextInput fileTextInput;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        fileTextInput.setEditable(false);
        selectButton.setEnabled(false);
        unselectButton.setEnabled(false);
        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                BiasComputationWizard.this.close();
            }
        });
        browseButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_AS);
                fileBrowserSheet.setDisabledFileFilter(PlannerWindow.CSV_FILE_FILTER);
                fileBrowserSheet.open(BiasComputationWizard.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File f = fileBrowserSheet.getSelectedFile();
                            try {
                                TournamentData data = new TournamentData();
                                data.readData(new FileReader(f));
                                ArrayList<Tournament> list = new ArrayList<>();
                                list.addAll(data.getTournaments());
                                fileTextInput.setText(f.getAbsolutePath());
                                unselectedListView.setListData(new ListAdapter<>(list));
                            } catch (Exception ex) {
                                wlog.error("Failed to read tournament data", ex);
                            }
                        }
                    }
                });
            }
        });
        unselectedListView.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(ListView listView, Object previousSelectedItem) {
                selectButton.setEnabled(listView.getSelectedIndex() >= 0);
            }
        });
        selectedListView.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(ListView listView, Object previousSelectedItem) {
                unselectButton.setEnabled(listView.getSelectedIndex() >= 0);
            }
        });
        selectButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Object item = unselectedListView.getSelectedItem();
                unselectedListView.getListData().remove(unselectedListView.getSelectedIndex(), 1);
                @SuppressWarnings("unchecked")
                List<Object> listData = (List<Object>) selectedListView.getListData();
                listData.add(item);
            }
        });
        unselectButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Object item = selectedListView.getSelectedItem();
                selectedListView.getListData().remove(selectedListView.getSelectedIndex(), 1);
                @SuppressWarnings("unchecked")
                List<Object> listData = (List<Object>) unselectedListView.getListData();
                listData.add(item);
            }
        });
    }
}
