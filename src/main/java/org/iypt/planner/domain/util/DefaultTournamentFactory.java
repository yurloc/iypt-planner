package org.iypt.planner.domain.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Jury;
import org.iypt.planner.domain.JuryMembership;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Team;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public class DefaultTournamentFactory {
    
    private int juryCapacity = 0;
    private List<Juror> jurors = new ArrayList<Juror>();
    private List<Round> rounds = new ArrayList<Round>();
    private List<JuryMembership> memberships = new ArrayList<JuryMembership>();

    /**
     * Get the value of juryCapacity
     *
     * @return the value of juryCapacity
     */
    public int getJuryCapacity() {
        return juryCapacity;
    }

    /**
     * Set the value of juryCapacity
     *
     * @param juryCapacity new value of juryCapacity
     */
    public void setJuryCapacity(int juryCapacity) {
        this.juryCapacity = juryCapacity;
    }
    
    /**
     * Creates a new round and initializes its groups as required by the given number of teams. Day index of the round will be
     * equal to its number.
     * @param number
     * @param teams
     * @return the created round with initialized groups
     */
    public Round createRound(int number, Team... teams) {
        if (juryCapacity < 1) throw new IllegalStateException("Must set juryCapacity prior to creating any round!");
        
        Round r = new Round(number, number);
        
        int[] caps = new int[teams.length / 3];
        
        for (int i = 0; i < teams.length; i++) {
            caps[i % caps.length]++;
        }
        
        int next = 0;
        for (int i = 0; i < caps.length; i++) {
            Group group = r.createGroup(String.valueOf((char) (65 + i)));
            group.setTeams(Arrays.asList(Arrays.copyOfRange(teams, next, next + caps[i])));
            Jury jury = group.createJury(juryCapacity);
            for (int c = 0; c < juryCapacity; c++) {
                memberships.add(new JuryMembership(jury, null));
            }
            next += caps[i];
        }
        rounds.add(r);
        return r;
    }

    public Round createRound(int number, Group... groups) {
        // FIXME remove duplicate code
        if (juryCapacity < 1) throw new IllegalStateException("Must set juryCapacity prior to creating any round!");
        Round r = new Round(number, number);
        r.addGroups(groups);
        char name = 65;
        for (Group group : groups) {
            group.setName(String.valueOf(name++));
            Jury jury = group.createJury(juryCapacity);
            for (int c = 0; c < juryCapacity; c++) {
                memberships.add(new JuryMembership(jury, null));
            }
        }
        rounds.add(r);
        return r;
    }
    
    public void addJurors(Juror... jurors) {
        Collections.addAll(this.jurors, jurors);
    }
    
    public Tournament newTournament() {
        Tournament tournament = new Tournament();
        tournament.setJurors(jurors);
        tournament.setRounds(rounds);
        tournament.setJuryMemberships(memberships);
        // TODO dayOffs, conflicts
        return tournament;
    }

}
