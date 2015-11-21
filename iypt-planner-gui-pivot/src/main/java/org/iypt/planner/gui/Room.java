package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.util.List;

public class Room {

    private final String groupName;
    private final List<CountryCode> teams;
    private final List<SeatInfo> seats;
    private final boolean locked;

    public Room(String groupName, List<CountryCode> teams, List<SeatInfo> seats, boolean locked) {
        this.groupName = groupName;
        this.teams = teams;
        this.seats = seats;
        this.locked = locked;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<CountryCode> getTeams() {
        return teams;
    }

    public List<SeatInfo> getSeats() {
        return seats;
    }

    public boolean isLocked() {
        return locked;
    }
}
