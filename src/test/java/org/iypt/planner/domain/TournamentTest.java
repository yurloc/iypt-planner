package org.iypt.planner.domain;

import com.neovisionaries.i18n.CountryCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.iypt.planner.domain.JurorLoad.INFINITE_LOAD_VALUE;
import static org.iypt.planner.domain.SampleFacts.*;

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
        Round r1 = new Round(1);
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
        int origSize = 10;
        Round r1 = new Round(1, origSize);
        Group g1A = r1.createGroup("A").addTeams(tA, tB, tC);
        Group g1B = r1.createGroup("B").addTeams(tD, tE, tF);

        Round r2 = new Round(2, origSize);
        Group g2A = r2.createGroup("A").addTeams(tA, tE, tC);
        Group g2B = r2.createGroup("B").addTeams(tD, tB, tF);

        Tournament t = new Tournament();
        t.addRounds(r1, r2);
        // getRounds, getGroups, getTeams
        assertThat(t.getRounds()).contains(r1, r2);
        assertThat(t.getGroups()).contains(g1A, g1B, g2A, g2B);
        assertThat(t.getGroups()).hasSize(4);
        assertThat(t.getTeams()).contains(tA, tB, tC, tD, tE, tF);
        // getJuries
        assertThat(t.getJuries()).hasSameSizeAs(t.getGroups());
        // getSeats
        assertThat(t.getSeats()).hasSize(origSize * t.getJuries().size());

        // setJuryCapacity
        int r1size = origSize - 1;
        int r2size = origSize + 10;
        assertThat(t.changeJurySize(r1, r1size)).isTrue();
        assertThat(t.changeJurySize(r2, r2size)).isTrue();
        assertThat(t.getJuries()).hasSameSizeAs(t.getGroups());
        assertThat(t.getSeats()).hasSize((r1size + r2size) * r1.getGroups().size());

        assertThat(t.getProblemFacts()).hasSize(
                t.getRounds().size()
                + t.getGroups().size()
                + t.getTeams().size()
                + t.getJuries().size()
                + EXTRA_FACTS);

        assertThat(t.isFeasibleSolutionPossible()).isFalse();
        assertThat(t.getAbsencesPerRound(r1)).isZero();
        assertThat(t.getAbsencesPerRound(r2)).isZero();

        int newSize = 2;
        t.changeJurySize(r1, newSize);
        t.changeJurySize(r2, newSize);
        assertThat(t.getSeats()).hasSize(newSize * t.getJuries().size());

        t.addJurors(jA1, jA2, jA3);
        assertThat(t.isFeasibleSolutionPossible()).isFalse();
        t.addJurors(jA4);
        assertThat(t.isFeasibleSolutionPossible()).isTrue();
        t.addJurors(jA5, jA6);
        assertThat(t.isFeasibleSolutionPossible()).isTrue();

        // conflicts
        assertThat(t.getConflicts()).hasSize(6);
        t.addConflicts(new Conflict(jA1, tB.getCountry()));
        assertThat(t.getConflicts()).hasSize(7);
        t.addConflicts(new Conflict(jA2, tA.getCountry()));
        assertThat(t.getConflicts()).hasSize(8); // duplicate conflicts are not removed

        // add some absences
        t.addAbsences(new Absence(jA1, r1));
        t.addAbsences(new Absence(jA3, r1));
        t.addAbsences(new Absence(jA2, r2));
        t.addAbsences(new Absence(jA4, r2));
        assertThat(t.getAbsencesPerRound(r1)).isEqualTo(2);
        assertThat(t.getAbsencesPerRound(r2)).isEqualTo(2);
        assertThat(t.isFeasibleSolutionPossible()).isTrue();

        // one more absence
        t.addAbsences(new Absence(jA1, r2));
        assertThat(t.isFeasibleSolutionPossible()).isFalse();
        assertThat(t.getAbsencesPerRound(r2)).isEqualTo(3);

        assertThat(t.getProblemFacts()).hasSize(
                t.getRounds().size()
                + t.getGroups().size()
                + t.getTeams().size()
                + t.getJuries().size()
                + t.getJurors().size()
                + t.getAbsences().size()
                + t.getConflicts().size() + EXTRA_FACTS);

        t.removeAbsences(new ArrayList<>(t.getAbsences()));
        assertThat(t.isFeasibleSolutionPossible()).isTrue();

        // add one more round
        Round r3 = new Round(3, newSize);
        Group g3A = r3.createGroup("A").addTeams(tA, tB, tF);
        Group g3B = r3.createGroup("B").addTeams(tD, tE, tC);
        t.addRounds(r3);
        assertThat(t.getRounds()).contains(r1, r2, r3);
        assertThat(t.getGroups()).contains(g1A, g1B, g2A, g2B, g3A, g3B);
        assertThat(t.getGroups()).hasSize(6);
        assertThat(t.getTeams()).contains(tA, tB, tC, tD, tE, tF);
        assertThat(t.getJuries()).hasSize(t.getGroups().size());
        assertThat(t.getSeats()).hasSize(newSize * t.getJuries().size());
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

        Round r1 = new Round(1, 2);
        r1.createGroup("A").addTeams(tA, tB, tC);
        r1.createGroup("B").addTeams(tD, tE, tF);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(0.0, offset(Double.MIN_VALUE));

        t.addJurors(jA1, jA2, jA3, jA4);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(0.0, offset(Double.MIN_VALUE));
        t.addRounds(r1);
        assertThat(t.getStatistics().getRounds()).isEqualTo(1);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(1.0, offset(Double.MIN_VALUE));

        t.addJurors(jA5, jA6, jB1, jB2, jB3);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(4.0 / 9, offset(Double.MIN_VALUE));

        int numberOfGroups = 2;
        t.changeJurySize(r1, 3);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(6.0 / 9, offset(Double.MIN_VALUE));

        double jurySizeTotal = 7.0;
        Round r2 = new Round(2, 4);
        r2.createGroup("A").addTeams(tA, tB, tC);
        r2.createGroup("B").addTeams(tD, tE, tF);
        t.addRounds(r2);
        assertThat(t.getStatistics().getRounds()).isEqualTo(2);
        assertThat(t.getStatistics().getOptimalLoad())
                .isEqualTo(jurySizeTotal * numberOfGroups / 18, offset(Double.MIN_VALUE));

        t.addAbsences(new Absence(jA1, r1), new Absence(jA2, r1));
        assertThat(t.getStatistics().getOptimalLoad())
                .isEqualTo(jurySizeTotal * numberOfGroups / (18 - 2), offset(Double.MIN_VALUE));

        // check that cloned solution calculates statistics correctly
        Tournament clone = (Tournament) t.cloneSolution();
        clone.changeJurySize(r2, 2);
        jurySizeTotal = 5.0;
        assertThat(clone.getStatistics().getOptimalLoad())
                .isEqualTo(jurySizeTotal * numberOfGroups / (18 - 2), offset(Double.MIN_VALUE));
    }

    @Test
    public void testIndependentRatio() {
        Round round = new Round(1);
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
        int jurySize1 = 7;
        int jurySize2 = 11;
        Round r1 = new Round(1, jurySize1);
        r1.createGroup("A").addTeams(tA, tB, tC);
        Round r2 = new Round(2, jurySize2);
        r2.createGroup("A").addTeams(tA, tB, tC);

        Tournament t = new Tournament();
        t.addRounds(r1, r2);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(0, offset(Double.MIN_VALUE));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(0, offset(Double.MIN_VALUE));

        t.addJurors(jI1, jI2, jT1, jT2);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(jurySize1 * 0.5, offset(Double.MIN_VALUE));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(jurySize2 * 0.5, offset(Double.MIN_VALUE));

        t.changeJurySize(r1, 2);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(1, offset(Double.MIN_VALUE));
        t.changeJurySize(r2, 3);
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(1.5, offset(Double.MIN_VALUE));

        t.changeJurySize(r1, 3);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(1.5, offset(Double.MIN_VALUE));
        t.changeJurySize(r2, 2);
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(1, offset(Double.MIN_VALUE));

        t.addAbsences(new Absence(jI1, r1), new Absence(jT2, r2));
        t.changeJurySize(r2, 3);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(1, offset(Double.MIN_VALUE));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(2, offset(Double.MIN_VALUE));
    }

    @Test
    public void testFeasibilitySimple() {
        int jurySize = 2;
        Round r = new Round(1, jurySize);
        r.createGroup("A").addTeams(tB, tC, tD);
        r.createGroup("B").addTeams(tE, tF, tG);
        Tournament t = new Tournament();
        t.addRounds(r);
        assertThat(t.getSeats()).hasSize(4);
        assertThat(t.getSeats()).hasSize(r.getGroups().size() * jurySize);

        t.addJurors(jA1, jA2, jA3, jA4);
        assertThat(t.isFeasibleSolutionPossible()).isTrue();

        t.addAbsences(new Absence(jA1, r));
        assertThat(t.getAbsencesPerRound(r)).isEqualTo(1);
        assertThat(t.isFeasibleSolutionPossible()).isFalse();
    }

    @Test
    public void testCloneSolution() {
        Round r1 = new Round(1, 2);
        r1.createGroup("A").addTeams(tA, tB, tC);
        r1.createGroup("B").addTeams(tD, tE, tF);

        Tournament t = new Tournament();
        t.addRounds(r1);
        testClone(t);

        Round r2 = new Round(2, 3);
        r2.createGroup("A").addTeams(tA, tB, tC);
        r2.createGroup("B").addTeams(tD, tE, tF);
        t.addRounds(r2);
        testClone(t);

        t.changeJurySize(r1, 3);
        testClone(t);

        t.addJurors(jA1, jB1, jC1);
        t.addAbsences(new Absence(jA1, r1), new Absence(jB1, r2));
        t.addLock(new Lock(jA1, t.getJuries().get(0), 0));

        t.addConflicts(
                new Conflict(jA1, tF.getCountry()),
                new Conflict(jB1, tE.getCountry())
        );
        t.changeJurySize(r1, 1);
        testClone(t);
    }

    @Test
    public void testLocking() {
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF);
        Round r2 = RoundFactory.createRound(2, tA, tB, tC, tD, tE, tF);
        r1.setJurySize(5);
        r2.setJurySize(5);
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

    @Test
    public void testFirstAvailableRound() {
        Tournament t = new Tournament();
        Round r1 = new Round(1);
        Round r2 = new Round(2);
        Round r3 = new Round(3);
        Round r4 = new Round(4);
        Round r5 = new Round(5);
        r1.addGroups(gABC);
        r2.addGroups(gABC);
        r3.addGroups(gABC);
        r4.addGroups(gABC);
        r5.addGroups(gABC);
        t.addRounds(r1, r2, r3, r4, r5);
        t.addJurors(jA1, jA2, jA3);
        t.addAbsences(new Absence(jA1, r2), new Absence(jA2, r4));
        t.addAbsences(new Absence(jA2, r1), new Absence(jA2, r3), new Absence(jA2, r5));
        t.addAbsences(new Absence(jA3, r1), new Absence(jA3, r2), new Absence(jA3, r3), new Absence(jA3, r4));
        assertThat(jA1.getFirstAvailable()).isEqualTo(1);
        assertThat(jA2.getFirstAvailable()).isEqualTo(2);
        assertThat(jA3.getFirstAvailable()).isEqualTo(5);
    }

    @Test
    public void testMaxJurySize() {
        Tournament t = new Tournament();
        Round r1 = new Round(1);
        Round r2 = new Round(2);
        r1.addGroups(gABC, gABC);
        r2.addGroups(gABC, gABC);
        t.addRounds(r1, r2);
        Juror inexperienced = new Juror("No", "Exp", CountryCode.CZ, JurorType.INDEPENDENT, false, false);
        t.addJurors(jA1, jA2, jA3, inexperienced);
        t.addAbsences(new Absence(jA1, r1));
        assertThat(r1.getMaxJurySize()).isEqualTo(1);
        assertThat(r2.getMaxJurySize()).isEqualTo(2);
    }

    private void testClone(Tournament t) {
        Tournament clone = (Tournament) t.cloneSolution();

        // check ordinary getters
        assertThat(clone.getRounds()).isEqualTo(t.getRounds());
        assertThat(clone.getGroups()).isEqualTo(t.getGroups());
        assertThat(clone.getTeams()).isEqualTo(t.getTeams());
        assertThat(clone.getJuries()).isEqualTo(t.getJuries());
        assertThat(clone.getJurors()).isEqualTo(t.getJurors());
        assertThat(clone.getAbsences()).isEqualTo(t.getAbsences());
        assertThat(clone.getConflicts()).isEqualTo(t.getConflicts());
        assertThat(clone.getLocks()).isEqualTo(t.getLocks());
        assertThat(clone.getStatistics()).isEqualTo(t.getStatistics());
        assertThat(clone.getWeightConfig()).isEqualTo(t.getWeightConfig());

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
