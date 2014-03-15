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

/**
 *
 * @author jlocker
 */
class JurorDetailsSkin extends ContainerSkin {

    private static final Font fontOrig = (Font) new Label().getStyles().get("font");
    private static final Font fontBold = fontOrig.deriveFont(fontOrig.getStyle() | Font.BOLD);
    private static final ListItem busyDisabledItem = new ListItem(Images.getImage("script_edit_disabled.png"));
    private static final ListItem busyItem = new ListItem(Images.getImage("script_edit.png"));
    private static final ListItem idleItem = new ListItem(Images.getImage("cup.png"));
    private static final ListItem awayItem = new ListItem(Images.getImage("delete.png"));
    private static final Filter<ListItem> statusChoiceFilter = new Filter<ListItem>() {
        @Override
        public boolean include(ListItem item) {
            return item == busyDisabledItem;
        }
    };
    private final Map<JurorAssignment, ListButton> roundStatusMap = new HashMap<>();
    private final Map<JurorAssignment, Label> roundLabelMap = new HashMap<>();
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
        final JurorDetails details = (JurorDetails) component;

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
                details.revertSchedule();
            }
        });

        saveButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                details.saveChanges();
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

    void showJuror(final JurorInfo jurorInfo) {
        Juror juror = jurorInfo.getJuror();

        // full name
        fullNameLabel.setText(juror.fullName());
        conflictsBoxPane.removeAll();
        for (CountryCode cc : jurorInfo.getConflicts()) {
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
        JurorLoad load = jurorInfo.getLoad();
        loadMeter.setPercentage(load.getLoad());
        loadMeter.setText(String.format("%.2f", load.getLoad()));
        Component.StyleDictionary loadStyles = loadMeter.getStyles();
        if (load.isExcessive()) {
            loadStyles.put("color", loadNokColor);
        } else {
            loadStyles.put("color", loadOkColor);
        }

        // schedule
        roundLabelMap.clear();
        roundStatusMap.clear();
        jurorScheduleTablePane.getRows().remove(0, jurorScheduleTablePane.getRows().getLength());
        for (final JurorAssignment assignment : jurorInfo.getSchedule()) {
            TablePane.Row roundRow = new TablePane.Row();
            jurorScheduleTablePane.getRows().add(roundRow);

            final Label roundLabel = new Label(assignment.getRound().toString());
            roundLabelMap.put(assignment, roundLabel);
            roundRow.add(roundLabel);

            ListButton roundStatusListButton = new ListButton();
            roundStatusMap.put(assignment, roundStatusListButton);
            roundStatusListButton.setDisabledItemFilter(statusChoiceFilter);
            ListViewItemRenderer renderer = new ListViewItemRenderer();
            renderer.setShowIcon(true);
            roundStatusListButton.setItemRenderer(renderer);
            List<ListItem> statusChoiceList = new ArrayList<>(JurorAssignment.Status.values().length);
            roundStatusListButton.setListData(statusChoiceList);
            roundRow.add(roundStatusListButton);

            if (assignment.getCurrentStatus() == JurorAssignment.Status.ASSIGNED) {
                statusChoiceList.add(busyItem);
                BoxPane teams = new BoxPane();
                roundRow.add(teams);
                teams.add(new Label(assignment.getGroup().getName()));
                for (Team team : assignment.getGroup().getTeams()) {
                    teams.add(new ImageView(Images.getFlag(team.getCountry())));
                }
            } else {
                statusChoiceList.add(busyDisabledItem);
            }
            statusChoiceList.add(idleItem);
            statusChoiceList.add(awayItem);

            roundStatusListButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {
                @Override
                public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
                    ((JurorDetails) getComponent()).changeStatus(assignment, listButton.getSelectedIndex());
                }
            });
        }
        // finally render the schedule
        renderSchedule(jurorInfo);
    }

    void renderSchedule(JurorInfo jurorInfo) {
        boolean scheduleHasChanges = false;
        for (JurorAssignment assignment : jurorInfo.getSchedule()) {
            // update overall dirty flag
            scheduleHasChanges = scheduleHasChanges |= assignment.isDirty();
            // if this assignment is dirty, higlight the round label
            roundLabelMap.get(assignment).getStyles().put("font", assignment.isDirty() ? fontBold : fontOrig);
            // set selected status
            ListButton statusChoiceButton = roundStatusMap.get(assignment);
            statusChoiceButton.setSelectedIndex(assignment.getCurrentStatus().ordinal());
        }

        // finally enable/disable save & revert buttons
        revertButton.setEnabled(scheduleHasChanges);
        saveButton.setEnabled(scheduleHasChanges);
    }
}
