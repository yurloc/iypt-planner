package org.iypt.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jlocker
 */
public class Group {

    private String name;
    private List<Team> teams; // no rep, opp, rev, obs because these change during stages of the round
    private Round round;
    private Jury jury;

    public Group(Team... teams) {
        this.teams = new ArrayList<Team>();
        Collections.addAll(this.teams, teams);
        jury = new Jury();
        jury.setGroup(this);
    }

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
     * Set the value of teams
     *
     * @param teams new value of teams
     */
    public void setTeams(List<Team> teams) {
        this.teams = teams;
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
