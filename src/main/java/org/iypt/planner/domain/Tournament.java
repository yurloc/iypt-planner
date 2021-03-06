package org.iypt.planner.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.iypt.planner.solver.DefaultWeightConfig;
import org.iypt.planner.solver.WeightConfig;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jlocker
 */
@PlanningSolution
public class Tournament implements Solution<HardSoftScore> {

    private static final Logger log = LoggerFactory.getLogger(Tournament.class);
    private HardSoftScore score;
    // planning entity
    private SortedSet<Seat> seats;
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
    private Map<Juror, List<Conflict>> conflictMap;
    private Tournament original = null;

    private Statistics stats;
    private Map<Round, List<Absence>> absencesPerRoundMap;
    private Map<Juror, List<Absence>> absencesPerJurorMap;
    private WeightConfig config = new DefaultWeightConfig();

    public Tournament() {
        // TODO move this out of default constructor
        rounds = new ArrayList<>();
        teams = new ArrayList<>();
        groups = new ArrayList<>();
        juries = new ArrayList<>();
        jurors = new ArrayList<>();
        seats = new TreeSet<>();
        locked = new HashSet<>();
        lockedRounds = new HashSet<>();
        absences = new ArrayList<>();
        absencesPerRoundMap = new HashMap<>();
        absencesPerJurorMap = new HashMap<>();
        conflicts = new ArrayList<>();
        conflictMap = new HashMap<>();
        stats = new Statistics();
    }

    @Override
    public HardSoftScore getScore() {
        return score;
    }

    @Override
    public void setScore(HardSoftScore score) {
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
        facts.add(stats);
        facts.add(config);
        if (original != null) {
            facts.add(original);
        }
        // All planning entities are automatically inserted into the Drools working memory
        // using @PlanningEntityCollectionProperty
        return facts;
    }

