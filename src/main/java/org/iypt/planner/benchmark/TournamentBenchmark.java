package org.iypt.planner.benchmark;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

public class TournamentBenchmark {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "org/iypt/planner/benchmark/tournamentBenchmarkConfig.xml");
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmark();
    }
}
