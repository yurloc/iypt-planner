package org.iypt.core;

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
import org.iypt.domain.Country;
import org.iypt.domain.Group;
import org.iypt.domain.Juror;
import org.iypt.domain.JuryMembership;
import org.iypt.domain.Round;
import org.iypt.domain.Team;
import org.iypt.domain.Tournament;
import org.iypt.domain.util.DefaultTournamentFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

/**
 *
 * @author jlocker
 */
public class ScoringRulesTest {

    // teams
    private Team tA = new Team(Country.A);
    private Team tB = new Team(Country.B);
    private Team tC = new Team(Country.C);
    private Team tD = new Team(Country.D);
    private Team tE = new Team(Country.E);
    private Team tF = new Team(Country.F);
    private Team tG = new Team(Country.G);
    private Team tH = new Team(Country.H);
    private Team tI = new Team(Country.I);
    // jurors
    private Juror jA1 = new Juror(Country.A);
    private Juror jA2 = new Juror(Country.A);
    private Juror jA3 = new Juror(Country.A);
    private Juror jA4 = new Juror(Country.A);
    private Juror jA5 = new Juror(Country.A);
    private Juror jB1 = new Juror(Country.B);
    private Juror jB2 = new Juror(Country.B);
    private Juror jB3 = new Juror(Country.B);
    private Juror jB4 = new Juror(Country.B);
    private Juror jC1 = new Juror(Country.C);
    private Juror jC2 = new Juror(Country.C);
    private Juror jC3 = new Juror(Country.C);
    private Juror jD1 = new Juror(Country.D);
    private Juror jD2 = new Juror(Country.D);
    private Juror jE1 = new Juror(Country.E);
    private Juror jF1 = new Juror(Country.F);
    private Juror jG1 = new Juror(Country.G);
    private Juror jH1 = new Juror(Country.H);
    private Juror jI1 = new Juror(Country.I);
    private Juror jJ1 = new Juror(Country.J);
    private Juror jK1 = new Juror(Country.K);
    private Juror jL1 = new Juror(Country.L);
    // group permutation #1
    private Group gABC = new Group(tA, tB, tC);
    private Group gDEF = new Group(tD, tE, tF);
    private Group gGHI = new Group(tG, tH, tI);
    // group permutation #2
    private Group gADG = new Group(tA, tD, tG);
    private Group gBEH = new Group(tB, tE, tH);
    private Group gCFI = new Group(tC, tF, tI);
    // group permutation #3
    private Group gAEI = new Group(tA, tE, tI);
    private Group gBFG = new Group(tB, tF, tG);
    private Group gCDH = new Group(tC, tD, tH);
    // group permutation #4
    private Group gAFH = new Group(tA, tF, tH);
    private Group gBDI = new Group(tB, tD, tI);
    private Group gCEG = new Group(tC, tE, tG);
    // rounds
    private Round round1 = new Round(1, 1);
    private Round round2 = new Round(2, 2);
    private Round round3 = new Round(3, 3);
    private Round round4 = new Round(4, 4);
    private Round round5 = new Round(5, 5);
    
    private static final Logger log = LoggerFactory.getLogger(ScoringRulesTest.class);
    private static final String SCORE_DRL = "org/iypt/core/score_rules.drl";
    private static final String SCORE_HOLDER_NAME = "scoreHolder";
    private static final String RULE_HARD = "hardConstraintsBroken";
    private static final String RULE_SOFT = "softConstraintsBroken";
    private static final String RULE_multipleMembershipsInRound = "multipleMembershipsInRound";
    private static final String RULE_teamAndJurorSameCountry = "teamAndJurorSameCountry";
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
        List<String> ruleNames = new ArrayList<String>();
        for (Rule rule : pkg.getRules()) {
            ruleNames.add(rule.getName());
        }
        assertThat(ruleNames, hasItem(RULE_HARD));
        assertThat(ruleNames, hasItem(RULE_SOFT));
        assertThat(ruleNames, hasItem(RULE_multipleMembershipsInRound));
    }

    @Test
    public void testMultiMembership() {
        DefaultTournamentFactory f = new DefaultTournamentFactory();
        f.setJuryCapacity(2);
        f.createRound(1, tA, tB, tC);
        f.addJurors(jD1);
        Tournament t = f.newTournament();
        for (JuryMembership m : t.getJuryMemberships()) {
            m.setJuror(jD1);
        }
        
        ScoringResult result = calculateScore(t);
        assertThat(result.getFireCount(RULE_multipleMembershipsInRound), is(2));
        assertThat(result.getTotalFireCount(), is(2));
        assertFalse(result.getScore().isFeasible());
    }
    
    @Test
    public void testTeamAndJurorSameCountry() {
        DefaultTournamentFactory f = new DefaultTournamentFactory();
        f.setJuryCapacity(1);
        f.createRound(1, tA, tB, tC);
        f.addJurors(jA1);
        Tournament t = f.newTournament();
        for (JuryMembership m : t.getJuryMemberships()) {
            m.setJuror(jA1);
        }
        
        ScoringResult result = calculateScore(t);
        assertThat(result.getFireCount(RULE_teamAndJurorSameCountry), is(1));
        assertThat(result.getTotalFireCount(), is(1));
        assertFalse(result.getScore().isFeasible());
    }
    
    private ScoringResult calculateScore(Tournament t) {
        // create ksession
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        
        // insert all problem facts and planning entities from the solution
        for (Object o : t.getProblemFacts()) {
            ksession.insert(o);
        }
        for (Object o : t.getJuryMemberships()) {
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
        private Map<String, Integer> firedRules = new LinkedHashMap<String, Integer>();
        private List<String> ignoredRules = new ArrayList<String>();
        
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
            
            if (ignoredRules.contains(ruleName)) return; // ignore this rule
            
            Integer count = firedRules.get(ruleName);
            firedRules.put(ruleName, count == null ? 1 : ++count);
            totalFired++;
            if (verbosity >= VERBOSITY_RULES) log.debug("Rule fired: {} ({})", ruleName, firedRules.get(ruleName));
            if (verbosity >= VERBOSITY_TUPLES) log.debug("Activated by tuple: {}", event.getActivation().getObjects());
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
