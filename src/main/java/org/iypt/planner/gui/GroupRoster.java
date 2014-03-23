package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Team;

/**
 *
 * @author jlocker
 */
public class GroupRoster extends Container {


    private static final class GroupRosterListenerList extends ListenerList<GroupRosterListener> implements GroupRosterListener {

        @Override
        public void groupRosterChanged(GroupRoster group) {
            for (GroupRosterListener listener : this) {
                listener.groupRosterChanged(group);
            }
        }
    }
    private GroupRosterListenerList groupRosterListenerList = new GroupRosterListenerList();
    private TournamentSchedule schedule;
    private Group group;
    private List<SeatInfo> seats = new ArrayList<>();
    private boolean roundLocked;

    boolean isRoundLocked() {
        return roundLocked;
    }

    // TODO move this into the skin?
    void jurorSelected(Object row) {
        if (row != null) {
            SeatInfo seat = (SeatInfo) row;
            schedule.seatSelected(seat);
        }
    }

    void lockIn(int rowIndex) {
        SeatInfo seat = getJurorList().get(rowIndex);
        schedule.lockSeat(seat);
    }

    void lockOut(int rowIndex) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void unlock(int rowIndex) {
        SeatInfo seat = getJurorList().get(rowIndex);
        schedule.unlockSeat(seat);
    }

    public GroupRoster() {
        super();
        setSkin(new GroupRosterSkin());
    }

    GroupRoster(TournamentSchedule schedule, Group group) {
        this.schedule = schedule;
        this.group = group;
        updateJurors();

        setSkin(new GroupRosterSkin());
    }

    public String getGroupName() {
        return group.getName();
    }

    public List<CountryCode> getTeams() {
        List<CountryCode> teams = new ArrayList<>();
        for (Team team : group.getTeams()) {
            teams.add(team.getCountry());
        }
        return teams;
    }

    private void updateJurors() {
        seats = new ArrayList<>();
        for (SeatInfo seat : schedule.getSeats(group)) {
            seats.add(seat);
        }
        roundLocked = schedule.getTournament().isLocked(group.getRound());
    }

    public List<SeatInfo> getJurorList() {
        return seats;
    }

    void update(Group group) {
        this.group = group;
        updateJurors();
        groupRosterListenerList.groupRosterChanged(this);
    }

    public ListenerList<GroupRosterListener> getGroupRosterListeners() {
        return groupRosterListenerList;
    }
}
