package org.iypt.planner.domain;

import java.util.Collection;
import java.util.Iterator;
import org.iypt.planner.domain.util.RoundFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.iypt.planner.domain.JurorLoad.INFINITE_LOAD_VALUE;
import static org.iypt.planner.domain.util.SampleFacts.*;

/**
 *
 * @author jlocker
 */
public class TournamentTest {

    // Statistics, WeightConfig
    private static final int EXTRA_FACTS = 2;

    @Test
    public void testRound() {
        // createGroup
        Round r1 = new Round(1, 1);
        Group g1A = r1.createGroup("A");
        assertThat(g1A.getRound()).isSameAs(r1);

        // addGroups
        Group g1B = new Group("B");
        Group g1C = new Group("C");
        r1.addGroups(g1B, g1C);
        assertThat(g1B.getRound()).isSameAs(r1);
        assertThat(g1C.getRound()).isSameAs(r1);
        assertThat(r1.getGroups()).contains(g1A, g1B, g1C);
    }

    @Test
    public void testGroup() {
        Group empty = new Group("A");
        assertThat(empty.getSize()).isZero();

        assertThat(new Group(tA, tB, tC).getSize()).isEqualTo(3);
        assertThat(new Group(tA, tB, tC, tD).getSize()).isEqualTo(4);
    }

    @Test
    public void testTournament() {
        Round r1 = new Round(1, 1);
        Group g1A = r1.createGroup("A").addTeams(tA, tB, tC);
        Group g1B = r1.createGroup("B").addTeams(tD, tE, tF);

        Round r2 = new Round(2, 2);
        Group g2A = r2.createGroup("A").addTeams(tA, tE, tC);
        Group g2B = r2.createGroup("B").addTeams(tD, tB, tF);

        Tournament t = new Tournament();
        int newCapacity = Tournament.DEFAULT_CAPACITY - 1;
        assertThat(t.setJuryCapacity(newCapacity)).isFalse(); // affects no juries

        t.addRounds(r1, r2);
        // getRounds, getGroups, getTeams
        assertThat(t.getRounds()).contains(r1, r2);
        assertThat(t.getGroups()).contains(g1A, g1B, g2A, g2B);
        assertThat(t.getGroups()).hasSize(4);
        assertThat(t.getTeams()).contains(tA, tB, tC, tD, tE, tF);
        // getJuries
        assertThat(t.getJuries()).hasSameSizeAs(t.getGroups());
        // getSeats
        assertThat(t.getSeats()).hasSize(newCapacity * t.getJuries().size());

        // setJuryCapacity
        assertThat(t.setJuryCapacity(Tournament.DEFAULT_CAPACITY)).isTrue();
        newCapacity = Tournament.DEFAULT_CAPACITY + 1;
        assertThat(t.setJuryCapacity(newCapacity)).isTrue();
        assertThat(t.getJuries()).hasSameSizeAs(t.getGroups());
        assertThat(t.getSeats()).hasSize(newCapacity * t.getJuries().size());

        assertThat(t.getProblemFacts()).hasSize(
                t.getRounds().size()
                + t.getGroups().size()
                + t.getTeams().size()
                + t.getJuries().size()
                + EXTRA_FACTS);

        assertThat(t.isFeasibleSolutionPossible()).isFalse();
        assertThat(t.getDayOffsPerRound(r1)).isZero();
        assertThat(t.getDayOffsPerRound(r2)).isZero();

        newCapacity = 2;
        t.setJuryCapacity(newCapacity);
        assertThat(t.getSeats()).hasSize(newCapacity * t.getJuries().size());

        t.addJurors(jA1, jA2, jA3);
        assertThat(t.isFeasibleSolutionPossible()).isFalse();
        t.addJurors(jA4);
        assertThat(t.isFeasibleSolutionPossible()).isTrue();
        t.addJurors(jA5, jA6);
        assertThat(t.isFeasibleSolutionPossible()).isTrue();

        // add some day offs
        t.addDayOffs(new DayOff(jA1, 1));
        t.addDayOffs(new DayOff(jA3, 1));
        t.addDayOffs(new DayOff(jA2, 2));
        t.addDayOffs(new DayOff(jA4, 2));
        assertThat(t.getDayOffsPerRound(r1)).isEqualTo(2);
        assertThat(t.getDayOffsPerRound(r2)).isEqualTo(2);
        assertThat(t.isFeasibleSolutionPossible()).isTrue();

        // one more day off
        t.addDayOffs(new DayOff(jA1, 2));
        assertThat(t.isFeasibleSolutionPossible()).isFalse();
        assertThat(t.getDayOffsPerRound(r2)).isEqualTo(3);

        assertThat(t.getProblemFacts()).hasSize(
                t.getRounds().size()
                + t.getGroups().size()
                + t.getTeams().size()
                + t.getJuries().size()
                + t.getJurors().size()
                + t.getDayOffs().size()
                + t.getConflicts().size() + EXTRA_FACTS);

        t.clearDayOffs();
        assertThat(t.isFeasibleSolutionPossible()).isTrue();

        // add one more round
        Round r3 = new Round(3, 3);
        Group g3A = r3.createGroup("A").addTeams(tA, tB, tF);
        Group g3B = r3.createGroup("B").addTeams(tD, tE, tC);
        t.addRounds(r3);
        assertThat(t.getRounds()).contains(r1, r2, r3);
        assertThat(t.getGroups()).contains(g1A, g1B, g2A, g2B, g3A, g3B);
        assertThat(t.getGroups()).hasSize(6);
        assertThat(t.getTeams()).contains(tA, tB, tC, tD, tE, tF);
        assertThat(t.getJuries()).hasSize(t.getGroups().size());
        assertThat(t.getSeats()).hasSize(newCapacity * t.getJuries().size());
    }

