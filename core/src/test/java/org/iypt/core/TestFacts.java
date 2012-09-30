package org.iypt.core;

import org.iypt.domain.CountryCode;
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
    public static final Team tA = new Team(CountryCode.AT);
    public static final Team tB = new Team(CountryCode.BE);
    public static final Team tC = new Team(CountryCode.CZ);
    public static final Team tD = new Team(CountryCode.DE);
    public static final Team tE = new Team(CountryCode.ES);
    public static final Team tF = new Team(CountryCode.FR);
    public static final Team tG = new Team(CountryCode.GB);
    public static final Team tH = new Team(CountryCode.HU);
    public static final Team tI = new Team(CountryCode.IT);
    
    // 26 teams named by numbers
    public static final Team t01 = new Team(CountryCode.AT);
    public static final Team t02 = new Team(CountryCode.BE);
    public static final Team t03 = new Team(CountryCode.CZ);
    public static final Team t04 = new Team(CountryCode.DE);
    public static final Team t05 = new Team(CountryCode.ES);
    public static final Team t06 = new Team(CountryCode.FR);
    public static final Team t07 = new Team(CountryCode.GB);
    public static final Team t08 = new Team(CountryCode.HU);
    public static final Team t09 = new Team(CountryCode.IT);
    public static final Team t10 = new Team(CountryCode.JP);
    public static final Team t11 = new Team(CountryCode.KR);
    public static final Team t12 = new Team(CountryCode.LV);
    public static final Team t13 = new Team(CountryCode.MM);
    public static final Team t14 = new Team(CountryCode.NL);
    public static final Team t15 = new Team(CountryCode.OM);
    public static final Team t16 = new Team(CountryCode.PL);
    public static final Team t17 = new Team(CountryCode.QA);
    public static final Team t18 = new Team(CountryCode.RU);
    public static final Team t19 = new Team(CountryCode.SK);
    public static final Team t20 = new Team(CountryCode.TR);
    public static final Team t21 = new Team(CountryCode.US);
    public static final Team t22 = new Team(CountryCode.VN);
    public static final Team t23 = new Team(CountryCode.WS);
    public static final Team t24 = new Team(CountryCode.MX);
    public static final Team t25 = new Team(CountryCode.YE);
    public static final Team t26 = new Team(CountryCode.ZA);

    // jurors
    public static final Juror jA1 = new Juror(CountryCode.AT);
    public static final Juror jA2 = new Juror(CountryCode.AT);
    public static final Juror jA3 = new Juror(CountryCode.AT);
    public static final Juror jA4 = new Juror(CountryCode.AT);
    public static final Juror jA5 = new Juror(CountryCode.AT);
    public static final Juror jA6 = new Juror(CountryCode.AT);
    public static final Juror jB1 = new Juror(CountryCode.BE);
    public static final Juror jB2 = new Juror(CountryCode.BE);
    public static final Juror jB3 = new Juror(CountryCode.BE);
    public static final Juror jB4 = new Juror(CountryCode.BE);
    public static final Juror jC1 = new Juror(CountryCode.CZ);
    public static final Juror jC2 = new Juror(CountryCode.CZ);
    public static final Juror jC3 = new Juror(CountryCode.CZ);
    public static final Juror jC4 = new Juror(CountryCode.CZ);
    public static final Juror jD1 = new Juror(CountryCode.DE);
    public static final Juror jD2 = new Juror(CountryCode.DE);
    public static final Juror jE1 = new Juror(CountryCode.ES);
    public static final Juror jF1 = new Juror(CountryCode.FR);
    public static final Juror jG1 = new Juror(CountryCode.GB);
    public static final Juror jH1 = new Juror(CountryCode.HU);
    public static final Juror jI1 = new Juror(CountryCode.IT);
    public static final Juror jJ1 = new Juror(CountryCode.JP);
    public static final Juror jK1 = new Juror(CountryCode.KR);
    public static final Juror jL1 = new Juror(CountryCode.LV);
    public static final Juror jM1 = new Juror(CountryCode.MM);
    public static final Juror jM2 = new Juror(CountryCode.MM);
    public static final Juror jM3 = new Juror(CountryCode.MM);
    public static final Juror jM4 = new Juror(CountryCode.MM);
    public static final Juror jM5 = new Juror(CountryCode.MM);
    public static final Juror jM6 = new Juror(CountryCode.MM);
    public static final Juror jN1 = new Juror(CountryCode.NL);
    public static final Juror jN2 = new Juror(CountryCode.NL);
    public static final Juror jN3 = new Juror(CountryCode.NL);
    public static final Juror jN4 = new Juror(CountryCode.NL);
    public static final Juror jN5 = new Juror(CountryCode.NL);
    public static final Juror jN6 = new Juror(CountryCode.NL);
    public static final Juror jY1 = new Juror(CountryCode.YE);
    public static final Juror jZ1 = new Juror(CountryCode.ZA);
    
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
