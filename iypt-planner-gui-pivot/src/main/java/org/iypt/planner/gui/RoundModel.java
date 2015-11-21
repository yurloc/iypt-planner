package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.util.ArrayList;
import java.util.List;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;

public class RoundModel {

    private final Round round;
    private final boolean locked;
    private final List<Room> rooms;
    private List<SeatInfo> idle;
    private List<SeatInfo> away;

    public RoundModel(Tournament tournament, Round round) {
        this.round = round;
        locked = tournament.isLocked(round);
        rooms = new ArrayList<>();
        for (Group group : round.getGroups()) {
            ArrayList<CountryCode> teams = new ArrayList<>();
            for (Team team : group.getTeams()) {
                teams.add(team.getCountry());
            }
            ArrayList<SeatInfo> seats = new ArrayList<>();
            for (Seat seat : tournament.getSeats(group.getJury())) {
                SeatInfo seatInfo = SeatInfo.newInstance(seat);
                if (tournament.isLocked(seat)) {
                    seatInfo.lock();
                }
                seats.add(seatInfo);
            }
            rooms.add(new Room(group.getName(), teams, seats, locked));
        }
    }

    public Round getRound() {
        return round;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    boolean isLocked() {
        return locked;
    }

    public int getNumber() {
        return round.getNumber();
    }

    public void setIdle(List<SeatInfo> idle) {
        this.idle = idle;
    }

    public void setAway(List<SeatInfo> away) {
        this.away = away;
    }

    public List<SeatInfo> getIdle() {
        return idle;
    }

    public List<SeatInfo> getAway() {
        return away;
    }

    @Override
    public String toString() {
        return "Round #" + round.getNumber();
    }
}
