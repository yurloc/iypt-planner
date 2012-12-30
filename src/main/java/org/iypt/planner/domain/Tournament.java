package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private List<Seat> seats;
    private Set<Seat> locked;
    // facts
    private List<Round> rounds;
    private List<Team> teams;
    private List<Group> groups;
    private List<Jury> juries;
    private List<Juror> jurors;
    private List<DayOff> dayOffs;
    private List<Conflict> conflicts;
    private List<Lock> locks;
    private Tournament original = null;

    private int juryCapacity = Jury.DEFAULT_CAPACITY;
    private Statistics stats;
    private Map<Integer, List<DayOff>> dayOffsMap;
    private WeightConfig config = new DefaultWeightConfig();

    public Tournament() {
        // TODO move this out of default constructor
        rounds = new ArrayList<>();
        teams = new ArrayList<>();
        groups = new ArrayList<>();
        juries = new ArrayList<>();
        jurors = new ArrayList<>();
        seats = new ArrayList<>();
        locked = new HashSet<>();
        dayOffs = new ArrayList<>();
        locks = new ArrayList<>();
        dayOffsMap = new HashMap<>();
        conflicts = new ArrayList<>();
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
        facts.addAll(locks);
        facts.add(stats);
        facts.add(config);
        if (original != null) {
            facts.add(original);
        }
        // All planning entities are automatically inserted into the Drools working memory
        // using @PlanningEntityCollectionProperty
        return facts;
    }

    @Override
    public Solution<HardAndSoftScore> cloneSolution() {
        Tournament clone = new Tournament();
        clone.score = score;
        clone.rounds = rounds;
        clone.teams = teams;
        clone.groups = groups;
        clone.juries = juries;
        clone.jurors = jurors;
        clone.dayOffs = dayOffs;
        clone.dayOffsMap = dayOffsMap;
        clone.conflicts = conflicts;
        clone.locks = locks;
        clone.stats = stats;
        clone.config = config;
        clone.original = original;

        // deep-clone the planning entity
        for (Seat seat : seats) {
            Seat seatClone = seat.clone();
            clone.seats.add(seatClone);
            if (isLocked(seat)) {
                clone.locked.add(seatClone);
            }
        }
        return clone;
    }

    /**
     * This is collection of planning entities.
     *
     * @return 
     */
    @PlanningEntityCollectionProperty
    public Collection<Seat> getSeats() {
        return seats;
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

    private void addRounds(Collection<Round> rounds) {
        this.rounds.addAll(rounds);
        for (Round r : rounds) {
            for (Group g : r.getGroups()) {
                groups.add(g);
                teams.addAll(g.getTeams());
                Jury jury = g.getJury();
                jury.setCapacity(juryCapacity);
                juries.add(jury);

                for (int i = 0; i < jury.getCapacity(); i++) {
                    Seat seat = new Seat(jury, i, null);
                    seats.add(seat);
                }
            }
        }
        stats.calculateOptimalLoad();
        calculateIratio();
    }

    public void addRounds(Round... rounds) {
        addRounds(Arrays.asList(rounds));
    }

    public void setRounds(Collection<Round> rounds) {
        this.rounds.clear();
        this.groups.clear();
        this.teams.clear();
        this.juries.clear();
        this.seats.clear();
        addRounds(rounds);
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

    public boolean isLocked(Seat seat) {
        return locked.contains(seat);
    }

    public boolean lock(Seat seat) {
        return locked.add(seat);
    }

    public boolean unlock(Seat seat) {
        return locked.remove(seat);
    }

    public boolean lock(Round round) {
        return locked.addAll(getSeats(round));
    }

    public boolean unlock(Round round) {
        return locked.removeAll(getSeats(round));
    }

    public List<Seat> getSeats(Jury jury) {
        // XXX relying on the fixed order of juries and seats (note: cloned tournament must preserve the order!)
        int start = juries.indexOf(jury) * juryCapacity;
        return seats.subList(start, start + juryCapacity);
    }

    List<Seat> getSeats(Round round) {
        int rSize = round.getGroups().size() * juryCapacity;
        return seats.subList((round.getNumber() - 1) * rSize, round.getNumber() * rSize);
    }

    public void addLock(Lock lock) {
        locks.add(lock);
    }

    public void removeLock(Lock lock) {
        locks.remove(lock);
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

        seats.clear();
        for (Jury jury : juries) {
            jury.setCapacity(capacity);
            for (int i = 0; i < capacity; i++) {
                Seat seat = new Seat(jury, i, null);
                seats.add(seat);
            }
        }
        stats.calculateOptimalLoad();
        calculateIratio();
        return true;
    }

    public void clear() {
        for (Seat seat : seats) {
            seat.setJuror(null);
        }
    }

    // ------------------------------------------------------------------------
    // Getters & Setters
    // ------------------------------------------------------------------------

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<Juror> getJurors() {
        return jurors;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<Jury> getJuries() {
        return juries;
    }

    public List<DayOff> getDayOffs() {
        return dayOffs;
    }

    public void setDayOffs(List<DayOff> dayOffs) {
        dayOffsMap.clear();
        addDayOffs(dayOffs);
    }

    public List<Conflict> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<Conflict> conflicts) {
        this.conflicts = conflicts;
    }

    public List<Lock> getLocks() {
        return locks;
    }

    public int getJuryCapacity() {
        return juryCapacity;
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

    public void setOriginal(Tournament original) {
        this.original = original;
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder(1024);
        for (Round r : this.getRounds()) {
            List<Juror> idle = new ArrayList<>();
            List<Juror> away = new ArrayList<>();
            idle.addAll(this.getJurors());
            sb.append('\n').append(r).append("\n=========\n");
            sb.append(" Group         |  Jury\n");
            //         A: AA BB CC DD | ...
            for (Group g : r.getGroups()) {
                sb.append(g.getName()).append(": ");
                for (Team t : g.getTeams()) {
                    sb.append(t.getCountry()).append(' ');
                }
                if (g.getSize() == 3) sb.append("   ");
                sb.append("| ");
                for (Seat s : this.getSeats()) {
                    if (s.getJury().equals(g.getJury())) {
                        idle.remove(s.getJuror());
                        Juror juror = s.getJuror();
                        if (s.isChair()) sb.append('[');
                        sb.append(juror == null ? "----" : juror);
                        if (s.isChair()) sb.append(']');
                        sb.append(',');
                    }
                }
                sb.replace(sb.length() - 1, sb.length(), "\n");
            }
            for (DayOff dayOff : this.getDayOffs()) {
                if (dayOff.getDay() == r.getDay()) {
                    away.add(dayOff.getJuror());
                }
            }
            idle.removeAll(away); // idle = all -busy -away

            sb.append(String.format("Jurors away (%2d): ", away.size()));
            for (Juror juror : away) {
                sb.append(juror).append(',');
            }
            sb.replace(sb.length() - 1, sb.length(), "\n");

            sb.append(String.format("Jurors idle (%2d): ", idle.size()));
            for (Juror juror : idle) {
                sb.append(juror).append(',');
            }
            sb.replace(sb.length() - 1, sb.length(), "\n");
            sb.append(String.format("Optimal number of independent jurors: %.4f%n", r.getOptimalIndependentCount()));
        }
        int md = this.getJurors().size() * this.getRounds().size() - this.getDayOffs().size();
        sb.append('\n');
        sb.append("Total jury seats:    ").append(this.getSeats().size()).append('\n');
        sb.append("Total juror mandays: ").append(md).append('\n');
        sb.append(String.format("Optimal juror load:  %.4f%n", this.getStatistics().getOptimalLoad()));
        return sb.toString();
    }

    public class Statistics {

        private double optimalLoad = 0.0;

        private void calculateOptimalLoad() {
            if (jurors.size() > 0 && rounds.size() > 0 && dayOffs.size() != jurors.size() * rounds.size()) {
                optimalLoad = ((double) seats.size()) / (jurors.size() * rounds.size() - dayOffs.size());
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
