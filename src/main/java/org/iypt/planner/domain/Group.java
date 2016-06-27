package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jlocker
 */
public final class Group implements Comparable<Group> {

    private String name;
    private List<Team> teams;
    private Round round;
    private Jury jury;

    private Group() {
        jury = createJury();
        teams = new ArrayList<>(4);
    }

    protected String coords() {
        return round.getNumber() + name;
    }

    public Group(Team... teams) {
        this();
        addTeams(teams);
    }

    public Group(String name) {
        this();
        this.name = name;
    }

    private Jury createJury() {
        jury = new Jury();
        jury.setGroup(this);
        return jury;
    }

    public Group addTeam(Team team) {
        teams.add(team);
        return this;
    }

    public Group addTeams(Team... teams) {
        Collections.addAll(this.teams, teams);
        return this;
    }

    @Override
    public String toString() {
        return String.format("Group %s", coords());
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // Getters & Setters
    //-------------------------------------------------------------------------------------------------------------------------
    /**
     * Get the jury that is assigned to this group.
     *
     * @return the jury assigned to this group
     */
    public Jury getJury() {
        return jury;
    }

    /**
     * Get the round this group is part of.
     *
     * @return the round of this group
     */
    public Round getRound() {
        return round;
    }

    /**
     * Set the round this group belongs to.
     *
     * @param round the round this group belongs to.
     */
    public void setRound(Round round) {
        this.round = round;
    }

    /**
     * Get the number of teams in this group. Most of the groups consist of 3 teams. There can be zero, one or two groups with
     * 4 teams, depending on the total number of teams taking part in the tournament.
     *
     * @return number of team in this group
     */
    public int getSize() {
        return getTeams().size();
    }

    /**
     * Get teams in this group.
     *
     * @return teams in this group.
     */
    public List<Team> getTeams() {
        return teams;
    }

    /**
     * Get the group name.
     *
     * @return group name, typically a capital letter
     */
    public String getName() {
        return name;
    }

    /**
     * Set the group name.
     *
     * @param name new group name, typically a capital letter
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Group other) {
        if (round.equals(other.round)) {
            return name.compareTo(other.name);
        }
        return round.compareTo(other.round);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.round);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Group other = (Group) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.round, other.round)) {
            return false;
        }
        return true;
    }
}
