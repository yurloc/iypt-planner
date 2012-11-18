package org.iypt.planner.gui;

import java.io.IOException;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.CountryCode;

/**
 *
 * @author jlocker
 */
public class GroupRosterSkin extends ContainerSkin implements GroupRosterListener {

    private Component content;
    private Label groupNameLabel;
    private BoxPane teamsBoxPane;
    private TableView juryTableView;
    private Dimensions preferredSize = null;
    private int rowIndex;
    private final Action lockInAction = new Action() {
        @Override
        public void perform(Component source) {
            GroupRoster group = (GroupRoster) getComponent();
            group.lockIn(rowIndex);
        }
    };
    private final Action lockOutAction = new Action() {
        @Override
        public void perform(Component source) {
            GroupRoster group = (GroupRoster) getComponent();
            group.lockOut(rowIndex);
        }
    };
    private final Action unlockAction = new Action() {
        @Override
        public void perform(Component source) {
            GroupRoster group = (GroupRoster) getComponent();
            group.unlock(rowIndex);
        }
    };

    @Override
    public void install(Component component) {
        super.install(component);
        final GroupRoster group = (GroupRoster) component;
        group.getGroupRosterListeners().add(this);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (Component) bxmlSerializer.readObject(GroupRosterSkin.class, "group_skin.bxml");
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }
        group.add(content);
        groupNameLabel = (Label) bxmlSerializer.getNamespace().get("groupNameLabel");
        teamsBoxPane = (BoxPane) bxmlSerializer.getNamespace().get("teamsBoxPane");
        juryTableView = (TableView) bxmlSerializer.getNamespace().get("juryTableView");
        juryTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                group.jurorSelected(tableView.getSelectedRow());
            }
        });
        juryTableView.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (component.isFocused()) {
                    group.jurorSelected(((TableView) component).getSelectedRow());
                }
            }
        });
        final Menu.Section section = new Menu.Section();
        Menu.Item li = new Menu.Item("Lock-in");
        Menu.Item lo = new Menu.Item("Lock-out");
        Menu.Item ul = new Menu.Item("Unlock");
        li.setAction(lockInAction);
        lo.setAction(lockOutAction);
        ul.setAction(unlockAction);
        section.add(li);
        section.add(lo);
        section.add(ul);
        juryTableView.setMenuHandler(new MenuHandler.Adapter() {
            @Override
            public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
                menu.getSections().add(section);
                rowIndex = juryTableView.getRowAt(y);
                return false;
            }
        });
        lockOutAction.setEnabled(false);

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
        if (preferredSize == null) {
            preferredSize = content.getPreferredSize();
        }
        return preferredSize;
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
