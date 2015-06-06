package org.iypt.planner.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.drools.planner.api.domain.solution.PlanningEntityCollectionProperty;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
import org.drools.planner.core.solution.Solution;
import org.iypt.planner.solver.DefaultWeightConfig;
import org.iypt.planner.solver.WeightConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlocker
 */
public class Tournament implements Solution<HardAndSoftScore> {

    public static final int DEFAULT_CAPACITY = 5;
    private static final Logger log = LoggerFactory.getLogger(Tournament.class);
    private HardAndSoftScore score;
    // planning entity
    private List<Seat> seats;
    private Set<Seat> locked;
    // facts
    private List<Round> rounds;
    private Set<Round> lockedRounds;
    private List<Team> teams;
    private List<Group> groups;
    private List<Jury> juries;
    private List<Juror> jurors;
    private List<Absence> absences;
    private List<Conflict> conflicts;
    private List<Lock> locks;
    private Tournament original = null;

    private int juryCapacity = DEFAULT_CAPACITY;
    private Statistics stats;
    private Map<Integer, List<Absence>> absencesPerRoundMap;
    private Map<Juror, List<Absence>> absencesPerJurorMap;
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
        lockedRounds = new HashSet<>();
        absences = new ArrayList<>();
        locks = new ArrayList<>();
        absencesPerRoundMap = new HashMap<>();
        absencesPerJurorMap = new HashMap<>();
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
        facts.addAll(absences);
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
        clone.lockedRounds = lockedRounds;
        clone.teams = teams;
        clone.groups = groups;
        clone.juries = juries;
        clone.jurors = jurors;
        clone.absences = absences;
        clone.absencesPerRoundMap = absencesPerRoundMap;
        clone.absencesPerJurorMap = absencesPerJurorMap;
        clone.conflicts = conflicts;
        clone.locks = locks;
        clone.stats = stats;
        clone.config = config;
        clone.original = original;
        clone.juryCapacity = juryCapacity;

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

    //-------------------------------------------------------------------------
    // Seats
    //-------------------------------------------------------------------------
    //
    /**
     * This is the collection of planning entities.
     *
     * @return all seats in all juries
     */
    @PlanningEntityCollectionProperty
    public Collection<Seat> getSeats() {
        return seats;
    }

    public List<Seat> getSeats(Jury jury) {
        // XXX relying on the fixed order of juries and seats (note: cloned tournament must preserve the order!)
        int start = juries.indexOf(jury) * juryCapacity;
        return seats.subList(start, start + juryCapacity);
    }

    private List<Seat> getSeats(Round round) {
        int rSize = round.getGroups().size() * juryCapacity;
        return seats.subList((round.getNumber() - 1) * rSize, round.getNumber() * rSize);
    }

    public void setSeats(List<Seat> seats) {
        this.seats = new ArrayList<>(seats);
    }

    public void clear() {
        for (Seat seat : seats) {
            seat.setJuror(null);
        }
    }

    //-------------------------------------------------------------------------
    // Inferred statistics
    //-------------------------------------------------------------------------
    //
    private double calculateOptimalLoad() {
        if (jurors.size() > 0 && rounds.size() > 0 && absences.size() != jurors.size() * rounds.size()) {
            return ((double) seats.size()) / (jurors.size() * rounds.size() - absences.size());
        }
        return 0.0;
    }

