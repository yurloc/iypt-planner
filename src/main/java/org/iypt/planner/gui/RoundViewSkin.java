package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.util.List;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Team;

/**
 *
 * @author jlocker
 */
public class RoundViewSkin extends ContainerSkin implements RoundViewListener {

    private BoxPane content;
    private GroupRoster[] views;

    @Override
    public void install(Component component) {
        super.install(component);

        // get component and register skin as a listener
        RoundView round = (RoundView) component;
        round.getRoundViewListeners().add(this);

        // create content and add it to component
        content = new BoxPane(Orientation.HORIZONTAL);
        round.add(content);

        // initialize group views
        List<Group> groups = round.getRound().getGroups();
        views = new GroupRoster[groups.size()];
        for (int i = 0; i < views.length; i++) {
            Room room = createRoom(groups.get(i));
            GroupRoster view = new GroupRoster(room);
            views[i] = view;
            content.add(view);
        }

        // TODO register group listeners
    }

    private Room createRoom(Group group) {
        RoundView round = (RoundView) getComponent();
        boolean isLocked = round.getSchedule().getTournament().isLocked(round.getRound());

        ArrayList<CountryCode> teams = new ArrayList<>();
        for (Team team : group.getTeams()) {
            teams.add(team.getCountry());
        }
        ArrayList<SeatInfo> seats = new ArrayList<>();
        for (SeatInfo seat : round.getSchedule().getSeats(group)) {
            seats.add(seat);
        }
        return new Room(group.getName(), teams, seats, isLocked);
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
    public void scheduleChanged(RoundView round) {
        List<Group> groups = round.getRound().getGroups();
        for (int i = 0; i < views.length; i++) {
            views[i].update(createRoom(groups.get(i)));
        }
    }
}
