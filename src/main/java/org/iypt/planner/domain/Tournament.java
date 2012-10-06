package org.iypt.planner.domain;

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

    public Tournament() {
        rounds = new LinkedHashSet<>();
        teams = new LinkedHashSet<>();
        groups = new LinkedHashSet<>();
        juries = new LinkedHashSet<>();
        jurors = new LinkedHashSet<>();
        juryMemberships = new LinkedHashSet<>();
        dayOffs = new LinkedHashSet<>();
        conflicts = new LinkedHashSet<>();
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
        clone.setRounds(rounds, true);
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
        setRounds(rounds, false);
    }

    private void setRounds(Collection<Round> rounds, boolean skipPlanningEntity) {
        this.rounds = rounds;
        this.groups.clear();
        this.juries.clear();
        this.teams.clear();
        this.juryMemberships.clear();
        for (Round r : rounds) {
            for (Group g : r.getGroups()) {
                groups.add(g);
                teams.addAll(g.getTeams());
                Jury jury = g.getJury();
                juries.add(jury);

                if (!skipPlanningEntity) {
                    for (int i = 0; i < jury.getCapacity(); i++) {
                        juryMemberships.add(new JuryMembership(jury, null));
                    }
                }
            }
        }
    }

    public Collection<Team> getTeams() {
        return teams;
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

    public Collection<Jury> getJuries() {
        return juries;
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
    
    public int getDayOffsPerRound(Round r) {
        int count = 0;
        for (DayOff dayOff : dayOffs) {
            if (dayOff.getDay() == r.getDay()) count++;
        }
        return count;
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
            int jurorsAvailable = jurors.size() - getDayOffsPerRound(r);
            if (jurorsNeeded > jurorsAvailable) return false;
        }
        return true;
    }

    /**
     * Sets or changes jury capacity for this tournament.
     * @param capacity number of jurors in each jury
     * @return <code>true</code> if jury capacity has changed
     */
    public boolean changeJuryCapacity(int capacity) {
        if (juries.isEmpty()) return false;

        if (juries.iterator().next().getCapacity() == capacity) return false;

        juryMemberships.clear();
        for (Jury jury : juries) {
            jury.setCapacity(capacity);
            for (int i = 0; i < capacity; i++) {
                juryMemberships.add(new JuryMembership(jury, null));
            }
        }
        return true;
    }
}
