package org.iypt.planner.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.domain.Absence;
import org.iypt.planner.domain.Conflict;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurorType;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.RoundFactory;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.Match;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScoreHolder;
import org.optaplanner.core.api.score.holder.ScoreHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.iypt.planner.Constants.*;
import static org.iypt.planner.domain.SampleFacts.*;
import static org.iypt.planner.solver.ScoringRulesTest.RuleType.*;

/**
 *
 * @author jlocker
 */
public class ScoringRulesTest {

    // TODO maybe add test for accumulatedBias rule
    // TODO test that each soft constraint rule reads CO weight from WeightConfig fact!
    private static final Logger LOG = LoggerFactory.getLogger(ScoringRulesTest.class);
    private static final String SCORE_DRL = "org/iypt/planner/solver/score_rules.drl";
    private static final String SCORE_HOLDER_NAME = "scoreHolder";
    private static KieContainer kieContainer;
    private static WeightConfig wconfig;

    @BeforeClass
    public static void setUpClass() {

        // prepare kie container
        KieServices kieServices = KieServices.Factory.get();
        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.writeKModuleXML(kieModuleModel.toXML());
        kfs.write(kieServices.getResources().newClassPathResource(SCORE_DRL).setResourceType(ResourceType.DRL));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        assertThat(kieBuilder.getResults().getMessages(Message.Level.ERROR)).isEmpty();
        kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());

        // do some checks
        KiePackage pkg = kieContainer.getKieBase().getKiePackage("org.iypt.planner.solver");
        // check the ScoreHolder global name
        assertThat(pkg.getGlobalVariables().iterator().next().getName()).as("Unexpected ScoreHolder global name")
                .isEqualTo(SCORE_HOLDER_NAME);

        // enforce that each rule is enumerated in ScoreRules
        // for example this will make sure that each rule either has correct constraint type metadata
        // or is marked as auxiliary, no typos in rule names, etc.
        for (Rule rule : pkg.getRules()) {
            ScoringRule.valueOf(rule.getName());
        }

