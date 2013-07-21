package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.content.ListItem;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorLoad;
import org.iypt.planner.domain.JurorType;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.util.CountryCodeIO;
import org.iypt.planner.solver.TournamentSolver;

/**
 *
 * @author jlocker
 */
public class JurorDetailsSkin extends ContainerSkin {

    private static final Font fontOrig = (Font) new Label().getStyles().get("font");
    private static final Font fontBold = fontOrig.deriveFont(fontOrig.getStyle() | Font.BOLD);
    private static final ListItem busyDisabledItem = new ListItem(Images.getImage("script_edit_disabled.png"));
    private static final ListItem busyItem = new ListItem(Images.getImage("script_edit.png"));
    private static final ListItem idleItem = new ListItem(Images.getImage("cup.png"));
    private static final ListItem awayItem = new ListItem(Images.getImage("delete.png"));
    private static final Filter<ListItem> filter = new Filter<ListItem>() {
        @Override
        public boolean include(ListItem item) {
            return item == busyDisabledItem;
        }
    };
    private final Map<JurorDay, ListButton> stateButtons = new HashMap<>();
    private final Map<JurorDay, Label> roundLables = new HashMap<>();
    private Component content;
    private Label fullNameLabel;
    private BoxPane conflictsBoxPane;
    private Checkbox independentCheckbox;
    private Checkbox chairCheckbox;
    private Label biasLabel;
    private Meter loadMeter;
    private Color loadOkColor;
    private Color loadNokColor = Color.RED.darker();
    private TablePane jurorScheduleTablePane;
    private PushButton revertButton;
    private PushButton saveButton;

    @Override
    public void install(Component component) {
        super.install(component);
        JurorDetails details = (JurorDetails) component;

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (Component) bxmlSerializer.readObject(GroupRosterSkin.class, "juror_details.bxml");
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }
        details.add(content);
        fullNameLabel = (Label) bxmlSerializer.getNamespace().get("fullNameLabel");
        conflictsBoxPane = (BoxPane) bxmlSerializer.getNamespace().get("conflictsBoxPane");
        independentCheckbox = (Checkbox) bxmlSerializer.getNamespace().get("independentCheckbox");
        chairCheckbox = (Checkbox) bxmlSerializer.getNamespace().get("chairCheckbox");
        biasLabel = (Label) bxmlSerializer.getNamespace().get("biasLabel");
        loadMeter = (Meter) bxmlSerializer.getNamespace().get("loadMeter");
        loadOkColor = (Color) loadMeter.getStyles().get("color");
        jurorScheduleTablePane = (TablePane) bxmlSerializer.getNamespace().get("jurorScheduleTablePane");
        revertButton = (PushButton) bxmlSerializer.getNamespace().get("revertButton");
        saveButton = (PushButton) bxmlSerializer.getNamespace().get("saveButton");
        revertButton.setEnabled(false);
        saveButton.setEnabled(false);

        revertButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                revert();
            }
        });

        saveButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                save();
            }
        });
    }

    @Override
    public int getPreferredWidth(int height) {
        return content.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return content.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return content.getPreferredSize();
    }

    @Override
    public void layout() {
        content.setLocation(0, 0);
        content.setSize(getWidth(), getHeight());
    }

    public void showJuror(Juror juror) {
        JurorDetails details = (JurorDetails) getComponent();
        TournamentSolver solver = details.getSolver();

        // full name
        fullNameLabel.setText(juror.fullName());
        conflictsBoxPane.removeAll();
        for (CountryCode cc : solver.getConflicts(juror)) {
            ImageView flag = new ImageView(Images.getFlag(cc));
            flag.setTooltipText(CountryCodeIO.getShortName(cc));
            flag.setTooltipDelay(200);
            conflictsBoxPane.add(flag);
        }

        // independent status
        independentCheckbox.setState(juror.getType() == JurorType.INDEPENDENT ? Button.State.SELECTED : Button.State.UNSELECTED);

        // chair candidate status
        chairCheckbox.setState(juror.isChairCandidate() ? Button.State.SELECTED : Button.State.UNSELECTED);

        // bias
        biasLabel.setText(String.format("%+.2f", juror.getBias()));
        Component.StyleDictionary biasStyles = biasLabel.getStyles();
        if (juror.getBias() > 0) {
            biasStyles.put("color", Color.RED);
        } else if (juror.getBias() < 0) {
            biasStyles.put("color", Color.BLUE);
        } else {
            biasStyles.put("color", Color.BLACK);
        }

        // load
        JurorLoad load = solver.getLoad(juror);
        loadMeter.setPercentage(load.getLoad());
        loadMeter.setText(String.format("%.2f", load.getLoad()));
        Component.StyleDictionary loadStyles = loadMeter.getStyles();
        if (load.isExcessive()) {
            loadStyles.put("color", loadNokColor);
        } else {
            loadStyles.put("color", loadOkColor);
        }

        // schedule
        roundLables.clear();
        stateButtons.clear();
        jurorScheduleTablePane.getRows().remove(0, jurorScheduleTablePane.getRows().getLength());
        final java.util.List<JurorDay> jurorDays = solver.getJurorDays(juror);
        for (final JurorDay jurorDay : jurorDays) {
            TablePane.Row row = new TablePane.Row();
            jurorScheduleTablePane.getRows().add(row);

            final Label label = new Label(jurorDay.getRound().toString());
            roundLables.put(jurorDay, label);
            row.add(label);

            ListButton listButton = new ListButton();
            stateButtons.put(jurorDay, listButton);
            listButton.setDisabledItemFilter(filter);
            ListViewItemRenderer renderer = new ListViewItemRenderer();
            renderer.setShowIcon(true);
            listButton.setItemRenderer(renderer);
            List<ListItem> list = new ArrayList<>(3);
            listButton.setListData(list);
            row.add(listButton);

            if (jurorDay.getStatus() == JurorDay.Status.ASSIGNED) {
                list.add(busyItem);
                BoxPane teams = new BoxPane();
                row.add(teams);
                teams.add(new Label(jurorDay.getGroup().getName()));
                for (Team team : jurorDay.getGroup().getTeams()) {
                    teams.add(new ImageView(Images.getFlag(team.getCountry())));
                }
            } else {
                list.add(busyDisabledItem);
            }
            list.add(idleItem);
            list.add(awayItem);
            setSelectedIndex(listButton, jurorDay);

            revertButton.setEnabled(false);
            saveButton.setEnabled(false);

            listButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {
                @Override
                public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
                    boolean dirty;
                    dirty = jurorDay.change(JurorDay.Status.values()[listButton.getSelectedIndex()]);
                    // if this button is dirty, higlight it's label
                    label.getStyles().put("font", dirty ? fontBold : fontOrig);

                    // now decide if revert and save button should be enabled
                    if (!dirty) {
                        // need to check the rest
                        for (JurorDay jd : jurorDays) {
                            if (jd.isDirty()) {
                                dirty |= true;
                            }
                        }
                    }
                    revertButton.setEnabled(dirty);
                    saveButton.setEnabled(dirty);
                }
            });
        }

    }

    private void setSelectedIndex(ListButton button, JurorDay jd) {
        button.setSelectedIndex(jd.getStatus().ordinal());
    }

    private void revert() {
        JurorDetails details = (JurorDetails) getComponent();
        TournamentSolver solver = details.getSolver();

        for (JurorDay day : solver.getJurorDays(details.getJuror())) {
            if (day.isDirty()) {
                setSelectedIndex(stateButtons.get(day), day);
                roundLables.get(day).getStyles().put("font", fontOrig);
                day.reset();
            }
        }
    }

    private void save() {
        JurorDetails details = (JurorDetails) getComponent();
        details.saveChanges();
    }
}
