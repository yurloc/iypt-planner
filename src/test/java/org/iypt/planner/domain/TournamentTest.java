package org.iypt.planner.domain;

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
        assertThat(empty.getJury().getCapacity(), is(Group.DEFAULT_JURY_CAPACITY));

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
        assertFalse(t.changeJuryCapacity(Group.DEFAULT_JURY_CAPACITY - 1));

        t.addRounds(r1, r2);
        // getRounds, getGroups, getTeams
        assertThat(t.getRounds(), hasItems(r1, r2));
        assertThat(t.getGroups(), hasItems(g1A, g1B, g2A, g2B));
        assertThat(t.getTeams(), hasItems(tA, tB, tC, tD, tE, tF));
        // getJuries
        assertThat(t.getJuries().size(), is(t.getGroups().size()));
        // getJuryMemberships
        assertThat(t.getJuryMemberships().size(), is(t.getJuries().size() * Group.DEFAULT_JURY_CAPACITY));

        // changeJuryCapacity
        assertFalse(t.changeJuryCapacity(Group.DEFAULT_JURY_CAPACITY));
        int newCapacity = Group.DEFAULT_JURY_CAPACITY + 1;
        assertTrue(t.changeJuryCapacity(newCapacity));
        assertThat(t.getJuries().size(), is(t.getGroups().size()));
        assertThat(t.getJuryMemberships().size(), is(t.getJuries().size() * newCapacity));

        assertThat(t.getProblemFacts().size(),
                is(t.getRounds().size()
                + t.getGroups().size()
                + t.getTeams().size()
                + t.getJuries().size()));

        assertFalse(t.isFeasibleSolutionPossible());
        assertThat(t.getDayOffsPerRound(r1), is(0));
        assertThat(t.getDayOffsPerRound(r2), is(0));

        newCapacity = 2;
        t.changeJuryCapacity(newCapacity);
        assertThat(t.getJuryMemberships().size(), is(newCapacity * t.getJuries().size()));

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
    }

    @Test
    public void testFeasibilitySimple() {
        Round r = new Round(1, 1);
        r.createGroup("A").addTeams(tB, tC, tD);
        r.createGroup("B").addTeams(tE, tF, tG);
        Tournament t = new Tournament();
        t.addRounds(r);
        int capacity = 2;
        t.changeJuryCapacity(capacity);
        assertThat(t.getJuryMemberships().size(), is(4));
        assertThat(t.getJuryMemberships().size(), is(r.getGroups().size() * capacity));

        t.addJurors(jA1, jA2, jA3, jA4);
        assertTrue(t.isFeasibleSolutionPossible());

        t.addDayOffs(new DayOff(jA1, r.getDay()));
        assertThat(t.getDayOffsPerRound(r), is(1));
        assertFalse(t.isFeasibleSolutionPossible());
    }
}