        // prepare testing weight configuration
        wconfig = new WeightConfig() {
            @Override
            public int getWeight(String ruleId) {
                return ScoringRule.valueOf(ruleId).weight;
            }

            @Override
            public void setWeight(String ruleId, int weight) {
                // do nothing
            }

            @Override
            public String toString() {
                // this will appear when printing the activation list
                return "Test weight config";
            }
        };
    }

    /**
     * Make sure that each scoring rule has constraint type metadata with a correct value (hard or soft). Non-constraint rules
     * must be marked as auxiliary in {@link ScoringRule} enumeration.
     */
    @Test
    public void testMetadata() {
        for (KiePackage pkg : kieContainer.getKieBase().getKiePackages()) {
            for (Rule rule : pkg.getRules()) {
                Map<String, Object> metaData = rule.getMetaData();
                if (metaData.containsKey(CONSTRAINT_TYPE_KEY)) {
                    assertThat(metaData.get(CONSTRAINT_TYPE_KEY))
                            .as(String.format("Wrong value of '%s' metadata of rule '%s'.", CONSTRAINT_TYPE_KEY, rule.getName()))
                            .isIn(CONSTRAINT_TYPE_HARD, CONSTRAINT_TYPE_SOFT);
                } else {
                    assertThat(ScoringRule.valueOf(rule.getName()).type)
                            .as(String.format("Rule %s is probably missing %s metadata:", rule.getName(), CONSTRAINT_TYPE_KEY))
                            .isEqualTo(AUX);

                }
            }
        }
    }

    @Test
    public void testIYPT2012() throws IOException {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jury_data.csv", "schedule2012.csv");
        Tournament t = factory.newTournament();
        t.setWeightConfig(wconfig);

        LOG.debug("Optimal juror load for IYPT2012: {}", t.getStatistics().getOptimalLoad());
        LOG.debug("Optimal chair load for IYPT2012: {}", t.getStatistics().getOptimalChairLoad());
        checkSolution(t, true, true,
                new RuleFiring(ScoringRule.accumulatedBias, 45),
                new RuleFiring(ScoringRule.independentRatioDeltaExceeded, 2),
                new RuleFiring(ScoringRule.jurorAndJurorConflict, 8),
                new RuleFiring(ScoringRule.jurorMeetsBigGroupOften, 4),
                new RuleFiring(ScoringRule.loadDeltaExceeded, 19),
                new RuleFiring(ScoringRule.chairLoadDeltaExceeded, 4),
                new RuleFiring(ScoringRule.teamAndJurorAlreadyMet, 110));
    }

    @Test
    public void testMultiSeat() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF);
        r1.setJurySize(2);
        t.addRounds(r1);
        t.addJurors(jG1, jH1, jI1);

        assignJurors(t, jG1, jG1, jH1, jI1);
        checkSolution(t, false, ScoringRule.multipleSeatsInRound, 1);

        Round r2 = RoundFactory.createRound(2, tA, tB, tC, tD, tE, tF);
        r2.setJurySize(2);
        t.addRounds(r2);
        assignJurors(t, jG1, jG1, jH1, jI1, jH1, jG1, jI1, jG1);
        checkSolution(t, false, ScoringRule.multipleSeatsInRound, 2);
    }

    @Test
    public void testEmptySeat() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        r1.setJurySize(3);
        t.addRounds(r1);
        t.addJurors(jD1);

        // detecting 'null' jurors was intentionally dropped from scoring rules since uninitialized entities are not inserted
        // into working memory
        assignJurors(t, null, Juror.NULL, jD1);
        checkSolution(t, false, ScoringRule.emptySeat, 1);
    }

    @Test
    public void testInexperiencedVoting() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        Round r2 = RoundFactory.createRound(2, tD, tE, tF);
        Round r3 = RoundFactory.createRound(3, tG, tH, tI);
        r1.setJurySize(2);
        r2.setJurySize(2);
        r3.setJurySize(2);
        t.addRounds(r1, r2, r3);
        t.addJurors(jM1, jM2, jM7);

        assignJurors(t, jM1, jM7, jM1, jM7, jM1, jM7);
        checkSolution(t, true, ScoringRule.inexperiencedJurorVoting, 1);

        assignJurors(t, jM1, jM2, jM1, jM7, jM1, jM7);
        checkSolution(t, true, ScoringRule.inexperiencedJurorVoting, 0);

        // add an absence and keep the assignment unchanged
        t.addAbsences(new Absence(jM7, r1));
        checkSolution(t, true, ScoringRule.inexperiencedJurorVoting, 1);
    }

    @Test
    public void testTeamAndJurorSameCountry() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        r1.setJurySize(1);
        t.addRounds(r1);
        t.addJurors(jA1, jD1);

        // simple country conflict
        assignJurors(t, jA1);
        checkSolution(t, false, ScoringRule.teamAndJurorSameCountry, 1);

        // add another juror with multiple conflicts
        t.changeJurySize(r1, 2);
        assignJurors(t, jA1, jD1);
        t.addConflicts(
                new Conflict(jD1, tB.getCountry()),
                new Conflict(jD1, tC.getCountry())
        );
        checkSolution(t, false, ScoringRule.teamAndJurorSameCountry, 3);
    }

    @Test
    public void testAbsenceRule() {
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        r1.setJurySize(1);
        Tournament t = new Tournament();
        t.addRounds(r1);
        t.addJurors(jD1);

        assignJurors(t, jD1);
        t.addAbsences(new Absence(jD1, r1));
        checkSolution(t, false, ScoringRule.absentJuror, 1);
    }

    @Test
    public void testInvalidChair() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF);
        r1.setJurySize(2);
        t.addRounds(r1);
        t.addJurors(jA1, jA2, jD1, jD2);

        // invalid chair
        assignJurors(t, jD2, jD1, jA1, jA2);
        checkSolution(t, false, ScoringRule.invalidChair, 1);

        // two invalid chairs
        assignJurors(t, jD2, jD1, jA2, jA1);
        checkSolution(t, false, ScoringRule.invalidChair, 2);

        // both chair seats occupied
        assignJurors(t, jD1, jD2, jA1, jA2);
        checkSolutionFeasible(t);
    }

    @Test
    public void testTeamAndChairMeetOften() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        Round r2 = RoundFactory.createRound(2, tA, tD, tE);
        Round r3 = RoundFactory.createRound(3, tA, tB, tC);
        Round r4 = RoundFactory.createRound(4, tA, tD, tE);
        r1.setJurySize(2);
        r2.setJurySize(2);
        r3.setJurySize(2);
        r4.setJurySize(2);
        t.addRounds(r1, r2, r3, r4);
        t.addJurors(jM1, jM2, jM3, jN1);

        assignJurors(t, jM1, jM2, jM1, jM2, jM1, jM2, jM1, jM3);
        // jM2 doesn't trigger the rule because he is not a chair (not even a chair candidate)
        checkSolution(t, false, ScoringRule.teamAndChairMeetOften, 1);

        assignJurors(t, jM1, jN1, jM1, jN1, jM1, jN1, jM1, jM3);
        // jN1 doesn't trigger the rule because he is not a chair (even though he's a chair candidate)
        checkSolution(t, false, ScoringRule.teamAndChairMeetOften, 1);
    }

    @Test
    public void testTeamAndChairMeetTwice() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tD);
        Round r2 = RoundFactory.createRound(2, tA, tC, tE);
        Round r3 = RoundFactory.createRound(3, tA, tB, tE);
        Round r4 = RoundFactory.createRound(4, tA, tC, tD);
        r1.setJurySize(2);
        r2.setJurySize(2);
        r3.setJurySize(2);
        r4.setJurySize(2);
        t.addRounds(r1, r2, r3, r4);
        t.addJurors(jM1, jM2, jM3, jN1);

        assignJurors(t, jM1, jM2, jM1, jM2, jN1, jM3, jN1, jM3);
        checkSolution(t, true, ScoringRule.teamAndChairMeetTwice, 2);
    }

    @Test
    public void testTeamAndJurorAlreadyMet() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF);
        Round r2 = RoundFactory.createRound(2, tA, tE, tF, tB, tC, tD);
        r1.setJurySize(2);
        r2.setJurySize(2);
        t.addRounds(r1, r2);
        t.addJurors(jJ1, jK1, jL1, jM1, jM2, jM3);

        assignJurors(t, jJ1, jM2, jK1, jM3, jL1, jM3, jM1, jM2);
        checkSolution(t, true, ScoringRule.teamAndJurorAlreadyMet, 4);
    }

    @Test
    public void testJurorBalance() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        Round r2 = RoundFactory.createRound(2, tA, tD, tE);
        Round r3 = RoundFactory.createRound(3, tA, tB, tC);
        Round r4 = RoundFactory.createRound(4, tA, tD, tE);
        Round r5 = RoundFactory.createRound(5, tA, tB, tC);
        r1.setJurySize(2);
        r2.setJurySize(2);
        r3.setJurySize(2);
        r4.setJurySize(2);
        r5.setJurySize(2);
        t.addRounds(r1, r2, r3, r4, r5);
        t.addJurors(jI1, jJ1, jK1, jL1, jM1); // chairs
        t.addJurors(jM2, jM3, jM4);
        assertThat(t.getStatistics().getOptimalLoad()).isEqualTo(2.0 / 8, offset(Double.MIN_VALUE));
        assignJurors(t, jI1, jM2, jJ1, jM3, jK1, jM4, jL1, jM3, jM1, jM2); // ok
        checkSolutionFeasible(t);

        // jM2 is overloaded, jM3 and jM4 are unused
        assignJurors(t, jI1, jM2, jJ1, jM2, jK1, jM2, jL1, jM2, jM1, jM2);
        checkSolution(t, true, ScoringRule.loadDeltaExceeded, 3);
        // TODO add absences
    }

    @Test
    public void testChairBalance() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF);
        Round r2 = RoundFactory.createRound(2, tA, tD, tE, tD, tE, tF);
        Round r3 = RoundFactory.createRound(3, tA, tB, tC, tD, tE, tF);
        r1.setJurySize(2);
        r2.setJurySize(2);
        r3.setJurySize(2);
        t.addRounds(r1, r2, r3);
        t.addJurors(jI1, jJ1, jK1, jL1, jM1); // chairs
        t.addJurors(jM2, jM3, jM4);
        assertThat(t.getStatistics().getOptimalChairLoad()).isEqualTo(2.0 / 5, offset(Double.MIN_VALUE));

        // jI1 is overloaded (100%), jJ1 and jK1 are within tolerance, jL1 and jM1 are unused (0%)
        //              1A        1B       2A        2B        3A       3B
        assignJurors(t, jI1, jM2, jJ1, jM3, jK1, jM4, jI1, jM3, jI1, jM2, jK1, jM4);
        checkSolutionFeasible(t);
        checkSolution(t, true, ScoringRule.chairLoadDeltaExceeded, 3);
        // TODO add absences
    }

    @Test
    public void testJurorAndJurorConflict() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF);
        r1.setJurySize(3);
        t.addRounds(r1);
        t.addJurors(jK1, jL1, jM1, jM2, jM3, jN1, jN2, jN3, jN4);

        assignJurors(t, jM1, jN1, jM2, jK1, jL1, jM3);
        checkSolution(t, true, ScoringRule.jurorAndJurorConflict, 1);
        assignJurors(t, jL1, jM2, jN4, jN1, jN2, jN3);
        checkSolution(t, true, ScoringRule.jurorAndJurorConflict, 3);
        assignJurors(t, jL1, jM2, jN4, jN1, jM3, jK1);
        t.addConflicts(
                new Conflict(jK1, jL1.getCountry()),
                new Conflict(jN1, jM3.getCountry())
        );
        checkSolution(t, true, ScoringRule.jurorAndJurorConflict, 1); // only jN1-jM3
    }

    @Test
    public void testZeroConflictJuror() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        r1.setJurySize(3);
        t.addRounds(r1);
        Juror j01 = new Juror("A", "1", null, JurorType.INDEPENDENT, true, true);
        Juror j02 = new Juror("B", "2", null, JurorType.INDEPENDENT, true, true);
        t.addJurors(j01, j02, jM1);

        assignJurors(t, j01, jM1, j02);
        checkSolution(t, true, ScoringRule.jurorAndJurorConflict, 0);
        checkSolution(t, true, ScoringRule.teamAndJurorSameCountry, 0);
    }

    @Test
    public void testIndependentBalance() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        r1.setJurySize(4);
        t.addRounds(r1);
        // 0.6 independent jurors
        t.addJurors(jI1, jI2, jI3, jI4, jI5, jI6, jT1, jT2, jT3, jT4);
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(2.4, offset(Double.MIN_VALUE));
        assignJurors(t, jI1, jT2, jI3, jT4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 0);
        assignJurors(t, jI1, jI2, jI3, jT4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 0);
        assignJurors(t, jI1, jI2, jI3, jI4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 1);
        assignJurors(t, jT1, jT2, jT3, jT4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 1);

        // two rounds and absences
        Round r2 = RoundFactory.createRound(2, tC, tB, tA);
        r2.setJurySize(4);
        t.addRounds(r2);
        t.addAbsences(new Absence(jT1, r1));
        t.addAbsences(new Absence(jI1, r2), new Absence(jI2, r2), new Absence(jI3, r2));
        assertThat(r1.getOptimalIndependentCount()).isEqualTo(2.7, offset(.05));
        assertThat(r2.getOptimalIndependentCount()).isEqualTo(1.7, offset(.05));
        assignJurors(t, jI1, jI2, jI3, jT4, jT1, jT2, jT3, jI4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 0);
        assignJurors(t, jI1, jI2, jI3, jI4, jT1, jT2, jT3, jT4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 2);
    }

    @Test
    public void testJurorMeetsBigGroupOften() {
        Tournament t = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF, tG);
        Round r2 = RoundFactory.createRound(2, tE, tF, tG, tH, tA, tB, tC);
        r1.setJurySize(2);
        r2.setJurySize(2);
        t.addRounds(r1, r2);
        t.addJurors(jI1, jI2, jM1, jM2);
        assignJurors(t, jI1, jI2, jM1, jM2, jM1, jI2, jI1, jM2);
        checkSolution(t, true, ScoringRule.jurorMeetsBigGroupOften, 1);
    }

    @Test
    public void testPenalizeChairChange() {
        Tournament tOld = new Tournament();
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        Round r2 = RoundFactory.createRound(2, tD, tE, tF);
        r1.setJurySize(1);
        r2.setJurySize(1);
        tOld.addRounds(r1, r2);
        tOld.addJurors(jK1, jL1, jM1, jN1);
        assignJurors(tOld, jK1, jL1);
        Tournament tNew = (Tournament) tOld.makeBackup();
        tNew.setOriginal(tOld);

        assignJurors(tNew, jK1, jL1);
        checkSolution(tNew, true, ScoringRule.penalizeChairChange, 0);

        assignJurors(tNew, jK1, jM1);
        checkSolution(tNew, true, ScoringRule.penalizeChairChange, 1);

        assignJurors(tNew, jL1, jK1);
        checkSolution(tNew, true, ScoringRule.penalizeChairChange, 2);

        assignJurors(tNew, jM1, jN1);
        checkSolution(tNew, true, ScoringRule.penalizeChairChange, 2);
    }

    @Test
    public void testPenalizeJurorChange() {
        Tournament tOld = new Tournament();
        Round r1 = RoundFactory.createRound(1, gABC, gDEF);
        r1.setJurySize(3);
        tOld.addRounds(r1);
        tOld.addJurors(jL1, jM1, jM2, jM3, jN1, jN2, jN3);
        assignJurors(tOld, jM1, jM2, jM3, jN1, jN2, jL1);
        Tournament tNew = (Tournament) tOld.makeBackup();
        tNew.setOriginal(tOld);

        // no change, no penalty
        assignJurors(tNew, jM1, jM2, jM3, jN1, jN2, jL1);
        checkSolution(tNew, true, ScoringRule.penalizeJurorChange, 0);

        // shuffling inside jury is not a change
        assignJurors(tNew, jM1, jM3, jM2, jL1, jN1, jN2);
        checkSolution(tNew, true, ScoringRule.penalizeJurorChange, 2 + 2);

        // chair changes are ignored (covered by other rule)
        assignJurors(tNew, jN1, jM2, jM3, jM1, jN2, jL1);
        checkSolution(tNew, true, ScoringRule.penalizeJurorChange, 0);

        // swap = 2 changes
        assignJurors(tNew, jM1, jM2, jL1, jN1, jN2, jM3);
        checkSolution(tNew, true, ScoringRule.penalizeJurorChange, 2);

        // single change
        assignJurors(tNew, jM1, jM2, jM3, jN1, jN2, jN3);
        checkSolution(tNew, true, ScoringRule.penalizeJurorChange, 1);

        // single change B3: jL1->jM1 (chairs ignored)
        assignJurors(tNew, jN1, jM2, jM3, jL1, jN2, jM1);
        checkSolution(tNew, true, ScoringRule.penalizeJurorChange, 1);
    }

    @Test
    public void testPenalizeJurorWithdraw() {
        Tournament tOld = new Tournament();
        Round r1 = RoundFactory.createRound(1, gABC, gDEF);
        Round r2 = RoundFactory.createRound(2, gADG, gBEH);
        r1.setJurySize(2);
        r2.setJurySize(2);
        tOld.addRounds(r1, r2);
        tOld.addJurors(jM1, jM2, jM3, jN1, jN2, jN3);
        assignJurors(tOld, jM1, jM2, jN1, jN2, jM1, jM2, jN1, jN2);
        Tournament tNew = (Tournament) tOld.makeBackup();
        tNew.setOriginal(tOld);

        // no change, no penalty
        assignJurors(tNew, jM1, jM2, jN1, jN2, jM1, jM2, jN1, jN2);
        checkSolution(tNew, true, ScoringRule.penalizeJurorWithdraw, 0);

        // shuffling inside rounds costs nothing
        assignJurors(tNew, jN1, jN2, jM1, jM2, jM1, jN2, jN1, jM2);
        checkSolution(tNew, true, ScoringRule.penalizeJurorWithdraw, 0);

        // jM2 has lost a seat
        assignJurors(tNew, jM1, jM2, jN1, jN2, jM1, jM3, jN1, jN2);
        checkSolution(tNew, true, ScoringRule.penalizeJurorWithdraw, 1);

        // jM1 and jN2 have lost one seat each
        assignJurors(tNew, jM1, jM3, jN1, jN3, jM1, jM2, jN1, jN2);
        checkSolution(tNew, true, ScoringRule.penalizeJurorWithdraw, 2);

        // jN2 has been withdrawn completely (2 seats)
        assignJurors(tNew, jM1, jM2, jN1, jM3, jM1, jM2, jN1, jM3);
        checkSolution(tNew, true, ScoringRule.penalizeJurorWithdraw, 2);
    }

    private void assignJurors(Tournament t, Juror... jurors) {
        int j = 0;
        for (Seat seat : t.getSeats()) {
            if (seat.isVoting()) {
                seat.setJuror(jurors[j++]);
            }
        }
    }

    private void checkSolutionFeasible(Tournament t) {
        ScoringResult result = calculateScore(t, createActivationListener(false));
        assertThat(result.getRuleActivations()).isEmpty();
        assertThat(result.getScore().isFeasible()).isTrue();
    }

    private void checkSolution(Tournament t, boolean feasibile, ScoringRule ruleFired, int fireCount) {
        checkSolution(t, feasibile, false, new RuleFiring(ruleFired, fireCount));
    }

    private void checkSolution(Tournament t, boolean feasibile, boolean strict, RuleFiring... firings) {
        ScoringResult result = calculateScore(t, createActivationListener(strict, firings));

        int total = 0;
        for (RuleFiring firing : firings) {
            assertThat(result.getFireCount(firing.rule.toString())).isEqualTo(firing.count);
            total += firing.count;
        }
        assertThat(result.getRuleActivations()).as("Total rules fired").hasSize(total);
        assertThat(result.getScore().isFeasible()).as("Score feasibility").isEqualTo(feasibile);
    }

    private ScoringResult calculateScore(Tournament t, ActivationListener activationListener) {
        // create ksession
        KieSession ksession = kieContainer.newKieSession();

        // insert all problem facts and planning entities from the solution
        for (Object o : t.getProblemFacts()) {
            ksession.insert(o);
        }
        for (Object o : t.getSeats()) {
            ksession.insert(o);
        }

        // set global score holder
        ksession.setGlobal(SCORE_HOLDER_NAME, new HardSoftScoreHolder(false));

        // register activation listener, ignoring the score accumulation rules
        ksession.addEventListener(activationListener);

        // fire the scoring rules
        ksession.fireAllRules();

        // return a ScoringResult that will be queried by the test
        ScoringResult scoringResult = new ScoringResult();
        scoringResult.setKnowledgeSession(ksession);
        scoringResult.setActivationListener(activationListener);
        return scoringResult;
    }

    private ActivationListener createActivationListener(boolean strict, RuleFiring... firings) {
        ActivationListener activationListener = new ActivationListener();
        // if strict, only ignore auxiliary (non-constraint) rules
        // if non-strict, also ignore soft constraints (still listen for hard constraints)
        for (ScoringRule rule : ScoringRule.values()) {
            if (rule.type == AUX || !strict && rule.type == SOFT) {
                activationListener.ignoreRule(rule.toString());
            }
        }
        // however, never ignore the tested rules
        for (RuleFiring firing : firings) {
            activationListener.unignoreRule(firing.rule.toString());
        }
        return activationListener;
    }

    /**
     * Listens for rule firings and allows to query what rules were fired.
     */
    private static class ActivationListener extends DefaultAgendaEventListener {

        private int totalFired = 0;
        private Map<String, Integer> firedRules = new LinkedHashMap<>();
        private List<RuleActivation> ruleActivations = new ArrayList<>();
        private List<String> ignoredRules = new ArrayList<>();

        public int getTotalFireCount() {
            return totalFired;
        }

        public int getFireCount(String ruleName) {
            return firedRules.containsKey(ruleName) ? firedRules.get(ruleName) : 0;
        }

        public List<RuleActivation> getRuleActivations() {
            return Collections.unmodifiableList(ruleActivations);
        }

        public void ignoreRule(String ruleName) {
            ignoredRules.add(ruleName);
        }

        public void unignoreRule(String ruleName) {
            ignoredRules.remove(ruleName);
        }

        @Override
        public void afterMatchFired(AfterMatchFiredEvent event) {
            super.afterMatchFired(event);
            Match match = event.getMatch();
            String ruleName = match.getRule().getName();
            if (ignoredRules.contains(ruleName)) {
                return; // ignore this rule
            }
            RuleActivation activation = new RuleActivation(ruleName, match.getObjects());
            ruleActivations.add(activation);
            Integer count = firedRules.get(ruleName);
            firedRules.put(ruleName, count == null ? 1 : ++count);
            totalFired++;
            LOG.debug("{}", activation);
        }
    }

    private static class RuleFiring {

        private ScoringRule rule;
        private int count;

        public RuleFiring(ScoringRule rule, int count) {
            this.rule = rule;
            this.count = count;
        }
    }

    private static class ScoringResult {

        private ActivationListener activationListener;
        private HardSoftScore score;

        public int getTotalFireCount() {
            return activationListener.getTotalFireCount();
        }

        public int getFireCount(String ruleName) {
            return activationListener.getFireCount(ruleName);
        }

        public List<RuleActivation> getRuleActivations() {
            return activationListener.getRuleActivations();
        }

        public HardSoftScore getScore() {
            return score;
        }

        public void setActivationListener(ActivationListener activationListener) {
            this.activationListener = activationListener;
        }

        public void setKnowledgeSession(KieSession ksession) {
            ScoreHolder holder = (ScoreHolder) ksession.getGlobal(SCORE_HOLDER_NAME);
            score = (HardSoftScore) holder.extractScore();
            LOG.debug(score.toString());
        }
    }

    private static class RuleActivation {

        private String ruleName;
        private List<Object> tuple;

        public RuleActivation(String ruleName, List<Object> tuple) {
            this.ruleName = ruleName;
            this.tuple = tuple;
        }

        @Override
        public String toString() {
            return String.format("[%s] activated by %s", ruleName, tuple);
        }
    }

    /**
     * There are three types of rules used to calculate score.
     */
    public enum RuleType {

        /**
         * This is a rule that inserts hard constraint occurrences.
         */
        HARD,
        /**
         * This is a rule that inserts soft constraint occurrences.
         */
        SOFT,
        /**
         * This is a rule that doesn't insert any constraint occurrences. This type covers rules that either insert logically
         * inferred facts or collects constraint occurrences to calculate the score.
         */
        AUX
    }

    /**
     * Enumeration of all rules used to calculate solution score. This helps to reference DRL rules from a Java test and makes
     * sure that all rules are covered.
     */
    public enum ScoringRule {

        // score calculation
        hardConstraintsBroken(AUX),
        softConstraintsBroken(AUX),
        // fact calculations
        calculateJurorLoads(AUX),
        calculateChairLoads(AUX),
        calculateIndependentRatio(AUX),
        // hard constraints
        emptySeat(HARD),
        multipleSeatsInRound(HARD),
        invalidChair(HARD),
        teamAndJurorSameCountry(HARD),
        teamAndChairMeetOften(HARD),
        absentJuror(HARD),
        experiencedJurorOnNonVotingVotingSeat(HARD),
        inexperiencedOnNonVotingVotingSeatNotInFirstRound(HARD),
        // soft constraints
        // * across rounds
        teamAndChairMeetTwice(SOFT, 200),
        teamAndJurorAlreadyMet(SOFT, 1),
        loadDeltaExceeded(SOFT, 100),
        chairLoadDeltaExceeded(SOFT, 150),
        jurorMeetsBigGroupOften(SOFT, 10),
        // * inside jury
        jurorAndJurorConflict(SOFT, 10),
        independentRatioDeltaExceeded(SOFT, 1),
        accumulatedBias(SOFT, 10),
        inexperiencedJurorVoting(SOFT, 500),
        inexperiencedJurorObserving(SOFT),
        // * change penalties
        penalizeChairChange(SOFT, 5),
        penalizeJurorChange(SOFT, 5),
        penalizeJurorWithdraw(SOFT, 5);
        private final RuleType type;
        private final int weight;

        private ScoringRule(RuleType type) {
            this.type = type;
            this.weight = 1;
        }

        private ScoringRule(RuleType type, int weight) {
            this.type = type;
            this.weight = weight;
        }
    }
}
