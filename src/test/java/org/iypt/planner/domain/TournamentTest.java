package org.iypt.planner.domain;

import java.util.Collection;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.iypt.planner.domain.util.SampleFacts.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

/**
 *
 * @author jlocker
 */
public class TournamentTest {

    @Test
    public void testRound() {
        // createGroup
        Round r1 = new Round(1, 1);
        Group g1A = r1.createGroup("A");
        assertThat(g1A.getRound(), sameInstance(r1));

        // addGroups
        Group g1B = new Group("B");
        Group g1C = new Group("C");
        r1.addGroups(g1B, g1C);
        assertThat(g1B.getRound(), sameInstance(r1));
        assertThat(g1C.getRound(), sameInstance(r1));
        assertThat(r1.getGroups(), hasItems(g1A, g1B, g1C));
    }

    @Test
    public void testGroup() {
        Group empty = new Group("A");
        assertThat(empty.getSize(), is(0));
        assertThat(empty.getJury().getCapacity(), is(Jury.DEFAULT_CAPACITY));

        assertThat(new Group(tA, tB, tC).getSize(), is(3));
        assertThat(new Group(tA, tB, tC, tD).getSize(), is(4));
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
        int newCapacity = Jury.DEFAULT_CAPACITY - 1;
        assertThat(t.setJuryCapacity(newCapacity), is(false)); // affects no juries

        t.addRounds(r1, r2);
        // getRounds, getGroups, getTeams
        assertThat(t.getRounds(), hasItems(r1, r2));
        assertThat(t.getGroups(), hasItems(g1A, g1B, g2A, g2B));
        assertThat(t.getGroups().size(), is(4));
        assertThat(t.getTeams(), hasItems(tA, tB, tC, tD, tE, tF));
        // getJuries
        assertThat(t.getJuries().size(), is(t.getGroups().size()));
        // getJurySeats
        assertThat(t.getJurySeats().size(), is(newCapacity * t.getJuries().size()));

        // setJuryCapacity
        assertThat(t.setJuryCapacity(Jury.DEFAULT_CAPACITY), is(true));
        newCapacity = Jury.DEFAULT_CAPACITY + 1;
        assertThat(t.setJuryCapacity(newCapacity), is(true));
        assertThat(t.getJuries().size(), is(t.getGroups().size()));
        assertThat(t.getJurySeats().size(), is(newCapacity * t.getJuries().size()));

        assertThat(t.getProblemFacts().size(),
                is(t.getRounds().size()
                + t.getGroups().size()
                + t.getTeams().size()
                + t.getJuries().size()));

        assertFalse(t.isFeasibleSolutionPossible());
        assertThat(t.getDayOffsPerRound(r1), is(0));
        assertThat(t.getDayOffsPerRound(r2), is(0));

        newCapacity = 2;
        t.setJuryCapacity(newCapacity);
        assertThat(t.getJurySeats().size(), is(newCapacity * t.getJuries().size()));

        t.addJurors(jA1, jA2, jA3);
        assertFalse(t.isFeasibleSolutionPossible());
        t.addJurors(jA4);
        assertTrue(t.isFeasibleSolutionPossible());
        t.addJurors(jA5, jA6);
        assertTrue(t.isFeasibleSolutionPossible());
        
        // add some day offs
        t.addDayOffs(new DayOff(jA1, 1));
        t.addDayOffs(new DayOff(jA3, 1));
        t.addDayOffs(new DayOff(jA2, 2));
        t.addDayOffs(new DayOff(jA4, 2));
        assertThat(t.getDayOffsPerRound(r1), is(2));
        assertThat(t.getDayOffsPerRound(r2), is(2));
        assertTrue(t.isFeasibleSolutionPossible());

        // one more day off
        t.addDayOffs(new DayOff(jA1, 2));
        assertFalse(t.isFeasibleSolutionPossible());
        assertThat(t.getDayOffsPerRound(r2), is(3));

        assertThat(t.getProblemFacts().size(),
                is(t.getRounds().size()
                + t.getGroups().size()
                + t.getTeams().size()
                + t.getJuries().size()
                + t.getJurors().size()
                + t.getDayOffs().size()
                + t.getConflicts().size()));

        t.getDayOffs().clear();
        assertTrue(t.isFeasibleSolutionPossible());

        // add one more round
        Round r3 = new Round(3, 3);
        Group g3A = r3.createGroup("A").addTeams(tA, tB, tF);
        Group g3B = r3.createGroup("B").addTeams(tD, tE, tC);
        t.addRounds(r3);
        assertThat(t.getRounds(), hasItems(r1, r2, r3));
        assertThat(t.getGroups(), hasItems(g1A, g1B, g2A, g2B, g3A, g3B));
        assertThat(t.getGroups().size(), is(6));
        assertThat(t.getTeams(), hasItems(tA, tB, tC, tD, tE, tF));
        assertThat(t.getJuries().size(), is(t.getGroups().size()));
        assertThat(t.getJurySeats().size(), is(newCapacity * t.getJuries().size()));
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
        assertThat(t.getJurySeats().size(), is(4));
        assertThat(t.getJurySeats().size(), is(r.getGroups().size() * capacity));

        t.addJurors(jA1, jA2, jA3, jA4);
        assertTrue(t.isFeasibleSolutionPossible());

        t.addDayOffs(new DayOff(jA1, r.getDay()));
        assertThat(t.getDayOffsPerRound(r), is(1));
        assertFalse(t.isFeasibleSolutionPossible());
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

        t.setJuryCapacity(Jury.DEFAULT_CAPACITY);
        testClone(t);

        t.addJurors(jA1, jB1, jC1);
        t.addDayOffs(new DayOff(jA1, 1), new DayOff(jB1, 2));
        // TODO add conflicts
        testClone(t);
    }

    private void testClone(Tournament t) {
        Tournament clone = (Tournament) t.cloneSolution();

        // check ordinary getters
        assertThat(clone.getRounds(), is(t.getRounds()));
        assertThat(clone.getGroups(), is(t.getGroups()));
        assertThat(clone.getTeams(), is(t.getTeams()));
        assertThat(clone.getJuries(), is(t.getJuries()));
        assertThat(clone.getJurors(), is(t.getJurors()));
        assertThat(clone.getDayOffs(), is(t.getDayOffs()));
        assertThat(clone.getConflicts(), is(t.getConflicts()));

        // check getProblemFacts
        @SuppressWarnings("unchecked")
        Collection<Object> origFacts = (Collection<Object>) t.getProblemFacts();
        @SuppressWarnings("unchecked")
        Collection<Object> cloneFacts = (Collection<Object>) clone.getProblemFacts();
        assertThat(cloneFacts, is(origFacts));

        // same number of planning entities
        assertThat(clone.getJurySeats().size(), is(t.getJurySeats().size()));
        // no entities in common
        assertThat(t.getJurySeats().removeAll(clone.getJurySeats()), is(false));
    }

}
