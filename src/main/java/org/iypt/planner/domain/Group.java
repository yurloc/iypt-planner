package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jlocker
 */
public final class Group {

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
     * Get the value of jury
     *
     * @return the value of jury
     */
    public Jury getJury() {
        return jury;
    }

    /**
     * Set the value of jury
     *
     * @param jury new value of jury
     */
    public void setJury(Jury jury) {
        this.jury = jury;
    }

    /**
     * Get the value of round
     *
     * @return the value of round
     */
    public Round getRound() {
        return round;
    }

    /**
     * Set the value of round
     *
     * @param round new value of round
     */
    public void setRound(Round round) {
        this.round = round;
    }

    /**
     * Get the value of size
     *
     * @return the value of size
     */
    public int getSize() {
        return getTeams().size();
    }

    /**
     * Get the value of teams
     *
     * @return the value of teams
     */
    public List<Team> getTeams() {
        return teams;
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.name = name;
    }

}
