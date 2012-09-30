package org.iypt.domain;

import org.iypt.domain.util.DefaultTournamentFactory;
import org.junit.Test;

import static org.iypt.core.TestFacts.*;
import static org.junit.Assert.*;

/**
 *
 * @author jlocker
 */
public class TournamentTest {
    
    @Test
    public void test() {
        DefaultTournamentFactory f = new DefaultTournamentFactory();
        f.setJuryCapacity(2);
        f.addJurors(jA1, jA2, jA3, jA4);
        Round r = f.createRound(1, tB, tC, tD, tE, tF, tG);
        Tournament t = f.newTournament();
        t.addDayOffs(new DayOff(jA1, r.getDay()));
        assertEquals(1, t.dayOffsPerRound(r));
        assertFalse(t.isFeasibleSolutionPossible());
    }
}
