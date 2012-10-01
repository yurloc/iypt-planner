package org.iypt.planner.gui;

import java.io.IOException;
import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.phase.event.SolverPhaseLifecycleListenerAdapter;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.iypt.planner.domain.DayOff;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;
import org.iypt.planner.domain.util.DefaultTournamentFactory;

import static org.iypt.planner.domain.util.SampleFacts.*;
/**
 *
 * @author jlocker
 */
public class PlannerWindow extends Window implements Bindable {

    private Label scoreLabel;
    private PushButton nextButton;
    private PushButton terminateButton;
    @BXML private TablePane roundHolder;
    private Tournament tournament;
    private SolverTask solverTask;
    private List<RoundView> roundViews = new ArrayList<RoundView>();

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        scoreLabel = (Label) namespace.get("scoreLabel");
        nextButton = (PushButton) namespace.get("nextButton");
        terminateButton = (PushButton) namespace.get("terminateButton");
        tournament = getInitialSolution();

        initRounds();

        terminateButton.setEnabled(false);
//        for (SolverPhase phase : app.getPhases()) {
//            phase.addSolverPhaseLifecycleListener(new );
//        }

        nextButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
//                Alert.alert(MessageType.INFO, "You clicked me!", PlannerWindow.this);
                button.setEnabled(false);
                terminateButton.setEnabled(true);
                scoreLabel.setText("calculating...");
                solverTask = new SolverTask();
                TaskListener<String> taskListener = new TaskListener<String>() {
                    @Override
                    public void taskExecuted(Task<String> task) {
//                        activityIndicator.setActive(false);
                        nextButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        scoreLabel.setText(task.getResult());
                    }

                    @Override
                    public void executeFailed(Task<String> task) {
//                        activityIndicator.setActive(false);
                        nextButton.setEnabled(true);
                        terminateButton.setEnabled(false);
                        scoreLabel.setText(task.getFault().toString());
                    }
                };
                solverTask.execute(new TaskAdapter<String>(taskListener));
            }
        });

        terminateButton.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                solverTask.terminate();
            }
        });
    }

    Tournament getInitialSolution() {
        DefaultTournamentFactory factory = new DefaultTournamentFactory();
        factory.setJuryCapacity(6);
        Round r1 = factory.createRound(1, gABC, gDEF, gGHI);
        Round r2 = factory.createRound(2, gADG, gBEH, gCFI);
        Round r3 = factory.createRound(3, gAFH, gBDI, gCEG);
        Tournament t = factory.newTournament();

        // TODO should be handled by the Factory?
        t.addJurors(jA1, jA2, jA3, jA4, jA5, jA6);
        t.addJurors(jB1, jB2, jB3, jB4);
        t.addJurors(jC1, jC2, jC3, jC4);
        t.addJurors(jD1, jD2, jE1, jF1, jG1);
        t.addDayOffs(new DayOff(jE1, r1.getDay()), new DayOff(jE1, r3.getDay()));

        return t;
    }

    private void initRounds() {
        for (Round round : tournament.getRounds()) {
            BXMLSerializer bxmlSerializer = new BXMLSerializer();
            bxmlSerializer.getNamespace().put("round", round);
            bxmlSerializer.getNamespace().put("tournament", tournament);
            try {
                RoundView roundView = ((RoundView) bxmlSerializer.readObject(PlannerWindow.class, "round.bxml"));
                //            Rollup rollup = new Rollup(true);
                //            rollup.setHeading(new Label("Rollup #" + round.getNumber()));
                //            rollup.getHeading().setTooltipText("Day " + round.getDay());
                            TablePane.Row row = new TablePane.Row();
                            row.add(roundView);
                            roundHolder.getRows().add(row);
                            roundViews.add(roundView);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (SerializationException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    private class SolverListener implements SolverEventListener {

        public void bestSolutionChanged(BestSolutionChangedEvent event) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private class PhaseListener extends SolverPhaseLifecycleListenerAdapter {

        @Override
        public void stepTaken(AbstractStepScope stepScope) {
            super.stepTaken(stepScope);
        }

    }

    private class SolverTask extends Task<String> {

//        private org.iypt.core.App app;

        public void terminate() {
//            app.getSolver().terminateEarly();
        }

        @Override
        public String execute() throws TaskExecutionException {
//            app = new org.iypt.core.App();
//            app.setTournament(tournament);
//            Tournament solved = app.solve();
//            return solved.getScore().toString();
            return null;
        }

    }
}
