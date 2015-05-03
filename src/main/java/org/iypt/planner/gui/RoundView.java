package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Team;

/**
 *
 * @author jlocker
 */
public class RoundView extends Container {

    private static final class RoundViewListenerList extends ListenerList<RoundViewListener> implements RoundViewListener {

        @Override
        public void roundChanged(RoundView round) {
            for (RoundViewListener listener : this) {
                listener.roundChanged(round);
            }
        }

        @Override
        public void seatSelected(SeatInfo seat) {
            for (RoundViewListener listener : this) {
                listener.seatSelected(seat);
            }
        }

        @Override
        public void seatLockChanged(SeatInfo seat) {
            for (RoundViewListener listener : this) {
                listener.seatLockChanged(seat);
            }
        }
    }
    private final RoundViewListenerList roundViewListeners = new RoundViewListenerList();
    private final TournamentSchedule schedule;
    private Round round;

    public RoundView(TournamentSchedule schedule, Round round) {
        this.schedule = schedule;
        this.round = round;
        setSkin(new RoundViewSkin());
    }

    public void update(Round round) {
        this.round = round;
        roundViewListeners.roundChanged(this);
    }

    public Round getRound() {
        return round;
    }

    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<>();
        for (Group group : round.getGroups()) {
            ArrayList<CountryCode> teams = new ArrayList<>();
            for (Team team : group.getTeams()) {
                teams.add(team.getCountry());
            }
            ArrayList<SeatInfo> seats = new ArrayList<>();
            for (SeatInfo seat : schedule.getSeats(group)) {
                seats.add(seat);
            }
            rooms.add(new Room(group.getName(), teams, seats, isLocked()));
        }
        return rooms;
    }

    public boolean isLocked() {
        return schedule.getTournament().isLocked(round);
    }

    void seatLockChanged(SeatInfo seat) {
        roundViewListeners.seatLockChanged(seat);
    }

    void seatSelected(SeatInfo selectedSeat) {
        roundViewListeners.seatSelected(selectedSeat);
    }

    public ListenerList<RoundViewListener> getRoundViewListeners() {
        return roundViewListeners;
    }
}
