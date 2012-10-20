package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.drools.planner.api.domain.solution.PlanningEntityCollectionProperty;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
import org.drools.planner.core.solution.Solution;
import org.iypt.planner.solver.DefaultWeightConfig;
import org.iypt.planner.solver.WeightConfig;

/**
 *
 * @author jlocker
 */
public class Tournament implements Solution<HardAndSoftScore> {

    private HardAndSoftScore score;
    // planning entity
    private Collection<JurySeat> jurySeats;
    // facts
    private Collection<Round> rounds;
    private Collection<Team> teams;
    private Collection<Group> groups;
    private Collection<Jury> juries;
    private Collection<Juror> jurors;
    private Collection<DayOff> dayOffs;
    private Collection<Conflict> conflicts;

    private int juryCapacity = Jury.DEFAULT_CAPACITY;
    private Statistics stats;
    private Map<Integer, List<DayOff>> dayOffsMap;
    // TODO remove this when bias data is available
    private Random random = new Random(0);
    private WeightConfig config = new DefaultWeightConfig();

    public Tournament() {
        // TODO move this out of default constructor
        rounds = new LinkedHashSet<>();
        teams = new LinkedHashSet<>();
        groups = new LinkedHashSet<>();
        juries = new LinkedHashSet<>();
        jurors = new LinkedHashSet<>();
        jurySeats = new LinkedHashSet<>();
        dayOffs = new LinkedHashSet<>();
        dayOffsMap = new HashMap<>();
        conflicts = new LinkedHashSet<>();
        stats = new Statistics();
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
        facts.add(stats);
        facts.add(config);
        // All planning entities are automatically inserted into the Drools working memory
        // using @PlanningEntityCollectionProperty
        return facts;
    }

    @Override
    public Solution<HardAndSoftScore> cloneSolution() {
        Tournament clone = new Tournament();
        clone.score = score;
        clone.addRounds(rounds, true);
        clone.jurors = jurors;
        clone.dayOffs = dayOffs;
        clone.dayOffsMap = dayOffsMap;
        clone.conflicts = conflicts;
        clone.stats = stats;
        clone.config = config;

        // deep-clone the planning entity
        for (JurySeat seat : jurySeats) {
            clone.jurySeats.add(seat.clone());
        }
        return clone;
    }

    /**
     * This is collection of planning entities.
     *
     * @return 
     */
    @PlanningEntityCollectionProperty
    public Collection<JurySeat> getJurySeats() {
        return jurySeats;
    }

    private void calculateIratio() {
        for (Round round : rounds) {
            double i = 0;
            int total = 0;
            for (Juror juror : jurors) {
                boolean present = true;
                List<DayOff> dayOffList = dayOffsMap.get(round.getDay());
                if (dayOffList != null) {
                    for (DayOff dayOff : dayOffList) {
                        if (dayOff.getJuror().equals(juror)) {
                            present = false;
                            break;
                        }
                    }
                }
                if (!present) {
                    // absent juror has no influence
                    continue;
                }
                if (juror.getType() == JurorType.INDEPENDENT) {
                    i++;
                }
                total++;
            }
            if (total > 0) {
                round.setOptimalIndependentCount(i / total * juryCapacity);
            }
        }
    }

    private void addRounds(Collection<Round> rounds, boolean cloningSolution) {
        this.rounds.addAll(rounds);
        for (Round r : rounds) {
            for (Group g : r.getGroups()) {
                groups.add(g);
                teams.addAll(g.getTeams());
                Jury jury = g.getJury();
                jury.setCapacity(juryCapacity);
                juries.add(jury);

                // skip this when cloning, planning entities have to be deep-cloned
                if (!cloningSolution) {
                    boolean chair = true; // first seat in each jury is the chair seat
                    for (int i = 0; i < jury.getCapacity(); i++) {
                        JurySeat seat = new JurySeat(chair, jury, null);
                        jurySeats.add(seat);
                        jury.getSeats().add(seat);
                        chair = false;
                    }
                }
            }
        }
        stats.calculateOptimalLoad();
        calculateIratio();
    }

    public void addRounds(Round... rounds) {
        addRounds(Arrays.asList(rounds), false);
    }

    public void setRounds(Collection<Round> rounds) {
        this.rounds.clear();
        this.groups.clear();
        this.teams.clear();
        this.juries.clear();
        this.jurySeats.clear();
        addRounds(rounds, false);
    }

