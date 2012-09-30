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
    public static final Team tA = new Team(Country.A);
    public static final Team tB = new Team(Country.B);
    public static final Team tC = new Team(Country.C);
    public static final Team tD = new Team(Country.D);
    public static final Team tE = new Team(Country.E);
    public static final Team tF = new Team(Country.F);
    public static final Team tG = new Team(Country.G);
    public static final Team tH = new Team(Country.H);
    public static final Team tI = new Team(Country.I);
    
    // 26 teams named by numbers
    public static final Team t01 = new Team(Country.A);
    public static final Team t02 = new Team(Country.B);
    public static final Team t03 = new Team(Country.C);
    public static final Team t04 = new Team(Country.D);
    public static final Team t05 = new Team(Country.E);
    public static final Team t06 = new Team(Country.F);
    public static final Team t07 = new Team(Country.G);
    public static final Team t08 = new Team(Country.H);
    public static final Team t09 = new Team(Country.I);
    public static final Team t10 = new Team(Country.J);
    public static final Team t11 = new Team(Country.K);
    public static final Team t12 = new Team(Country.L);
    public static final Team t13 = new Team(Country.M);
    public static final Team t14 = new Team(Country.N);
    public static final Team t15 = new Team(Country.O);
    public static final Team t16 = new Team(Country.P);
    public static final Team t17 = new Team(Country.Q);
    public static final Team t18 = new Team(Country.R);
    public static final Team t19 = new Team(Country.S);
    public static final Team t20 = new Team(Country.T);
    public static final Team t21 = new Team(Country.U);
    public static final Team t22 = new Team(Country.V);
    public static final Team t23 = new Team(Country.W);
    public static final Team t24 = new Team(Country.X);
    public static final Team t25 = new Team(Country.Y);
    public static final Team t26 = new Team(Country.Z);

    // jurors
    public static final Juror jA1 = new Juror(Country.A);
    public static final Juror jA2 = new Juror(Country.A);
    public static final Juror jA3 = new Juror(Country.A);
    public static final Juror jA4 = new Juror(Country.A);
    public static final Juror jA5 = new Juror(Country.A);
    public static final Juror jA6 = new Juror(Country.A);
    public static final Juror jB1 = new Juror(Country.B);
    public static final Juror jB2 = new Juror(Country.B);
    public static final Juror jB3 = new Juror(Country.B);
    public static final Juror jB4 = new Juror(Country.B);
    public static final Juror jC1 = new Juror(Country.C);
    public static final Juror jC2 = new Juror(Country.C);
    public static final Juror jC3 = new Juror(Country.C);
    public static final Juror jC4 = new Juror(Country.C);
    public static final Juror jD1 = new Juror(Country.D);
    public static final Juror jD2 = new Juror(Country.D);
    public static final Juror jE1 = new Juror(Country.E);
    public static final Juror jF1 = new Juror(Country.F);
    public static final Juror jG1 = new Juror(Country.G);
    public static final Juror jH1 = new Juror(Country.H);
    public static final Juror jI1 = new Juror(Country.I);
    public static final Juror jJ1 = new Juror(Country.J);
    public static final Juror jK1 = new Juror(Country.K);
    public static final Juror jL1 = new Juror(Country.L);
    public static final Juror jM1 = new Juror(Country.M);
    public static final Juror jM2 = new Juror(Country.M);
    public static final Juror jM3 = new Juror(Country.M);
    public static final Juror jM4 = new Juror(Country.M);
    public static final Juror jM5 = new Juror(Country.M);
    public static final Juror jM6 = new Juror(Country.M);
    public static final Juror jN1 = new Juror(Country.N);
    public static final Juror jN2 = new Juror(Country.N);
    public static final Juror jN3 = new Juror(Country.N);
    public static final Juror jN4 = new Juror(Country.N);
    public static final Juror jN5 = new Juror(Country.N);
    public static final Juror jN6 = new Juror(Country.N);
    public static final Juror jY1 = new Juror(Country.Y);
    public static final Juror jZ1 = new Juror(Country.Z);
    
    // group permutation #1
    public static final Group gABC = new Group(tA, tB, tC);
    public static final Group gDEF = new Group(tD, tE, tF);
    public static final Group gGHI = new Group(tG, tH, tI);
    
    // group permutation #2
    public static final Group gADG = new Group(tA, tD, tG);
    public static final Group gBEH = new Group(tB, tE, tH);
    public static final Group gCFI = new Group(tC, tF, tI);
    
    // group permutation #3
    public static final Group gAEI = new Group(tA, tE, tI);
    public static final Group gBFG = new Group(tB, tF, tG);
    public static final Group gCDH = new Group(tC, tD, tH);
    
    // group permutation #4
    public static final Group gAFH = new Group(tA, tF, tH);
    public static final Group gBDI = new Group(tB, tD, tI);
    public static final Group gCEG = new Group(tC, tE, tG);
    
    // rounds
    public static final Round round1 = new Round(1, 1);
    public static final Round round2 = new Round(2, 2);
    public static final Round round3 = new Round(3, 3);
    public static final Round round4 = new Round(4, 4);
    public static final Round round5 = new Round(5, 5);
}
