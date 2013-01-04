package org.iypt.planner.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScoreHolder;
import org.drools.planner.core.score.holder.ScoreHolder;
import org.drools.runtime.StatefulKnowledgeSession;
import org.iypt.planner.csv.CSVTournamentFactory;
import org.iypt.planner.domain.Conflict;
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.Lock;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Seat;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.RoundFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.iypt.planner.domain.util.SampleFacts.*;
import static org.junit.Assert.*;

/**
 *
 * @author jlocker
 */
public class ScoringRulesTest {

    private static final Logger log = LoggerFactory.getLogger(ScoringRulesTest.class);
    private static final String SCORE_DRL = "org/iypt/planner/solver/score_rules.drl";
    private static final String SCORE_HOLDER_NAME = "scoreHolder";
    private static KnowledgeBase kbase;
    private static WeightConfig wconfig;

    @BeforeClass
    public static void setUpClass() {

        // create ksession
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(SCORE_DRL), ResourceType.DRL);
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());
        kbase = kbuilder.newKnowledgeBase();

        // check the ScoreHolder global name
        KnowledgePackage pkg = kbase.getKnowledgePackages().iterator().next();
        assertEquals("Unexpected ScoreHolder global name", SCORE_HOLDER_NAME, pkg.getGlobalVariables().iterator().next().getName());

        // verify we are using the correct rule names (to detect typos and fail fast if a rule is renamed in DRL and not here)
        List<String> ruleNames = new ArrayList<>();
        for (Rule rule : pkg.getRules()) {
            ruleNames.add(rule.getName());
        }
        for (ScoringRule rule : ScoringRule.values()) {
            assertThat(ruleNames, hasItem(rule.toString()));
        }

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

    @Test
    public void testIYPT2012() throws IOException {
        CSVTournamentFactory factory = new CSVTournamentFactory();
        factory.readDataFromClasspath("/org/iypt/planner/csv/", "team_data.csv", "jury_data.csv", "schedule2012.csv");
        Tournament t = factory.newTournament();
        t.setWeightConfig(wconfig);

        log.debug("Optimal load for IYPT2012: {}", t.getStatistics().getOptimalLoad());
        checkSolution(t, true, true,
                new RuleFiring(ScoringRule.loadDeltaExceeded, 18),
                new RuleFiring(ScoringRule.teamAndJurorAlreadyMet, 110),
                new RuleFiring(ScoringRule.jurorAndJurorConflict, 16),
                new RuleFiring(ScoringRule.independentRatioDeltaExceeded, 2),
                new RuleFiring(ScoringRule.accumulatedBias, 45));
    }

    @Test
    public void testMultiSeat() {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addJurors(jD1);

        assignJurors(t, jD1, jD1);
        checkSolution(t, false, ScoringRule.multipleSeatsInRound, 2);
    }

    @Test
    public void testEmptySeat() {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addJurors(jD1);

        assignJurors(t, jD1, Juror.NULL);
        checkSolution(t, false, ScoringRule.emptySeat, 1);
    }

    @Test
    public void testTeamAndJurorSameCountry() {
        Tournament t = new Tournament();
        t.setJuryCapacity(1);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addJurors(jA1, jD1);

        // simple country conflict
        assignJurors(t, jA1);
        checkSolution(t, false, ScoringRule.teamAndJurorSameCountry, 1);

        // add another juror with multiple conflicts
        t.setJuryCapacity(2);
        assignJurors(t, jA1, jD1);
        t.getConflicts().add(new Conflict(jD1, tB.getCountry()));
        t.getConflicts().add(new Conflict(jD1, tC.getCountry()));
        checkSolution(t, false, ScoringRule.teamAndJurorSameCountry, 3);
    }

    @Test
    public void testDayOffRule() {
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        Tournament t = new Tournament();
        t.setJuryCapacity(1);
        t.addRounds(r1);
        t.addJurors(jD1);

        assignJurors(t, jD1);
        t.addDayOffs(new DayOff(jD1, r1.getDay()));
        checkSolution(t, false, ScoringRule.dayOff, 1);
    }

    @Test
    public void testBrokenLock() {
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(r1);
        t.addJurors(jD1, jE1, jF1);

        assignJurors(t, jD1, jE1);
        t.addLock(new Lock(jF1, t.getJuries().get(0), 0));
        t.addLock(new Lock(jE1, t.getJuries().get(0), 1));
        checkSolution(t, false, ScoringRule.brokenLock, 1);
    }

    @Test
    public void testInvalidChair() {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF));
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
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addRounds(RoundFactory.createRound(2, tA, tD, tE));
        t.addRounds(RoundFactory.createRound(3, tA, tB, tC));
        t.addRounds(RoundFactory.createRound(4, tA, tD, tE));
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
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tD));
        t.addRounds(RoundFactory.createRound(2, tA, tC, tE));
        t.addRounds(RoundFactory.createRound(3, tA, tB, tE));
        t.addRounds(RoundFactory.createRound(4, tA, tC, tD));
        t.addJurors(jM1, jM2, jM3, jN1);

        assignJurors(t, jM1, jM2, jM1, jM2, jN1, jM3, jN1, jM3);
        checkSolution(t, true, ScoringRule.teamAndChairMeetTwice, 2);
    }

    @Test
    public void testTeamAndJurorAlreadyMet() {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF));
        t.addRounds(RoundFactory.createRound(2, tA, tE, tF, tB, tC, tD));
        t.addJurors(jJ1, jK1, jL1, jM1, jM2, jM3);

        assignJurors(t, jJ1, jM2, jK1, jM3, jL1, jM3, jM1, jM2);
        checkSolution(t, true, ScoringRule.teamAndJurorAlreadyMet, 4);
    }

    @Test
    public void testJurorBalance() {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addRounds(RoundFactory.createRound(2, tA, tD, tE));
        t.addRounds(RoundFactory.createRound(3, tA, tB, tC));
        t.addRounds(RoundFactory.createRound(4, tA, tD, tE));
        t.addRounds(RoundFactory.createRound(5, tA, tB, tC));
        t.addJurors(jI1, jJ1, jK1, jL1, jM1); // chairs
        t.addJurors(jM2, jM3, jM4);
        assertEquals(2.0 / 8, t.getStatistics().getOptimalLoad(), Double.MIN_VALUE);
        assignJurors(t, jI1, jM2, jJ1, jM3, jK1, jM4, jL1, jM3, jM1, jM2); // ok
        checkSolutionFeasible(t);

        // jM2 is overloaded, jM3 and jM4 are unused
        assignJurors(t, jI1, jM2, jJ1, jM2, jK1, jM2, jL1, jM2, jM1, jM2);
        checkSolution(t, true, ScoringRule.loadDeltaExceeded, 3);
        // TODO add dayOffs
    }

    @Test
    public void testJurorAndJurorConflict() {
        Tournament t = new Tournament();
        t.setJuryCapacity(3);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF));
        t.addJurors(jK1, jL1, jM1, jM2, jM3, jN1, jN2, jN3, jN4);

        assignJurors(t, jM1, jN1, jM2, jK1, jL1, jM3);
        checkSolution(t, true, ScoringRule.jurorAndJurorConflict, 2);
        assignJurors(t, jL1, jM2, jN4, jN1, jN2, jN3);
        checkSolution(t, true, ScoringRule.jurorAndJurorConflict, 6);
        assignJurors(t, jL1, jM2, jN4, jN1, jM3, jK1);
        t.getConflicts().add(new Conflict(jK1, jL1.getCountry()));
        t.getConflicts().add(new Conflict(jN1, jM3.getCountry()));
        checkSolution(t, true, ScoringRule.jurorAndJurorConflict, 2); // only jN1-jM3
    }

    @Test
    public void testIndependentBalance() {
        Tournament t = new Tournament();
        t.setJuryCapacity(4);
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        t.addRounds(r1);
        // 0.6 independent jurors
        t.addJurors(jI1, jI2, jI3, jI4, jI5, jI6, jT1, jT2, jT3, jT4);
        assertEquals(2.4, r1.getOptimalIndependentCount(), Double.MIN_VALUE);
        assignJurors(t, jI1, jT2, jI3, jT4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 0);
        assignJurors(t, jI1, jI2, jI3, jT4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 0);
        assignJurors(t, jI1, jI2, jI3, jI4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 1);
        assignJurors(t, jT1, jT2, jT3, jT4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 1);

        // two rounds and dayOffs
        Round r2 = RoundFactory.createRound(2, tC, tB, tA);
        t.addRounds(r2);
        t.addDayOffs(new DayOff(jT1, r1.getDay()));
        t.addDayOffs(new DayOff(jI1, r2.getDay()), new DayOff(jI2, r2.getDay()), new DayOff(jI3, r2.getDay()));
        assertEquals(2.7, r1.getOptimalIndependentCount(), .05);
        assertEquals(1.7, r2.getOptimalIndependentCount(), .05);
        assignJurors(t, jI1, jI2, jI3, jT4, jT1, jT2, jT3, jI4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 0);
        assignJurors(t, jI1, jI2, jI3, jI4, jT1, jT2, jT3, jT4);
        checkSolution(t, true, ScoringRule.independentRatioDeltaExceeded, 2);
    }

    @Test
    public void testPenalizeChairChange() {
        Tournament tOld = new Tournament();
        tOld.setJuryCapacity(1);
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        Round r2 = RoundFactory.createRound(2, tD, tE, tF);
        tOld.addRounds(r1, r2);
        tOld.addJurors(jK1, jL1, jM1, jN1);
        assignJurors(tOld, jK1, jL1);
        Tournament tNew = (Tournament) tOld.cloneSolution();
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
    public void testPenalizeJurorWithdraw() {
        Tournament tOld = new Tournament();
        tOld.setJuryCapacity(2);
        Round r1 = RoundFactory.createRound(1, gABC, gDEF);
        Round r2 = RoundFactory.createRound(2, gADG, gBEH);
        tOld.addRounds(r1, r2);
        tOld.addJurors(jM1, jM2, jM3, jN1, jN2, jN3);
        assignJurors(tOld, jM1, jM2, jN1, jN2, jM1, jM2, jN1, jN2);
        Tournament tNew = (Tournament) tOld.cloneSolution();
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

    // TODO maybe add test for accumulatedBias rule
    private void assignJurors(Tournament t, Juror... jurors) {
        Iterator<Seat> it = t.getSeats().iterator();
        for (int i = 0; i < jurors.length; i++) {
            it.next().setJuror(jurors[i]);
        }
    }

    private void checkSolutionFeasible(Tournament t) {
        ScoringResult result = calculateScore(t, createActivationListener(false));
        assertThat(result.getRuleActivations(), is(Collections.EMPTY_LIST));
        assertThat(result.getScore().isFeasible(), is(true));
    }

    private void checkSolution(Tournament t, boolean feasibile, ScoringRule ruleFired, int fireCount) {
        checkSolution(t, feasibile, false, new RuleFiring(ruleFired, fireCount));
    }

    private void checkSolution(Tournament t, boolean feasibile, boolean strict, RuleFiring... firings) {
        ScoringResult result = calculateScore(t, createActivationListener(strict, firings));

        int total = 0;
        for (RuleFiring firing : firings) {
            assertThat(result.getFireCount(firing.rule.toString()), is(firing.count));
            total += firing.count;
        }
        assertThat(result.getTotalFireCount(), is(total));
        assertThat(result.getScore().isFeasible(), is(feasibile));
    }

    private ScoringResult calculateScore(Tournament t, ActivationListener activationListener) {
        // create ksession
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        // insert all problem facts and planning entities from the solution
        for (Object o : t.getProblemFacts()) {
            ksession.insert(o);
        }
        for (Object o : t.getSeats()) {
            ksession.insert(o);
        }

        // set global score holder
        ksession.setGlobal(SCORE_HOLDER_NAME, new HardAndSoftScoreHolder());

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
        if (strict) {
            // if strict, only ignore non-constraint rules
            activationListener.ignoreRule(ScoringRule.hardConstraintsBroken.toString());
            activationListener.ignoreRule(ScoringRule.softConstraintsBroken.toString());
            activationListener.ignoreRule(ScoringRule.calculateJurorLoads.toString());
            activationListener.ignoreRule(ScoringRule.calculateIndependentRatio.toString());
        } else {
            // if not strict, listen for hard constraint rules only
            for (ScoringRule rule : ScoringRule.values()) {
                if (!rule.hard) {
                    activationListener.ignoreRule(rule.toString());
                }
            }
        }
        // however, don't ignore the tested rules
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
        public void afterActivationFired(AfterActivationFiredEvent event) {
            super.afterActivationFired(event);
            String ruleName = event.getActivation().getRule().getName();
            if (ignoredRules.contains(ruleName)) {
                return; // ignore this rule
            }
            RuleActivation activation = new RuleActivation(ruleName, event.getActivation().getObjects());
            ruleActivations.add(activation);
            Integer count = firedRules.get(ruleName);
            firedRules.put(ruleName, count == null ? 1 : ++count);
            totalFired++;
            log.debug("{}", activation);
        }
    }

    private static class ScoringResult {

        private ActivationListener activationListener;
        private StatefulKnowledgeSession ksession;
        private HardAndSoftScore score;

        public int getTotalFireCount() {
            return activationListener.getTotalFireCount();
        }

        public int getFireCount(String ruleName) {
            return activationListener.getFireCount(ruleName);
        }

        public List<RuleActivation> getRuleActivations() {
            return activationListener.getRuleActivations();
        }

        public HardAndSoftScore getScore() {
            return score;
        }

        public void setActivationListener(ActivationListener activationListener) {
            this.activationListener = activationListener;
        }

        public void setKnowledgeSession(StatefulKnowledgeSession ksession) {
            this.ksession = ksession;
            ScoreHolder holder = (ScoreHolder) ksession.getGlobal(SCORE_HOLDER_NAME);
            score = (HardAndSoftScore) holder.extractScore();
            log.debug(score.toString());
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

    private enum ScoringRule {

        // score calculation
        hardConstraintsBroken(false),
        softConstraintsBroken(false),
        // hard constraints
        emptySeat(true),
        multipleSeatsInRound(true),
        invalidChair(true),
        teamAndJurorSameCountry(true),
        teamAndChairMeetOften(true),
        dayOff(true),
        brokenLock(true),
        // soft constraints
        teamAndChairMeetTwice(false, 200),
        teamAndJurorAlreadyMet(false, 1),
        calculateJurorLoads(false),
        loadDeltaExceeded(false, 100),
        jurorAndJurorConflict(false, 10),
        calculateIndependentRatio(false),
        independentRatioDeltaExceeded(false, 1),
        accumulatedBias(false, 10),
        penalizeChairChange(false, 133),
        penalizeJurorWithdraw(false, 111);
        private boolean hard;
        private int weight = 1;

        private ScoringRule(boolean hard) {
            this.hard = hard;
        }

        private ScoringRule(boolean hard, int weight) {
            this.hard = hard;
            this.weight = weight;
        }
    }

    private class RuleFiring {

        private ScoringRule rule;
        private int count;

        public RuleFiring(ScoringRule rule, int count) {
            this.rule = rule;
            this.count = count;
        }
    }
}
