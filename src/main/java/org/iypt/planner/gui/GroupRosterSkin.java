package org.iypt.planner.gui;

import java.io.IOException;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.CountryCode;

/**
 *
 * @author jlocker
 */
public class GroupRosterSkin extends ContainerSkin implements GroupRosterListener {

    private Component content;
    private BoxPane groupNameBoxPane;
    private Label groupNameLabel;
    private BoxPane teamsBoxPane;
    private TableView juryTableView;

    @Override
    public void install(Component component) {
        super.install(component);
        GroupRoster group = (GroupRoster) component;
        group.getGroupRosterListeners().add(this);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (Component) bxmlSerializer.readObject(GroupRosterSkin.class, "group_skin.bxml");
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }
        group.add(content);
        groupNameBoxPane = (BoxPane) bxmlSerializer.getNamespace().get("groupNameBoxPane");
        groupNameLabel = (Label) bxmlSerializer.getNamespace().get("groupNameLabel");
        teamsBoxPane = (BoxPane) bxmlSerializer.getNamespace().get("teamsBoxPane");
        juryTableView = (TableView) bxmlSerializer.getNamespace().get("juryTableView");

        groupRosterChanged(group);
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

    @Override
    public void groupRosterChanged(GroupRoster group) {
        groupNameLabel.setText(group.getGroupName());
        teamsBoxPane.removeAll();
        for (CountryCode country : group.getTeams()) {
            ImageView teamFlag = new ImageView(Images.getFlag(country));
            teamFlag.setTooltipText(country.getName());
            teamFlag.setTooltipDelay(200);
            teamsBoxPane.add(teamFlag);
        }

        juryTableView.setTableData(group.getJurorList());
    }
}
