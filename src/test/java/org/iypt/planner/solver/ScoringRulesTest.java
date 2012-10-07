package org.iypt.planner.solver;

import java.util.ArrayList;
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
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.JurySeat;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.RoundFactory;
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
    private static final String RULE_HARD = "hardConstraintsBroken";
    private static final String RULE_SOFT = "softConstraintsBroken";
    private static final String RULE_multipleSeatsInRound = "multipleSeatsInRound";
    private static final String RULE_teamAndJurorSameCountry = "teamAndJurorSameCountry";
    private static final String RULE_dayOff = "dayOff";
    private static int ACTIVATION_LISTENER_VERBOSITY = ActivationListener.VERBOSITY_RULES;
    private static KnowledgeBase kbase;

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
        assertThat(ruleNames, hasItem(RULE_HARD));
        assertThat(ruleNames, hasItem(RULE_SOFT));
        assertThat(ruleNames, hasItem(RULE_multipleSeatsInRound));
    }

    @Test
    public void testMultiSeat() {
        Tournament t = new Tournament();
        t.setJuryCapacity(2);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addJurors(jD1);
        for (JurySeat s : t.getJurySeats()) {
            s.setJuror(jD1);
        }

        checkSolution(t, RULE_multipleSeatsInRound, 2, false);
    }

    @Test
    public void testTeamAndJurorSameCountry() {
        Tournament t = new Tournament();
        t.setJuryCapacity(1);
        t.addRounds(RoundFactory.createRound(1, tA, tB, tC));
        t.addJurors(jA1);
        for (JurySeat s : t.getJurySeats()) {
            s.setJuror(jA1);
        }

        checkSolution(t, RULE_teamAndJurorSameCountry, 1, false);
    }

    @Test
    public void testDayOffRule() {
        Round r1 = RoundFactory.createRound(1, tA, tB, tC);
        Tournament t = new Tournament();
        t.setJuryCapacity(1);
        t.addRounds(r1);
        t.addJurors(jD1);
        t.getJurySeats().iterator().next().setJuror(jD1);
        t.addDayOffs(new DayOff(jD1, r1.getDay()));

        checkSolution(t, RULE_dayOff, 1, false);
    }

    private void checkSolution(Tournament t, String ruleFired, int fireCount, boolean feasibile) {
        ScoringResult result = calculateScore(t);
        assertThat(result.getFireCount(ruleFired), is(fireCount));
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
        activationListener.ignoreRule(RULE_HARD);
        activationListener.ignoreRule(RULE_SOFT);
        activationListener.setVerbosity(ACTIVATION_LISTENER_VERBOSITY);
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

        public static final int VERBOSITY_RULES = 1;
        public static final int VERBOSITY_TUPLES = 2;
        private int verbosity = VERBOSITY_RULES;
        private int totalFired = 0;
        private Map<String, Integer> firedRules = new LinkedHashMap<>();
        private List<String> ignoredRules = new ArrayList<>();

        public int getTotalFireCount() {
            return totalFired;
        }

        public int getFireCount(String ruleName) {
            return firedRules.containsKey(ruleName) ? firedRules.get(ruleName) : 0;
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
            Integer count = firedRules.get(ruleName);
            firedRules.put(ruleName, count == null ? 1 : ++count);
            totalFired++;
            if (verbosity >= VERBOSITY_RULES) {
                log.debug("Rule fired: {} ({})", ruleName, firedRules.get(ruleName));
            }
            if (verbosity >= VERBOSITY_TUPLES) {
                log.debug("Activated by tuple: {}", event.getActivation().getObjects());
            }
        }

        public int getVerbosity() {
            return verbosity;
        }

        public void setVerbosity(int verbosity) {
            this.verbosity = verbosity;
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
}
