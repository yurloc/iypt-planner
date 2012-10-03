package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.drools.planner.api.domain.solution.PlanningEntityCollectionProperty;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
import org.drools.planner.core.solution.Solution;

/**
 *
 * @author jlocker
 */
public class Tournament implements Solution<HardAndSoftScore> {

    private HardAndSoftScore score;
    // planning entity
    private Collection<JuryMembership> juryMemberships;
    // facts
    private Collection<Round> rounds;
    private Collection<Team> teams;
    private Collection<Group> groups;
    private Collection<Jury> juries;
    private Collection<Juror> jurors;
    private Collection<DayOff> dayOffs;
    private Collection<Conflict> conflicts;
    // utils
    private Map<Round, Integer> mapDayOffsPerRound;

    public Tournament() {
        rounds = new LinkedHashSet<>();
        teams = new LinkedHashSet<>();
        groups = new LinkedHashSet<>();
        juries = new LinkedHashSet<>();
        jurors = new LinkedHashSet<>();
        juryMemberships = new LinkedHashSet<>();
        dayOffs = new LinkedHashSet<>();
        conflicts = new LinkedHashSet<>();
        mapDayOffsPerRound = new HashMap<>();
    }

    @Override
    public HardAndSoftScore getScore() {
        return score;
    }

    @Override
    public void setScore(HardAndSoftScore score) {
        this.score = score;
    }

    @Override
    public Collection<? extends Object> getProblemFacts() {
        ArrayList<Object> facts = new ArrayList<>();
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

    @Override
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
        for (DayOff dayOff : dayOffs) {
            this.dayOffs.add(dayOff);
            for (Round r : rounds) {
                if (r.getDay() == dayOff.getDay()) {
                    Integer dayOffsThisRound = mapDayOffsPerRound.get(r);
                    mapDayOffsPerRound.put(r, dayOffsThisRound == null ? 1 : ++dayOffsThisRound);
                    break;
                }
            }
        }
    }
    
    public int dayOffsPerRound(Round r) {
        if (!mapDayOffsPerRound.containsKey(r)) return 0;
        return mapDayOffsPerRound.get(r);
    }

    /**
     * Performs a sanity-check on this tournament. Checks the included problem facts and indicates if some hard constraints
     * obviously cannot be satisfied.
     * @return <code>false</code> if a feasible solution certainly does not exist,
     * <code>true</code> if a feasible solution <em>may</em> be found
     */
    public boolean isFeasibleSolutionPossible() {
        for (Round r : rounds) {
            // TODO (?) don't assume all juries have the same capacity
            int jurorsNeeded = r.getGroups().size() * r.getGroups().iterator().next().getJury().getCapacity();
            int jurorsAvailable = jurors.size() - dayOffsPerRound(r);
            if (jurorsNeeded > jurorsAvailable) return false;
        }
        return true;
    }

}
