package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Team;
import org.iypt.planner.solver.TournamentSolver;

public class RoundModel {

    private final Round round;
    private final boolean locked;
    private final List<Room> rooms;

    public RoundModel(TournamentSolver solver, Round round) {
        this.round = round;
        locked = solver.getTournament().isLocked(round);
        rooms = new ArrayList<>();
        for (Group group : round.getGroups()) {
            ArrayList<CountryCode> teams = new ArrayList<>();
            for (Team team : group.getTeams()) {
                teams.add(team.getCountry());
            }
            ArrayList<SeatInfo> seats = new ArrayList<>();
            for (Seat seat : solver.getTournament().getSeats(group.getJury())) {
                SeatInfo seatInfo = SeatInfo.newInstance(seat);
                if (solver.getTournament().isLocked(seat)) {
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

    @Override
    public String toString() {
        return "Round #" + round.getNumber();
    }
}
