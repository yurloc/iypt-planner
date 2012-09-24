package org.iypt.core;

import org.iypt.domain.Country;
import org.iypt.domain.Group;
import org.iypt.domain.Juror;
import org.iypt.domain.Round;
import org.iypt.domain.Team;

/**
 *
 * @author jlocker
 */
public class TestFacts {

    // teams
    static final Team tA = new Team(Country.A);
    static final Team tB = new Team(Country.B);
    static final Team tC = new Team(Country.C);
    static final Team tD = new Team(Country.D);
    static final Team tE = new Team(Country.E);
    static final Team tF = new Team(Country.F);
    static final Team tG = new Team(Country.G);
    static final Team tH = new Team(Country.H);
    static final Team tI = new Team(Country.I);

    // jurors
    static final Juror jA1 = new Juror(Country.A);
    static final Juror jA2 = new Juror(Country.A);
    static final Juror jA3 = new Juror(Country.A);
    static final Juror jA4 = new Juror(Country.A);
    static final Juror jA5 = new Juror(Country.A);
    static final Juror jA6 = new Juror(Country.A);
    static final Juror jB1 = new Juror(Country.B);
    static final Juror jB2 = new Juror(Country.B);
    static final Juror jB3 = new Juror(Country.B);
    static final Juror jB4 = new Juror(Country.B);
    static final Juror jC1 = new Juror(Country.C);
    static final Juror jC2 = new Juror(Country.C);
    static final Juror jC3 = new Juror(Country.C);
    static final Juror jC4 = new Juror(Country.C);
    static final Juror jD1 = new Juror(Country.D);
    static final Juror jD2 = new Juror(Country.D);
    static final Juror jE1 = new Juror(Country.E);
    static final Juror jF1 = new Juror(Country.F);
    static final Juror jG1 = new Juror(Country.G);
    static final Juror jH1 = new Juror(Country.H);
    static final Juror jI1 = new Juror(Country.I);
    static final Juror jJ1 = new Juror(Country.J);
    static final Juror jK1 = new Juror(Country.K);
    static final Juror jL1 = new Juror(Country.L);
    
    // group permutation #1
    static final Group gABC = new Group(tA, tB, tC);
    static final Group gDEF = new Group(tD, tE, tF);
    static final Group gGHI = new Group(tG, tH, tI);
    
    // group permutation #2
    static final Group gADG = new Group(tA, tD, tG);
    static final Group gBEH = new Group(tB, tE, tH);
    static final Group gCFI = new Group(tC, tF, tI);
    
    // group permutation #3
    static final Group gAEI = new Group(tA, tE, tI);
    static final Group gBFG = new Group(tB, tF, tG);
    static final Group gCDH = new Group(tC, tD, tH);
    
    // group permutation #4
    static final Group gAFH = new Group(tA, tF, tH);
    static final Group gBDI = new Group(tB, tD, tI);
    static final Group gCEG = new Group(tC, tE, tG);
    
    // rounds
    static final Round round1 = new Round(1, 1);
    static final Round round2 = new Round(2, 2);
    static final Round round3 = new Round(3, 3);
    static final Round round4 = new Round(4, 4);
    static final Round round5 = new Round(5, 5);
}
