package org.iypt.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import org.drools.planner.api.domain.solution.PlanningEntityCollectionProperty;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
import org.drools.planner.core.solution.Solution;

/**
 *
 * @author jlocker
 */
public class Tournament implements Solution<HardAndSoftScore> {

    private HardAndSoftScore score;
    private Collection<Round> rounds;
    private Collection<Team> teams;
    private Collection<Group> groups;
    private Collection<Jury> juries;
    private Collection<Juror> jurors;
    private Collection<JuryMembership> juryMemberships; // planning entity
    private Collection<DayOff> dayOffs;
    private Collection<Conflict> conflicts;

    public Tournament() {
        rounds = new LinkedHashSet<Round>();
        teams = new LinkedHashSet<Team>();
        groups = new LinkedHashSet<Group>();
        juries = new LinkedHashSet<Jury>();
        jurors = new LinkedHashSet<Juror>();
        juryMemberships = new LinkedHashSet<JuryMembership>();
        dayOffs = new LinkedHashSet<DayOff>();
        conflicts = new LinkedHashSet<Conflict>();
    }

    public HardAndSoftScore getScore() {
        return score;
    }

    public void setScore(HardAndSoftScore score) {
        this.score = score;
    }

    public Collection<? extends Object> getProblemFacts() {
        ArrayList<Object> facts = new ArrayList<Object>();
        facts.addAll(rounds);
        facts.addAll(teams);
        facts.addAll(groups);
        facts.addAll(juries);
        facts.addAll(jurors);
        facts.addAll(dayOffs);
        facts.addAll(conflicts);
        // All planning entities are automatically inserted into the Drools working memory
        // using @PlanningEntityCollectionProperty
        return facts;
    }

    public Solution<HardAndSoftScore> cloneSolution() {
        Tournament clone = new Tournament();
        clone.score = score;
        clone.setRounds(rounds);
        clone.jurors = jurors;
        clone.dayOffs = dayOffs;
        clone.conflicts = conflicts;
        
        // deep-clone the planning entity
        for (JuryMembership membership : juryMemberships) {
            clone.juryMemberships.add(membership.clone());
        }
        return clone;
    }

    /**
     * Get the value of juryMemberships
     *
     * @return the value of juryMemberships
     */
    @PlanningEntityCollectionProperty
    public Collection<JuryMembership> getJuryMemberships() {
        return juryMemberships;
    }

    /**
     * Set the value of juryMemberships
     *
     * @param juryMemberships new value of juryMemberships
     */
    public void setJuryMemberships(Collection<JuryMembership> juryMemberships) {
        this.juryMemberships = juryMemberships;
    }

    public Collection<Round> getRounds() {
        return rounds;
    }

    public void setRounds(Collection<Round> rounds) {
        this.rounds = rounds;
        for (Round r : rounds) {
            for (Group g : r.getGroups()) {
                groups.add(g);
                juries.add(g.getJury());
                teams.addAll(g.getTeams());
            }
        }
    }

    public Collection<Team> getTeams() {
        return teams;
    }

    public void setTeams(Collection<Team> teams) {
        this.teams = teams;
    }

    public Collection<Juror> getJurors() {
        return jurors;
    }

    public void setJurors(Collection<Juror> jurors) {
        this.jurors = jurors;
    }

    public Collection<Group> getGroups() {
        return groups;
    }

    public void setGroups(Collection<Group> groups) {
        this.groups = groups;
    }

    public Collection<Jury> getJuries() {
        return juries;
    }

    public void setJuries(Collection<Jury> juries) {
        this.juries = juries;
    }

    public Collection<DayOff> getDayOffs() {
        return dayOffs;
    }

    public void setDayOffs(Collection<DayOff> dayOffs) {
        this.dayOffs = dayOffs;
    }

    public Collection<Conflict> getConflicts() {
        return conflicts;
    }

    public void setConflicts(Collection<Conflict> conflicts) {
        this.conflicts = conflicts;
    }

    public void addRounds(Round... rounds) {
        setRounds(Arrays.asList(rounds));
    }

    public void addJurors(Juror... jurors) {
        Collections.addAll(this.jurors, jurors);
    }

    public void addDayOffs(DayOff... dayOffs) {
        Collections.addAll(this.dayOffs, dayOffs);
    }

    // FIXME
    @Deprecated
    public void createJuries(Round round, int juryCapacity) {
        for (Group g : round.getGroups()) {
            Jury jury = new Jury();
            jury.setCapacity(juryCapacity);
            jury.setGroup(g);
            g.setJury(jury);
            for (int i = 0; i < juryCapacity; i++) {
                // no need to initialize the planning variable (Jury), will be done by construction heuristic
                this.juryMemberships.add(new JuryMembership(jury, null));
            }
        }
    }
}