    @Test
    public void testJurorLoad() {
        testLoad(new JurorLoad(jA1, 0, 5, 0, .77), .00, -.77, true);
        testLoad(new JurorLoad(jA1, 0, 5, 1, .77), .00, -.77, true);
        testLoad(new JurorLoad(jA1, 0, 5, 2, .77), .00, -.77, true);
        testLoad(new JurorLoad(jA1, 0, 5, 3, .77), .00, -.77, true);
        testLoad(new JurorLoad(jA1, 0, 5, 4, .77), .00, -.77, true);
        testLoad(new JurorLoad(jA1, 0, 5, 5, .77), .00, -.77, true);

        testLoad(new JurorLoad(jA1, 1, 5, 0, .77), .20, -.57, true);
        testLoad(new JurorLoad(jA1, 1, 5, 1, .77), .25, -.52, true);
        testLoad(new JurorLoad(jA1, 1, 5, 2, .77), .33, -.44, true);
        testLoad(new JurorLoad(jA1, 1, 5, 3, .77), .50, -.27, false);
        testLoad(new JurorLoad(jA1, 1, 5, 4, .77), 1.0, +.23, false);
        testLoad(new JurorLoad(jA1, 1, 5, 5, .77), INFINITE_LOAD_VALUE, INFINITE_LOAD_VALUE - .77, true);

        testLoad(new JurorLoad(jA1, 4, 5, 0, .77), .80, +.03, false);
        testLoad(new JurorLoad(jA1, 4, 5, 1, .77), 1.0, +.23, false);
        testLoad(new JurorLoad(jA1, 4, 5, 2, .77), 1.33, +.56, true);
    }