    public void setJurors(Collection<Juror> jurors) {
        this.jurors.clear();
        addJurors(jurors);
    }

    public void addJurors(Juror... jurors) {
        addJurors(Arrays.asList(jurors));
    }

    public void addJurors(Collection<Juror> jurors) {
        for (Juror juror : jurors) {
            // TODO remove this when bias data is available
            if (juror.getBias() == 0) {
                juror.setBias(random.nextDouble() * 2 - 1);
            }
            this.jurors.add(juror);
            this.conflicts.add(new Conflict(juror, juror.getCountry()));
        }
        stats.calculateOptimalLoad();
        calculateIratio();
    }

    public void addDayOffs(DayOff... dayOffs) {
        addDayOffs(Arrays.asList(dayOffs));
    }

    public void addDayOffs(List<DayOff> dayOffs) {
        for (DayOff dayOff : dayOffs) {
            List<DayOff> dayOffList = dayOffsMap.get(dayOff.getDay());
            if (dayOffList == null) {
                dayOffList = new ArrayList<>();
                dayOffsMap.put(dayOff.getDay(), dayOffList);
            }
            dayOffList.add(dayOff);
            this.dayOffs.add(dayOff);
        }
        stats.calculateOptimalLoad();
        calculateIratio();
    }

    public int getDayOffsPerRound(Round r) {
        List<DayOff> list = dayOffsMap.get(r.getDay());
        return list == null ? 0 : list.size();
    }

    public void clearDayOffs() {
        dayOffs.clear();
        dayOffsMap.clear();
        calculateIratio();
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
            int jurorsNeeded = r.getGroups().size() * juryCapacity;
            int jurorsAvailable = jurors.size() - getDayOffsPerRound(r);
            if (jurorsNeeded > jurorsAvailable) return false;
        }
        return true;
    }

    /**
     * Sets or changes jury capacities for this tournament. All juries contained in this tournament and all that will be added
     * in future will have this capacity.
     * <p><em>NOTE: effective change of jury capacities will clear the current seat occupations.</em></p>
     * @param capacity number of jurors in each jury
     * @return <code>true</code> if jury capacity change has affected any juries
     */
    public boolean setJuryCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be positive, got: " + capacity);
        }
        this.juryCapacity = capacity; // remember the new capacity
        if (juries.isEmpty()) return false;

        if (juries.iterator().next().getCapacity() == capacity) return false;

        jurySeats.clear();
        for (Jury jury : juries) {
            jury.setCapacity(capacity);
            boolean chair = true; // first seat in each jury is the chair seat
            for (int i = 0; i < capacity; i++) {
                JurySeat seat = new JurySeat(chair, jury, null);
                jurySeats.add(seat);
                jury.getSeats().add(seat); // XXX relying on implementation of jurySeat collection
                chair = false;
            }
        }
        stats.calculateOptimalLoad();
        calculateIratio();
        return true;
    }

    // ------------------------------------------------------------------------
    // Getters & Setters
    // ------------------------------------------------------------------------

    public void setJurySeats(Collection<JurySeat> jurySeat) {
        this.jurySeats = jurySeat;
    }

    public Collection<Round> getRounds() {
        return rounds;
    }

    public Collection<Team> getTeams() {
        return teams;
    }

    public Collection<Juror> getJurors() {
        return jurors;
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

    public void setDayOffs(List<DayOff> dayOffs) {
        dayOffsMap.clear();
        addDayOffs(dayOffs);
    }

    public Collection<Conflict> getConflicts() {
        return conflicts;
    }

    public void setConflicts(Collection<Conflict> conflicts) {
        this.conflicts = conflicts;
    }

    public Statistics getStatistics() {
        return stats;
    }

    public WeightConfig getWeightConfig() {
        return config;
    }

    public void setWeightConfig(WeightConfig config) {
        this.config = config;
    }

    public class Statistics {

        private double optimalLoad = 0.0;

        private void calculateOptimalLoad() {
            if (jurors.size() > 0 && rounds.size() > 0 && dayOffs.size() != jurors.size() * rounds.size()) {
                optimalLoad = ((double) jurySeats.size()) / (jurors.size() * rounds.size() - dayOffs.size());
            }
        }

        public double getOptimalLoad() {
            return optimalLoad;
        }

        public int getRounds() {
            return rounds.size();
        }
    }
}
