package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import org.apache.pivot.beans.BXML;
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
import org.apache.pivot.wtk.LinkButton;
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
class JurorDetailsSkin extends ContainerSkin implements JurorDetailsListener {

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
    private Component content;
    private Color loadOkColor;
    private Color loadNokColor = Color.RED.darker();
    @BXML private Label fullNameLabel;
    @BXML private BoxPane conflictsBoxPane;
    @BXML private Checkbox independentCheckbox;
    @BXML private Checkbox chairCheckbox;
    @BXML private Checkbox experiencedCheckbox;
    @BXML private Label biasLabel;
    @BXML private Meter loadMeter;
    @BXML private TablePane jurorScheduleTablePane;
    @BXML private PushButton revertButton;
    @BXML private PushButton saveButton;

    @Override
    public void install(Component component) {
        super.install(component);

        // get component and register skin as a listener
        final JurorDetails details = (JurorDetails) component;
        details.getDetailsListeners().add(this);

        // read BXML
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (Component) bxmlSerializer.readObject(JurorDetailsSkin.class, "juror_details.bxml");
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // add it to container
        details.add(content);

        // initialize fields with elements from BXML
        bxmlSerializer.bind(this, JurorDetailsSkin.class);

        // set up controls
        loadOkColor = (Color) loadMeter.getStyles().get("color");
        revertButton.setEnabled(false);
        saveButton.setEnabled(false);

        // register listeners
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

        jurorChanged();
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

    private void showJuror(final JurorInfo jurorInfo) {
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

        // chair experience status
        experiencedCheckbox.setState(juror.isExperienced() ? Button.State.SELECTED : Button.State.UNSELECTED);

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
        jurorScheduleTablePane.getRows().remove(0, jurorScheduleTablePane.getRows().getLength());
        for (final JurorAssignment assignment : jurorInfo.getSchedule()) {
            TablePane.Row roundRow = new TablePane.Row();
            jurorScheduleTablePane.getRows().add(roundRow);

            final LinkButton roundButton = new LinkButton(assignment.getRound().toString());
            roundRow.add(roundButton);
            roundButton.getButtonPressListeners().add(new ButtonPressListener() {
                @Override
                public void buttonPressed(Button button) {
                    ((JurorDetails) getComponent()).selectAssignment(assignment);
                }
            });

            ListButton roundStatusListButton = new ListButton();
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
                    ((JurorDetails) getComponent()).changeAssignment(assignment, listButton.getSelectedIndex());
                }
            });
        }
        // finally render the schedule
        updateAssignments(jurorInfo);
    }

    private void updateAssignments(JurorInfo jurorInfo) {
        boolean scheduleHasChanges = false;
        for (JurorAssignment assignment : jurorInfo.getSchedule()) {
            // update overall dirty flag
            scheduleHasChanges = scheduleHasChanges |= assignment.isDirty();
            int rowIndex = assignment.getRound().getNumber() - 1;
            // if this assignment is dirty, higlight the round label
            Component label = jurorScheduleTablePane.getRows().get(rowIndex).get(0);
            label.getStyles().put("font", assignment.isDirty() ? fontBold : fontOrig);
            // set selected status
            ListButton statusChoiceButton = (ListButton) jurorScheduleTablePane.getRows().get(rowIndex).get(1);
            statusChoiceButton.setSelectedIndex(assignment.getCurrentStatus().ordinal());
        }

        // finally enable/disable save & revert buttons
        revertButton.setEnabled(scheduleHasChanges);
        saveButton.setEnabled(scheduleHasChanges);
    }

    @Override
    public void jurorChanged() {
        JurorDetails details = (JurorDetails) getComponent();
        if (details.getJurorInfo() != null) {
            showJuror(details.getJurorInfo());
        }
    }

    @Override
    public void jurorAssignmentChanged() {
        JurorDetails details = (JurorDetails) getComponent();
        updateAssignments(details.getJurorInfo());
    }

    @Override
    public void jurorChangesSaved(JurorDetails details) {
        updateAssignments(details.getJurorInfo());
    }

    @Override
    public void jurorAssignmentSelected(JurorAssignment assignment) {
        // do nothing
    }
}