    @Test
    public void testOptimalLoad() {
        Tournament t = new Tournament();
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(0.0, offset(Double.MIN_VALUE));

        Round r1 = new Round(1, 1);
        r1.createGroup("A").addTeams(tA, tB, tC);
        r1.createGroup("B").addTeams(tD, tE, tF);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(0.0, offset(Double.MIN_VALUE));

        t.setJuryCapacity(2);
        t.addJurors(jA1, jA2, jA3, jA4);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(0.0, offset(Double.MIN_VALUE));
        t.addRounds(r1);
        assertThat(t.getStatistics().getRounds()).isEqualTo(1);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(1.0, offset(Double.MIN_VALUE));

        t.addJurors(jA5, jA6, jB1, jB2, jB3);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(4.0 / 9, offset(Double.MIN_VALUE));

        t.setJuryCapacity(3);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(6.0 / 9, offset(Double.MIN_VALUE));

        Round r2 = new Round(2, 2);
        r2.createGroup("A").addTeams(tA, tB, tC);
        r2.createGroup("B").addTeams(tD, tE, tF);
        t.addRounds(r2);
        assertThat(t.getStatistics().getRounds()).isEqualTo(2);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(2 * 6.0 / 18, offset(Double.MIN_VALUE));

        t.addDayOffs(new DayOff(jA1, 1), new DayOff(jA2, 1));
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(2 * 6.0 / (18 - 2), offset(Double.MIN_VALUE));

        // check that cloneed solution calculates statistics correctly
        Tournament clone = (Tournament) t.cloneSolution();
        clone.setJuryCapacity(2);
        assertThat(clone.getStatistics().getOptimalLoad()).isEqualTo(2 * 4.0 / (18 - 2), offset(Double.MIN_VALUE));
    }

    @Test
    public void testIndependentRatio() {
        Round round = new Round(1, 1);
        Jury jury = new Jury();
        jury.setGroup(round.createGroup("A"));

        round.setOptimalIndependentCount(3.3);
        assertThat(new IndependentRatio(jury, 0).getDelta()).isEqualTo(-3);
        assertThat(new IndependentRatio(jury, 3).getDelta()).isEqualTo(0);
        assertThat(new IndependentRatio(jury, 4).getDelta()).isEqualTo(0);
        assertThat(new IndependentRatio(jury, 5).getDelta()).isEqualTo(1);

        round.setOptimalIndependentCount(3.0);
        assertThat(new IndependentRatio(jury, 2).getDelta()).isEqualTo(-1);
        assertThat(new IndependentRatio(jury, 3).getDelta()).isEqualTo(0);
        assertThat(new IndependentRatio(jury, 4).getDelta()).isEqualTo(1);
    }

    @Test
    public void testOptimalIndependentCount() {
        Round r1 = new Round(1, 1);
        r1.createGroup("A").addTeams(tA, tB, tC);
        Round r2 = new Round(2, 2);
        r2.createGroup("A").addTeams(tA, tB, tC);

        Tournament t = new Tournament();
        t.addRounds(r1, r2);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(0, offset(Double.MIN_VALUE));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(0, offset(Double.MIN_VALUE));

        t.addJurors(jI1, jI2, jT1, jT2);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(Tournament.DEFAULT_CAPACITY * 0.5, offset(Double.MIN_VALUE));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(Tournament.DEFAULT_CAPACITY * 0.5, offset(Double.MIN_VALUE));

        t.setJuryCapacity(2);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(1, offset(Double.MIN_VALUE));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(1, offset(Double.MIN_VALUE));

        t.setJuryCapacity(3);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(1.5, offset(Double.MIN_VALUE));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(1.5, offset(Double.MIN_VALUE));

        t.addDayOffs(new DayOff(jI1, r1.getDay()), new DayOff(jT2, r2.getDay()));
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(1, offset(Double.MIN_VALUE));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(2, offset(Double.MIN_VALUE));
    }

    @Test
    public void testFeasibilitySimple() {
        Round r = new Round(1, 1);
        r.createGroup("A").addTeams(tB, tC, tD);
        r.createGroup("B").addTeams(tE, tF, tG);
        Tournament t = new Tournament();
        t.addRounds(r);
        int capacity = 2;
        t.setJuryCapacity(capacity);
        assertThat(t.getSeats()).hasSize(4);
        assertThat(t.getSeats()).hasSize(r.getGroups().size() * capacity);

        t.addJurors(jA1, jA2, jA3, jA4);
        assertThat(t.isFeasibleSolutionPossible()).isTrue();

        t.addDayOffs(new DayOff(jA1, r.getDay()));
        assertThat(t.getDayOffsPerRound(r)).isEqualTo(1);
        assertThat(t.isFeasibleSolutionPossible()).isFalse();
    }

