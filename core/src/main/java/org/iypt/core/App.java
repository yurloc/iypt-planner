package org.iypt.core;

import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.iypt.domain.Country;
import org.iypt.domain.DayOff;
import org.iypt.domain.Group;
import org.iypt.domain.Juror;
import org.iypt.domain.Jury;
import org.iypt.domain.Round;
import org.iypt.domain.Team;
import org.iypt.domain.Tournament;

public class App {

    public static void main(String[] args) {
        // Build the Solver
        SolverFactory solverFactory = new XmlSolverFactory(
                "/org/iypt/core/config.xml");
        Solver solver = solverFactory.buildSolver();

        Tournament unsolved = createTournament();

        // Solve the problem
        solver.setPlanningProblem(unsolved);
        solver.solve();
        Tournament solved = (Tournament) solver.getBestSolution();

        // Display the result
        System.out.println("\nSolved Tournament:\n" + toDisplayString(solved));
    }

    private static Tournament createTournament() {
        // teams
        Team tA = new Team(Country.A);
        Team tB = new Team(Country.B);
        Team tC = new Team(Country.C);
        Team tD = new Team(Country.D);
        Team tE = new Team(Country.E);
        Team tF = new Team(Country.F);
        Team tG = new Team(Country.G);
        Team tH = new Team(Country.H);
        Team tI = new Team(Country.I);
        
        // jurors
        Juror jA = new Juror(Country.A);
        Juror jB = new Juror(Country.B);
        Juror jC = new Juror(Country.C);
        Juror jD = new Juror(Country.D);
        Juror jE = new Juror(Country.E);
        Juror jF = new Juror(Country.F);
        Juror jG = new Juror(Country.G);
        Juror jH = new Juror(Country.H);
        Juror jI = new Juror(Country.I);
        Juror jJ = new Juror(Country.J);
        Juror jK = new Juror(Country.K);
        
        // round 1
        Group g1A = new Group(tA, tB, tC);
        Group g1B = new Group(tD, tE, tF);
        Group g1C = new Group(tG, tH, tI);
        Round r1 = new Round(1, 1);
        r1.addGroups(g1A, g1B, g1C);
        
        // round 2
        Round r2 = new Round(2, 2);
        Group g2A = new Group(tA, tD, tG);
        Group g2B = new Group(tB, tE, tH);
        Group g2C = new Group(tC, tF, tI);
        r2.addGroups(g2A, g2B, g2C);
        
        // round 3
        Round r3 = new Round(3, 3);
        Group g3A = new Group(tA, tH, tF);
        Group g3B = new Group(tB, tD, tI);
        Group g3C = new Group(tC, tE, tG);
        r3.addGroups(g3A, g3B, g3C);
        
        // tournament
        Tournament t = new Tournament();
        t.addRounds(r1, r2, r3);
        t.addJurors(jA, jB, jC);
        t.addDayOffs(new DayOff(jA, r1.getDay()), new DayOff(jA, r3.getDay()));
        // FIXME temporary dirty way to set jury size
        for (Jury j : t.getJuries()) j.setCapacity(3);
        
        return t;
    }

    private static String toDisplayString(Tournament tournament) {
        StringBuilder re = new StringBuilder(1024);
        for (Round r : tournament.getRounds()) {
            re.append(r).append("=========");
            re.append(" group             | jury");
            //         A: CZE,SVK,GER     | ...
            for (Group g : r.getGroups()) {
                re.append(g.getName()).append(": ");
                for (Team t : g.getTeams()) {
                    re.append(t.getCountry()).append(' ');
                }
                if (g.getSize() == 3) re.append("    ");
                re.append("| ");
                for (Juror j : g.getJury().getMembers()) {
                    re.append(j.getCountry());
                }
            }
        }
        return re.toString();
    }
}
