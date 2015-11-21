package org.iypt.planner.gui;

import java.util.List;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 *
 * @author jlocker
 */
public class RoundViewSkin extends ContainerSkin implements RoundViewListener {

    private BoxPane content;
    private RoomView[] roomViews;

    @Override
    public void install(Component component) {
        super.install(component);

        // get component and register skin as a listener
        final RoundView round = (RoundView) component;
        round.getRoundViewListeners().add(this);

        // create content and add it to component
        content = new BoxPane(Orientation.HORIZONTAL);
        round.add(content);

        // initialize group views
        List<Room> rooms = round.getRooms();
        roomViews = new RoomView[rooms.size()];
        for (int i = 0; i < roomViews.length; i++) {
            Room room = rooms.get(i);
            RoomView view = new RoomView(room);
            roomViews[i] = view;
            content.add(view);
            view.getRoomViewListenerList().add(new RoomViewListener() {

                @Override
                public void roomChanged() {
                    // do nothing
                }

                @Override
                public void seatSelected(RoomView room, SeatInfo previousSeat) {
                    round.seatSelected(room.getSelectedSeat());
                }

                @Override
                public void seatLockChanged(RoomView room, SeatInfo seat) {
                    round.seatLockChanged(seat);
                }
            });
        }
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
    public void roundChanged() {
        RoundView round = (RoundView) getComponent();
        List<Room> rooms = round.getRooms();
        for (int i = 0; i < roomViews.length; i++) {
            roomViews[i].update(rooms.get(i));
        }
    }

    @Override
    public void seatSelected(SeatInfo seat) {
        // do nothing
    }

    @Override
    public void seatLockChanged(SeatInfo seat) {
        // do nothing
    }
}
