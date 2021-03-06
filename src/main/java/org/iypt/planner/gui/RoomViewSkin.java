package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.adapter.ListAdapter;
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
import org.iypt.planner.domain.util.CountryCodeIO;

/**
 *
 * @author jlocker
 */
public class RoomViewSkin extends ContainerSkin implements RoomViewListener {

    private Component content;
    @BXML private Label groupNameLabel;
    @BXML private BoxPane teamsBoxPane;
    @BXML private TableView juryTableView;
    private Dimensions preferredSize = null;
    private int rowIndex;
    private final Action lockInAction = new Action() {
        @Override
        public void perform(Component source) {
            RoomView room = (RoomView) getComponent();
            room.lockIn(rowIndex);
        }
    };
    private final Action lockOutAction = new Action() {
        @Override
        public void perform(Component source) {
            RoomView room = (RoomView) getComponent();
            room.lockOut(rowIndex);
        }
    };
    private final Action unlockAction = new Action() {
        @Override
        public void perform(Component source) {
            RoomView room = (RoomView) getComponent();
            room.unlock(rowIndex);
        }
    };

    @Override
    public void install(Component component) {
        super.install(component);

        // get component and register skin as a listener
        final RoomView room = (RoomView) component;
        room.getRoomViewListenerList().add(this);

        // read BXML
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (Component) bxmlSerializer.readObject(RoomViewSkin.class, "room_skin.bxml");
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // add it to container
        room.add(content);

        // initialize fields with elements from BXML
        bxmlSerializer.bind(this, RoomViewSkin.class);

        // register listeners
        juryTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                room.setSelectedSeat((SeatInfo) tableView.getSelectedRow());
            }
        });
        juryTableView.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (component.isFocused()) {
                    room.setSelectedSeat((SeatInfo) ((TableView) component).getSelectedRow());
                }
            }
        });
        final Menu.Section section = new Menu.Section();
        Menu.Item li = new Menu.Item("Lock");
        li.setAction(lockInAction);
        section.add(li);

        Menu.Item ul = new Menu.Item("Unlock");
        ul.setAction(unlockAction);
        section.add(ul);

        //Menu.Item lo = new Menu.Item("Lock-out");
        //lo.setAction(lockOutAction);
        //section.add(lo);
        juryTableView.setMenuHandler(new MenuHandler.Adapter() {
            @Override
            public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
                menu.getSections().add(section);
                rowIndex = juryTableView.getRowAt(y);
                return false;
            }
        });
        lockOutAction.setEnabled(false);

        roomChanged();
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
    public void roomChanged() {
        RoomView room = (RoomView) getComponent();
        groupNameLabel.setText(room.getGroupName());
        teamsBoxPane.removeAll();
        for (CountryCode country : room.getTeams()) {
            ImageView teamFlag = new ImageView(Images.getFlag(country));
            teamFlag.setTooltipText(CountryCodeIO.getShortName(country));
            teamFlag.setTooltipDelay(200);
            teamsBoxPane.add(teamFlag);
        }

        if (juryTableView.getTableData().getLength() != room.getSeats().size()) {
            preferredSize = null;
        }

        juryTableView.setTableData(new ListAdapter<>(room.getSeats()));
        juryTableView.setEnabled(!room.isLocked());
    }

    @Override
    public void seatSelected(RoomView room, SeatInfo previousSeat) {
        // do nothing
    }

    @Override
    public void seatLockChanged(RoomView room, SeatInfo seat) {
        // do nothing
    }
}