    private void calculateIndependentRatio() {
        for (Round round : rounds) {
            double i = 0;
            int total = 0;
            for (Juror juror : jurors) {
                boolean present = true;
                List<Absence> absenceList = absencesPerRoundMap.get(round.getNumber());
                if (absenceList != null) {
                    for (Absence absence : absenceList) {
                        if (absence.getJuror().equals(juror)) {
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

    private void calculateFirstAvailableRounds() {
        for (Juror juror : jurors) {
            SortedSet<Integer> away = new TreeSet<>();
            away.add(0);
            for (Absence absence : absencesPerJurorMap.get(juror)) {
                away.add(absence.getRoundNumber());
            }
            SortedSet<Integer> available = new TreeSet<>();
            for (int i = 1; i <= away.last() + 1; i++) {
                available.add(i);
            }
            available.removeAll(away);
            juror.setFirstAvailable(available.first());
        }
    }

    //-------------------------------------------------------------------------
    // Rounds
    //-------------------------------------------------------------------------
    //
    public List<Round> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    public void setRounds(Collection<Round> rounds) {
        this.rounds.clear();
        this.groups.clear();
        this.teams.clear();
        this.juries.clear();
        this.seats.clear();
        this.absencesPerRoundMap.clear();
        addRounds(rounds);
    }

    public void addRounds(Round... rounds) {
        addRounds(Arrays.asList(rounds));
    }

    private void addRounds(Collection<Round> rounds) {
        this.rounds.addAll(rounds);
        stats.setRounds(this.rounds.size());
        for (Round r : rounds) {
            for (Group g : r.getGroups()) {
                groups.add(g);
                teams.addAll(g.getTeams());
                Jury jury = g.getJury();
                juries.add(jury);

                for (int i = 0; i < juryCapacity; i++) {
                    Seat seat = new Seat(jury, i, null);
                    seats.add(seat);
                }
            }

            absencesPerRoundMap.put(r.getNumber(), new ArrayList<Absence>());
        }
        stats.setOptimalLoad(calculateOptimalLoad());
        calculateIndependentRatio();
    }

    //-------------------------------------------------------------------------
    // Jurors
    //-------------------------------------------------------------------------
    //
    public List<Juror> getJurors() {
        return Collections.unmodifiableList(jurors);
    }

    public void setJurors(Collection<Juror> jurors) {
        this.jurors.clear();
        this.absencesPerJurorMap.clear();
        addJurors(jurors);
    }

    public void addJurors(Juror... jurors) {
        addJurors(Arrays.asList(jurors));
    }

    public void addJurors(Collection<Juror> jurors) {
        for (Juror juror : jurors) {
            this.jurors.add(juror);
            this.conflicts.add(new Conflict(juror, juror.getCountry()));
            this.absencesPerJurorMap.put(juror, new ArrayList<Absence>());
        }
        stats.setOptimalLoad(calculateOptimalLoad());
        calculateIndependentRatio();
    }

    //-------------------------------------------------------------------------
    // Absences
    //-------------------------------------------------------------------------
    //
    public List<Absence> getAbsences() {
        return Collections.unmodifiableList(absences);
    }

    public List<Absence> getAbsences(Juror juror) {
        return Collections.unmodifiableList(absencesPerJurorMap.get(juror));
    }

    public void setAbsences(List<Absence> absences) {
        for (Map.Entry<Integer, List<Absence>> entry : absencesPerRoundMap.entrySet()) {
            entry.getValue().clear();
        }
        for (Map.Entry<Juror, List<Absence>> entry : absencesPerJurorMap.entrySet()) {
            entry.getValue().clear();
        }
        addAbsences(absences);
    }

    public void addAbsences(Absence... absences) {
        addAbsences(Arrays.asList(absences));
    }

    public void addAbsences(List<Absence> absences) {
        for (Absence absence : absences) {
            // cache round's absences
            if (!absencesPerRoundMap.containsKey(absence.getRoundNumber())) {
                log.warn("Adding absence {}, but round #{} doesn't exist.", absence, absence.getRoundNumber());
                absencesPerRoundMap.put(absence.getRoundNumber(), new ArrayList<Absence>());
            }
            absencesPerRoundMap.get(absence.getRoundNumber()).add(absence);

            // cache juror's absences
            absencesPerJurorMap.get(absence.getJuror()).add(absence);

            this.absences.add(absence);
        }
        stats.setOptimalLoad(calculateOptimalLoad());
        calculateIndependentRatio();
        calculateFirstAvailableRounds();
    }

    public void removeAbsences(List<Absence> absences) {
        for (Absence absence : absences) {
            if (!this.absences.contains(absence)) {
                throw new IllegalArgumentException("Cannot remove: " + absence);
            }
            absencesPerRoundMap.get(absence.getRoundNumber()).remove(absence);
            absencesPerJurorMap.get(absence.getJuror()).remove(absence);
            this.absences.remove(absence);
        }
        stats.setOptimalLoad(calculateOptimalLoad());
        calculateIndependentRatio();
        calculateFirstAvailableRounds();
    }

    public int getAbsencesPerRound(Round r) {
        List<Absence> list = absencesPerRoundMap.get(r.getNumber());
        return list == null ? 0 : list.size();
    }

    //-------------------------------------------------------------------------
    // Locking
    //-------------------------------------------------------------------------
    //
    public List<Lock> getLocks() {
        return Collections.unmodifiableList(locks);
    }

    public void addLock(Lock lock) {
        locks.add(lock);
    }

    public void removeLock(Lock lock) {
        locks.remove(lock);
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

    public boolean isLocked(Round round) {
        return lockedRounds.contains(round);
    }

    public boolean lock(Round round) {
        lockedRounds.add(round);
        return locked.addAll(getSeats(round));
    }

    public boolean unlock(Round round) {
        lockedRounds.remove(round);
        return locked.removeAll(getSeats(round));
    }

    //-------------------------------------------------------------------------
    // Capacity
    //-------------------------------------------------------------------------
    //
    public int getJuryCapacity() {
        return juryCapacity;
    }

    /**
     * Changes jury capacity for this tournament. It can be set anytime and does the following:
     * <ul>
     * <li>If the tournament has no rounds (and juries) yet, the capacity will be effective when they are added.</li>
     * <li>If juries have already been created, number of seats (planning entities returned by {@link #getSeats()}) will be
     * adjusted.</li>
     * <li>If the capacity increases, empty seats will be added and existing ones will not be touched.</li>
     * <li>If the capacity decreases, the redundant seats will be removed while, again, the rest will be preserved.</li>
     * </ul>
     *
     * @param newCapacity number of jurors in each jury
     * @return <code>true</code> if the number of seats has changed
     */
    public boolean setJuryCapacity(int newCapacity) {
        if (newCapacity < 1) {
            throw new IllegalArgumentException("Capacity must be positive, got: " + newCapacity);
        }

        // no change
        if (juryCapacity == newCapacity) {
            return false;
        }

        // nothing to update
        if (juries.isEmpty()) {
            juryCapacity = newCapacity;
            return false;
        }

        ArrayList<Seat> newSeats = new ArrayList<>(newCapacity * juries.size());
        for (Jury jury : juries) {
            // copy old seats up to min{old capacity, new capacity}
            int fromIndex = juries.indexOf(jury) * juryCapacity;
            int toIndex = fromIndex + Math.min(juryCapacity, newCapacity);
            newSeats.addAll(seats.subList(fromIndex, toIndex));
            // add empty seats (if the capacity was increased)
            for (int i = juryCapacity; i < newCapacity; i++) {
                newSeats.add(new Seat(jury, i, null));
            }
        }
        seats = newSeats;
        juryCapacity = newCapacity;
        stats.setOptimalLoad(calculateOptimalLoad());
        calculateIndependentRatio();
        return true;
    }

    /**
     * Performs a sanity-check on this tournament. Checks the included problem facts and indicates if some hard constraints
     * obviously cannot be satisfied.
     *
     * @return <code>false</code> if a feasible solution certainly does not exist, <code>true</code> if a feasible solution
     * <em>may</em> be found
     */
    public boolean isFeasibleSolutionPossible() {
        for (Round r : rounds) {
            int jurorsNeeded = r.getGroups().size() * juryCapacity;
            int jurorsAvailable = jurors.size() - getAbsencesPerRound(r);
            if (jurorsNeeded > jurorsAvailable) {
                return false;
            }
        }
        return true;
    }

    //-------------------------------------------------------------------------
    // Other accessors
    //-------------------------------------------------------------------------
    //
    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public List<Jury> getJuries() {
        return Collections.unmodifiableList(juries);
    }

    public List<Conflict> getConflicts() {
        return Collections.unmodifiableList(conflicts);
    }

    public void addConflicts(Conflict... conflicts) {
        addConflicts(Arrays.asList(conflicts));
    }

    public void addConflicts(List<Conflict> conflicts) {
        this.conflicts.addAll(conflicts);
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
                if (g.getSize() == 3) {
                    sb.append("   ");
                }
                sb.append("| ");
                for (Seat s : this.getSeats()) {
                    if (s.getJury().equals(g.getJury())) {
                        idle.remove(s.getJuror());
                        Juror juror = s.getJuror();
                        if (s.isChair()) {
                            sb.append('[');
                        }
                        sb.append(juror == null ? "----" : juror);
                        if (s.isChair()) {
                            sb.append(']');
                        }
                        sb.append(',');
                    }
                }
                sb.replace(sb.length() - 1, sb.length(), "\n");
            }
            for (Absence absence : this.getAbsences()) {
                if (absence.getRoundNumber() == r.getNumber()) {
                    away.add(absence.getJuror());
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
        int md = this.getJurors().size() * this.getRounds().size() - this.getAbsences().size();
        sb.append('\n');
        sb.append("Total jury seats:    ").append(this.getSeats().size()).append('\n');
        sb.append("Total juror mandays: ").append(md).append('\n');
        sb.append(String.format("Optimal juror load:  %.4f%n", stats.getOptimalLoad()));
        return sb.toString();
    }

    public static class Statistics {

        private double optimalLoad = 0.0;
        private int rounds = 0;

        public void setOptimalLoad(double optimalLoad) {
            this.optimalLoad = optimalLoad;
        }

        public double getOptimalLoad() {
            return optimalLoad;
        }

        public void setRounds(int rounds) {
            this.rounds = rounds;
        }

        public int getRounds() {
            return rounds;
        }
    }
}
