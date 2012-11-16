package org.iypt.planner.gui;

import java.awt.Color;
import java.io.IOException;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.content.ListItem;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.CountryCode;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorLoad;
import org.iypt.planner.domain.JurorType;
import org.iypt.planner.domain.Team;
import org.iypt.planner.solver.TournamentSolver;

/**
 *
 * @author jlocker
 */
public class JurorDetailsSkin extends ContainerSkin {

    private Component content;
    private Label fullNameLabel;
    private BoxPane conflictsBoxPane;
    private Checkbox independentCheckbox;
    private Checkbox chairCheckbox;
    private Label biasLabel;
    private Meter loadMeter;
    private TablePane jurorScheduleTablePane;
    private Color loadOkColor;
    private Color loadNokColor = Color.RED.darker();

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
            flag.setTooltipText(cc.getName());
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
        jurorScheduleTablePane.getRows().remove(0, jurorScheduleTablePane.getRows().getLength());
        for (JurorDay jurorDay : solver.getJurorDays(juror)) {
            TablePane.Row row = new TablePane.Row();
            jurorScheduleTablePane.getRows().add(row);
            row.add(new Label(jurorDay.getRound().toString()));
            ListButton listButton = new ListButton();
            ListViewItemRenderer renderer = new ListViewItemRenderer();
            renderer.setShowIcon(true);
            listButton.setItemRenderer(renderer);
            List<ListItem> list = new ArrayList<>(3);
            listButton.setListData(list);
            row.add(listButton);
            switch (jurorDay.getStatus()) {
                case AWAY:
                    list.add(new ListItem(Images.getImage("cup.png")));
                    list.add(new ListItem(Images.getImage("delete.png")));
                    listButton.setSelectedIndex(0);
                    break;
                case IDLE:
                    list.add(new ListItem(Images.getImage("cup.png")));
                    list.add(new ListItem(Images.getImage("delete.png")));
                    listButton.setSelectedIndex(1);
                    break;
                case ASSIGNED:
                    list.add(new ListItem(Images.getImage("script_edit.png")));
                    list.add(new ListItem(Images.getImage("cup.png")));
                    list.add(new ListItem(Images.getImage("delete.png")));
                    listButton.setSelectedIndex(0);
                    BoxPane teams = new BoxPane();
                    row.add(teams);
                    teams.add(new Label(jurorDay.getGroup().getName()));
                    for (Team team : jurorDay.getGroup().getTeams()) {
                        teams.add(new ImageView(Images.getFlag(team.getCountry())));
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

}