    /**
     * Clones this solution to create a backup that is used to penalize changes when one ore more rounds are locked.
     *
     * @return backup solution
     */
    public Solution<HardSoftScore> makeBackup() {
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
        clone.conflictMap = conflictMap;
        clone.conflicts = conflicts;
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
        List<Seat> seatsInJury = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.getJury().equals(jury)) {
                seatsInJury.add(seat);
            }
        }
        return seatsInJury;
    }

    private List<Seat> getSeats(Round round) {
        List<Seat> seatsInRound = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.getJury().getGroup().getRound().equals(round)) {
                seatsInRound.add(seat);
            }
        }
        return seatsInRound;
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
            double jurorsNeeded = 0;
            for (Seat seat : seats) {
                if (seat.isVoting()) {
                    jurorsNeeded++;
                }
            }
            int jurorsReadyToVote = jurors.size() * rounds.size() - absences.size();
            for (Juror j : jurors) {
                if (!j.isExperienced()) {
                    jurorsReadyToVote--;
                }
            }
            return jurorsNeeded / jurorsReadyToVote;
        }
        return 0.0;
    }

    private double calculateOptimalChairLoad() {
        if (jurors.size() > 0 && rounds.size() > 0 && absences.size() != jurors.size() * rounds.size()) {
            double totalSeats = groups.size();
            int chairCandidates = 0;
            int absentChairs = 0;
            for (Juror juror : jurors) {
                if (juror.isChairCandidate()) {
                    chairCandidates++;
                }
            }
            for (Absence absence : absences) {
                if (absence.getJuror().isChairCandidate()) {
                    absentChairs++;
                }
            }
            return totalSeats / (chairCandidates * rounds.size() - absentChairs);
        }
        return 0.0;
    }

    private void calculateIndependentRatio() {
        for (Round round : rounds) {
            double i = 0;
            int total = 0;
            for (Juror juror : jurors) {
                boolean present = true;
                List<Absence> absenceList = absencesPerRoundMap.get(round);
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
                round.setOptimalIndependentCount(i / total * round.getJurySize());
            }
        }
    }

    private void calculateFirstAvailableRounds() {
        for (Juror juror : jurors) {
            // TODO use sorted set of Rounds (when they are comaparable)
            SortedSet<Integer> away = new TreeSet<>();
            away.add(0);
            for (Absence absence : absencesPerJurorMap.get(juror)) {
                away.add(absence.getRound().getNumber());
            }
            SortedSet<Integer> available = new TreeSet<>();
            for (int i = 1; i <= away.last() + 1; i++) {
                available.add(i);
            }
            available.removeAll(away);
            juror.setFirstAvailable(available.first());
        }
    }

    private void calculateMaxJurySize(boolean setJurySizeToMax) {
        for (Round round : rounds) {
            if (round.getGroups().isEmpty()) {
                throw new IllegalStateException(round + " has no groups");
            }
            assert absencesPerRoundMap.get(round) != null;
            int availableJurors = jurors.size() - absencesPerRoundMap.get(round).size();
            round.setMaxJurySize(availableJurors / round.getGroups().size());
            if (setJurySizeToMax && round.getJurySize() == 0) {
                changeJurySize(round, round.getMaxJurySize());
            }
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

                for (int i = 0; i < r.getJurySize(); i++) {
                    Seat seat = new VotingSeat(jury, i, null);
                    seats.add(seat);
                }
            }

            absencesPerRoundMap.put(r, new ArrayList<Absence>());
        }
        stats.setOptimalLoad(calculateOptimalLoad());
        stats.setOptimalChairLoad(calculateOptimalChairLoad());
        calculateIndependentRatio();
        updateNonVotingBuffers();
    }

    //-------------------------------------------------------------------------
    // Jurors
    //-------------------------------------------------------------------------
    //
    @ValueRangeProvider(id = "jurors")
    public List<Juror> getJurors() {
        return Collections.unmodifiableList(jurors);
    }

    public void setJurors(Collection<Juror> jurors) {
        this.jurors.clear();
        this.absencesPerJurorMap.clear();
        this.conflictMap.clear();
        addJurors(jurors);
    }

    public void addJurors(Juror... jurors) {
        addJurors(Arrays.asList(jurors));
    }

    public void addJurors(Collection<Juror> jurors) {
        for (Juror juror : jurors) {
            this.jurors.add(juror);
            List<Conflict> jurorConflicts = new ArrayList<>(1);
            if (juror.getCountry() != null) {
                Conflict conflict = new Conflict(juror, juror.getCountry());
                conflicts.add(conflict);
                jurorConflicts.add(conflict);
            }
            this.conflictMap.put(juror, jurorConflicts);
            this.absencesPerJurorMap.put(juror, new ArrayList<Absence>());
        }
        stats.setOptimalLoad(calculateOptimalLoad());
        stats.setOptimalChairLoad(calculateOptimalChairLoad());
        calculateIndependentRatio();
        updateNonVotingBuffers();
        calculateMaxJurySize(false);
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
        for (Map.Entry<Round, List<Absence>> entry : absencesPerRoundMap.entrySet()) {
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
            if (!absencesPerRoundMap.containsKey(absence.getRound())) {
                log.warn("Adding absence {}, but {} doesn't exist.", absence, absence.getRound());
                absencesPerRoundMap.put(absence.getRound(), new ArrayList<Absence>());
            }
            absencesPerRoundMap.get(absence.getRound()).add(absence);

            // cache juror's absences
            absencesPerJurorMap.get(absence.getJuror()).add(absence);

            this.absences.add(absence);
        }
        stats.setOptimalLoad(calculateOptimalLoad());
        stats.setOptimalChairLoad(calculateOptimalChairLoad());
        calculateIndependentRatio();
        calculateFirstAvailableRounds();
        updateNonVotingBuffers();
        calculateMaxJurySize(true);
    }

    public void removeAbsences(List<Absence> absences) {
        for (Absence absence : absences) {
            if (!this.absences.contains(absence)) {
                throw new IllegalArgumentException("Cannot remove: " + absence);
            }
            absencesPerRoundMap.get(absence.getRound()).remove(absence);
            absencesPerJurorMap.get(absence.getJuror()).remove(absence);
            this.absences.remove(absence);
        }
        stats.setOptimalLoad(calculateOptimalLoad());
        stats.setOptimalChairLoad(calculateOptimalChairLoad());
        calculateIndependentRatio();
        calculateFirstAvailableRounds();
        updateNonVotingBuffers();
        calculateMaxJurySize(true);
    }

    public int getAbsencesPerRound(Round round) {
        List<Absence> list = absencesPerRoundMap.get(round);
        return list == null ? 0 : list.size();
    }

    //-------------------------------------------------------------------------
    // Locking
    //-------------------------------------------------------------------------
    //
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

    private void updateNonVotingBuffers() {
        int[] nonVoting = new int[rounds.size()];
        Arrays.fill(nonVoting, 0);
        for (Juror juror : jurors) {
            if (!juror.isExperienced() && juror.getFirstAvailable() - 1 < nonVoting.length) {
                nonVoting[juror.getFirstAvailable() - 1]++;
            }
        }

        SortedSet<Seat> newSeats = new TreeSet<>();
        for (Jury jury : juries) {
            Round round = jury.getGroup().getRound();

            BigDecimal totalNonVoting = BigDecimal.valueOf(nonVoting[round.getNumber() - 1]);
            int bufferSize = totalNonVoting.divide(BigDecimal.valueOf(round.getGroups().size()), 0, RoundingMode.CEILING).intValue();

            List<Seat> j = getSeats(jury);
            // copy existing seats
            newSeats.addAll(j.subList(0, Math.min(j.size(), round.getJurySize() + bufferSize)));

            // add empty non-voting seats if the buffer has increased
            for (int i = j.size(); i < round.getJurySize() + bufferSize; i++) {
                NonVotingSeat seat = new NonVotingSeat(jury, i + 100, null);
                newSeats.add(seat);
            }
        }
        seats = newSeats;
    }

    /**
     * Change jury size of the given round. It does the following:
     * <ul>
     * <li>If juries have already been created, number of seats (planning entities returned by {@link #getSeats()}) will be
     * adjusted.</li>
     * <li>If the size increases, empty seats will be added and existing ones will not be touched.</li>
     * <li>If the size decreases, the redundant seats will be removed while, again, the rest will be preserved.</li>
     * </ul>
     *
     * @param round round whose jury size should be changed
     * @param newSize number of jurors in each jury
     * @return <code>true</code> if the number of seats has changed
     */
    public boolean changeJurySize(Round round, int newSize) {
        if (round == null) {
            throw new IllegalArgumentException("Argument 'round' must not be null");
        }
        if (!rounds.contains(round)) {
            throw new IllegalArgumentException(round + " has not yet been added");
        }
        if (newSize < 1) {
            throw new IllegalArgumentException("Size must be positive, got: " + newSize);
        }

        // no change
        if (round.getJurySize() == newSize) {
            return false;
        }

        // nothing to update
        if (juries.isEmpty()) {
            round.setJurySize(newSize);
            return false;
        }

        SortedSet<Seat> newSeats = new TreeSet<>();
        for (Jury jury : juries) {
            if (jury.getGroup().getRound().equals(round)) {
                List<Seat> j = getSeats(jury);
                // copy old seats up to min{old size, new size}
                newSeats.addAll(j.subList(0, Math.min(round.getJurySize(), newSize)));
                // add empty seats (if the size was increased)
                for (int i = round.getJurySize(); i < newSize; i++) {
                    newSeats.add(new VotingSeat(jury, i, null));
                }
                // add non-voting seats (which excess jury size)
                newSeats.addAll(j.subList(round.getJurySize(), j.size()));
            } else {
                newSeats.addAll(getSeats(jury));
            }
        }
        seats = newSeats;
        round.setJurySize(newSize);
        stats.setOptimalLoad(calculateOptimalLoad());
        stats.setOptimalChairLoad(calculateOptimalChairLoad());
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
            int jurorsNeeded = r.getGroups().size() * r.getJurySize();
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

    public List<Conflict> getConflicts(Juror juror) {
        return conflictMap.get(juror);
    }

    public void addConflicts(Conflict... conflicts) {
        addConflicts(Arrays.asList(conflicts));
    }

    public void addConflicts(List<Conflict> conflicts) {
        this.conflicts.addAll(conflicts);
        for (Conflict conflict : conflicts) {
            conflictMap.get(conflict.getJuror()).add(conflict);
        }
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
                if (r.equals(absence.getRound())) {
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
        sb.append(String.format("Optimal chair load:  %.4f%n", stats.getOptimalChairLoad()));
        return sb.toString();
    }

    public static class Statistics {

        private double optimalLoad = 0.0;
        private double optimalChairLoad = 0.0;
        private int rounds = 0;

        public void setOptimalLoad(double optimalLoad) {
            this.optimalLoad = optimalLoad;
        }

        public double getOptimalLoad() {
            return optimalLoad;
        }

        public void setOptimalChairLoad(double optimalChairLoad) {
            this.optimalChairLoad = optimalChairLoad;
        }

        public double getOptimalChairLoad() {
            return optimalChairLoad;
        }

        public void setRounds(int rounds) {
            this.rounds = rounds;
        }

        public int getRounds() {
            return rounds;
        }
    }
}
