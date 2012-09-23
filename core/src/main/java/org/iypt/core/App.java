package org.iypt.core;

import java.util.Iterator;
import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
import org.iypt.domain.Country;
import org.iypt.domain.DayOff;
import org.iypt.domain.Group;
import org.iypt.domain.Juror;
import org.iypt.domain.JuryMembership;
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
        ScoreDirector scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
        scoreDirector.setWorkingSolution(solved);
        Score finalScore = scoreDirector.calculateScore();
        System.out.println("Final score: " + finalScore);
        System.out.println("Explanation:\n");
        
        WorkingMemory workingMemory = ((DroolsScoreDirector) scoreDirector).getWorkingMemory();
        
        @SuppressWarnings("unchecked")
        Iterator<ConstraintOccurrence> it = (Iterator<ConstraintOccurrence>) workingMemory.iterateObjects(
                new ClassObjectFilter(ConstraintOccurrence.class));
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
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
        Juror jG = new Juror(Country.C);
        Juror jH = new Juror(Country.C);
        Juror jI = new Juror(Country.A); // 9
        Juror jJ = new Juror(Country.A);
        Juror jK = new Juror(Country.B);
        Juror jL = new Juror(Country.B); //12
        Juror jM = new Juror(Country.B);
        Juror jN = new Juror(Country.C);
        Juror jO = new Juror(Country.D); //15
        Juror jP = new Juror(Country.A);
        Juror jQ = new Juror(Country.A);
        Juror jR = new Juror(Country.A); //18
        
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

        g1A.setName("A");
        g2A.setName("A");
        g3A.setName("A");
        g1B.setName("B");
        g2B.setName("B");
        g3B.setName("B");
        g1C.setName("C");
        g2C.setName("C");
        g3C.setName("C");
        
        // tournament
        Tournament t = new Tournament();
        t.addRounds(r1, r2, r3);
        t.addJurors(jA, jB, jC, jD, jE, jF, jG, jH, jG, jI, jJ, jK, jL);
        t.addJurors(jM, jN, jO, jP, jQ, jR);
        t.addDayOffs(new DayOff(jA, r1.getDay()), new DayOff(jA, r3.getDay()));
        
        t.createJuries(r1, 6);
        t.createJuries(r2, 6);
        t.createJuries(r3, 6);
        
        return t;
    }

    private static String toDisplayString(Tournament tournament) {
        StringBuilder re = new StringBuilder(1024);
        for (Round r : tournament.getRounds()) {
            re.append(r).append("\n=========\n");
            re.append(" group       | jury\n");
            //         A: A B C D | ...
            for (Group g : r.getGroups()) {
                re.append(g.getName()).append(": ");
                for (Team t : g.getTeams()) {
                    re.append(t.getCountry()).append(' ');
                }
                if (g.getSize() == 3) re.append("    ");
                re.append("| ");
                for (JuryMembership m : tournament.getJuryMemberships()) {
                    if (m.getJury().equals(g.getJury())) {
                        Juror juror = m.getJuror();
                        re.append(juror == null ? "[---]" : juror.getCountry());
                    }
                }
                re.append('\n');
            }
        }
        return re.toString();
    }
}