    @Test
    public void testCloneSolution() {
        Round r1 = new Round(1, 1);
        r1.createGroup("A").addTeams(tA, tB, tC);
        r1.createGroup("B").addTeams(tD, tE, tF);

        Tournament t = new Tournament();
        t.addRounds(r1);
        testClone(t);

        Round r2 = new Round(2, 2);
        r2.createGroup("A").addTeams(tA, tB, tC);
        r2.createGroup("B").addTeams(tD, tE, tF);
        t.addRounds(r2);
        testClone(t);

        t.setJuryCapacity(Tournament.DEFAULT_CAPACITY * 10);
        testClone(t);

        t.addJurors(jA1, jB1, jC1);
        t.addDayOffs(new DayOff(jA1, 1), new DayOff(jB1, 2));
        t.addLock(new Lock(jA1, t.getJuries().get(0), 0));

        t.getConflicts().add(new Conflict(jA1, tF.getCountry()));
        t.getConflicts().add(new Conflict(jB1, tE.getCountry()));
        t.setJuryCapacity(1);
        testClone(t);
    }

    @Test
    public void testLocking() {
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF);
        Round r2 = RoundFactory.createRound(2, tA, tB, tC, tD, tE, tF);
        Tournament t = new Tournament();
        t.addRounds(r1, r2);

        // lock the first seat
        Iterator<Seat> it = t.getSeats().iterator();
        Seat seat = it.next();
        assertThat(t.isLocked(seat)).isFalse();
        assertThat(t.lock(seat)).isTrue();
        assertThat(t.isLocked(seat)).isTrue();
        assertThat(t.isLocked(it.next())).isFalse();

        // lock the second round
        t.lock(r2);
        for (Seat s : t.getSeats()) {
            boolean locked = s == seat || s.getJury().getGroup().getRound() == r2;
            assertThat(t.isLocked(s)).as(String.format("Unexpected lock state for: %s/%d", s, s.getPosition())).isEqualTo(locked);
        }
    }

    private void testClone(Tournament t) {
        Tournament clone = (Tournament) t.cloneSolution();

        // check ordinary getters
        assertThat(clone.getRounds()).isEqualTo(t.getRounds());
        assertThat(clone.getGroups()).isEqualTo(t.getGroups());
        assertThat(clone.getTeams()).isEqualTo(t.getTeams());
        assertThat(clone.getJuries()).isEqualTo(t.getJuries());
        assertThat(clone.getJurors()).isEqualTo(t.getJurors());
        assertThat(clone.getDayOffs()).isEqualTo(t.getDayOffs());
        assertThat(clone.getConflicts()).isEqualTo(t.getConflicts());
        assertThat(clone.getLocks()).isEqualTo(t.getLocks());
        assertThat(clone.getStatistics()).isEqualTo(t.getStatistics());
        assertThat(clone.getWeightConfig()).isEqualTo(t.getWeightConfig());
        assertThat(clone.getJuryCapacity()).isEqualTo(t.getJuryCapacity());

        // check getProblemFacts
        @SuppressWarnings("unchecked")
        Collection<Object> origFacts = (Collection<Object>) t.getProblemFacts();
        @SuppressWarnings("unchecked")
        Collection<Object> cloneFacts = (Collection<Object>) clone.getProblemFacts();
        assertThat(cloneFacts).isEqualTo(origFacts);

        // same number of planning entities
        assertThat(clone.getSeats()).hasSameSizeAs(t.getSeats());
        // no entities in common (verify planning entities are deep cloned)
        assertThat(clone.getSeats()).doesNotContainAnyElementsOf(t.getSeats());
        // verify getSeats(jury) works on cloned solution
        Jury firstJury = t.getJuries().get(0);
        Jury lastJury = t.getJuries().get(t.getJuries().size() - 1);
        assertThat(clone.getSeats()).containsAll(clone.getSeats(firstJury));
        assertThat(clone.getSeats()).containsAll(clone.getSeats(lastJury));
    }

    private void testLoad(JurorLoad load, double expectedLoad, double expectedDelta, boolean excessive) {
        assertThat(load.getLoad()).as(load.toString()).isEqualTo(expectedLoad, offset(.005));
        assertThat(load.getDelta()).as(load.toString()).isEqualTo(expectedDelta, offset(.005));
        assertThat(load.isExcessive()).as(load.toString()).isEqualTo(excessive);
    }
}
