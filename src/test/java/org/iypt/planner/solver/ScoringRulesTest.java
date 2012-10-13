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
import org.iypt.planner.domain.Conflict;
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Juror;
import org.iypt.planner.domain.JurySeat;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.CSVTournamentFactory;
import org.iypt.planner.domain.util.RoundFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.iypt.planner.domain.util.SampleFacts.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

/**
 *
 * @author jlocker
 */
public class ScoringRulesTest {

    private static final Logger log = LoggerFactory.getLogger(ScoringRulesTest.class);
    private static final String SCORE_DRL = "org/iypt/planner/solver/score_rules.drl";
    private static final String SCORE_HOLDER_NAME = "scoreHolder";
    private static KnowledgeBase kbase;
    private List<ScoringRule> ignoredRules;

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
    }

    @Before
    public void resetIgnoredRules() {
        ignoredRules = new ArrayList<>();
        for (ScoringRule rule : ScoringRule.values()) {
            if (!rule.hard) {
                ignoredRules.add(rule);
            }
        }
    }

    @Test
    public void testIYPT2012() throws IOException {
        String path = "/org/iypt/planner/csv/";
        CSVTournamentFactory factory = new CSVTournamentFactory(
                path + "team_data.csv", path + "jury_data.csv", path + "schedule2012.csv");
        Tournament t = factory.newTournament();

        ignoredRules.remove(ScoringRule.loadDeltaExceeded);
        log.debug("Optimal load for IYPT2012: {}", t.getStatistics().getOptimalLoad());
        checkSolution(t, ScoringRule.loadDeltaExceeded, 18, true);
    }

    @Test
    public void testMultiSeat() {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addJurors(jD1);

        assignJurors(t, jD1, jD1);
        checkSolution(t, ScoringRule.multipleSeatsInRound, 2, false);
    }

    @Test
    public void testTeamAndJurorSameCountry() {
        Tournament t = new Tournament();
        t.setJuryCapacity(1);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addJurors(jA1, jD1);

        // simple country conflict
        assignJurors(t, jA1);
        checkSolution(t, ScoringRule.teamAndJurorSameCountry, 1, false);

        // add another juror with multiple conflicts
        t.setJuryCapacity(2);
        assignJurors(t, jA1, jD1);
        t.getConflicts().add(new Conflict(jD1, tB.getCountry()));
        t.getConflicts().add(new Conflict(jD1, tC.getCountry()));
        checkSolution(t, ScoringRule.teamAndJurorSameCountry, 3, false);
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
        checkSolution(t, ScoringRule.dayOff, 1, false);
    }

    @Test
    public void testInvalidChair() {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC, tD, tE, tF));
        t.addJurors(jA1, jA2, jD1, jD2);

        // invalid chair
        assignJurors(t, jD2, jD1);
        checkSolution(t, ScoringRule.invalidChair, 1, false);

        // two invalid chairs
        assignJurors(t, jD2, null, jA2);
        checkSolution(t, ScoringRule.invalidChair, 2, false);

        // one chair seat ok, one empty -> no invalid occupation
        assignJurors(t, jD1, null, null);
        checkSolutionFeasible(t);

        // both chair seats occupied
        assignJurors(t, jD1, null, jA1);
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
        checkSolution(t, ScoringRule.teamAndChairMeetOften, 1, false);

        assignJurors(t, jM1, jN1, jM1, jN1, jM1, jN1, jM1, jM3);
        // jN1 doesn't trigger the rule because he is not a chair (even though he's a chair candidate)
        checkSolution(t, ScoringRule.teamAndChairMeetOften, 1, false);
    }

    @Test
    public void testJurorBalance() {
        ignoredRules.remove(ScoringRule.loadDeltaExceeded);
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
        checkSolution(t, ScoringRule.loadDeltaExceeded, 3, true);
        // TODO add dayOffs
    }

    private void assignJurors(Tournament t, Juror... jurors) {
        Iterator<JurySeat> it = t.getJurySeats().iterator();
        for (int i = 0; i < jurors.length; i++) {
            it.next().setJuror(jurors[i]);
        }
    }

    private void checkSolutionFeasible(Tournament t) {
        ScoringResult result = calculateScore(t);
        assertThat(result.getRuleActivations(), is(Collections.EMPTY_LIST));
        assertThat(result.getScore().isFeasible(), is(true));
    }

    private void checkSolution(Tournament t, ScoringRule ruleFired, int fireCount, boolean feasibile) {
        ScoringResult result = calculateScore(t);
        assertThat(result.getFireCount(ruleFired.toString()), is(fireCount));
        assertThat(result.getTotalFireCount(), is(fireCount));
        assertThat(result.getScore().isFeasible(), is(feasibile));
    }

    private ScoringResult calculateScore(Tournament t) {
        // create ksession
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        // insert all problem facts and planning entities from the solution
        for (Object o : t.getProblemFacts()) {
            ksession.insert(o);
        }
        for (Object o : t.getJurySeats()) {
            ksession.insert(o);
        }

        // set global score holder
        ksession.setGlobal(SCORE_HOLDER_NAME, new HardAndSoftScoreHolder());

        // register activation listener, ignoring the score accumulation rules
        ActivationListener activationListener = new ActivationListener();
        for (ScoringRule rule : ignoredRules) {
            activationListener.ignoreRule(rule.toString());
        }
        ksession.addEventListener(activationListener);

        // fire the scoring rules
        ksession.fireAllRules();

        // return a ScoringResult that will be queried by the test
        ScoringResult scoringResult = new ScoringResult();
        scoringResult.setKnowledgeSession(ksession);
        scoringResult.setActivationListener(activationListener);
        return scoringResult;
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
            return ruleActivations;
        }

        public void ignoreRule(String ruleName) {
            ignoredRules.add(ruleName);
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

        hardConstraintsBroken(false),
        softConstraintsBroken(false),
        multipleSeatsInRound(true),
        teamAndJurorSameCountry(true),
        dayOff(true),
        invalidChair(true),
        teamAndChairMeetOften(true),
        calculateJurorLoads(false),
        loadDeltaExceeded(false);
        private boolean hard;

        private ScoringRule(boolean hard) {
            this.hard = hard;
        }
    }
}
