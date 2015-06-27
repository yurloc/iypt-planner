package org.iypt.planner.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.iypt.planner.solver.TournamentSolver;
import org.iypt.planner.solver.WeightConfig;
import org.optaplanner.core.impl.score.constraint.ConstraintOccurrence;
import org.optaplanner.core.impl.score.constraint.ConstraintType;

/**
 * This is a controller for constraint configuration model. It defines some beans ({@link Constraint}) to shield the skin from
 * the actual model's API and to simplify updating the model with user input. Besides that it just extends {@link Container}
 * and sets the custom skin that is able to display the model data.
 *
 * @see Container#setSkin(org.apache.pivot.wtk.Skin)
 * @author jlocker
 */
public class ConstraintsConfig extends Container {

    protected final class Constraint {

        private final String name;
        private final boolean hard;

        public Constraint(String name, boolean hard) {
            this.name = name;
            this.hard = hard;
        }

        public String getName() {
            return name;
        }

        public boolean isHard() {
            return hard;
        }

        public void setWeight(int weight) {
            weightConfig.setWeight(getName(), weight);
        }

        public int getWeight() {
            return weightConfig.getWeight(getName());
        }
    }

    private static class ConstraintsConfigListenerList extends ListenerList<ConstraintsConfigListener>
            implements ConstraintsConfigListener {

        @Override
        public void constraintsChanged(ConstraintsConfig config) {
            for (ConstraintsConfigListener listener : this) {
                listener.constraintsChanged(config);
            }
        }
    }
    private final ConstraintsConfigListenerList constraintsConfigListeners = new ConstraintsConfigListenerList();
    private WeightConfig weightConfig;
    private List<Constraint> constraints = new ArrayList<>();

    public ConstraintsConfig() {
        setSkin(new ConstraintsConfigSkin());
    }

    public void setSolver(TournamentSolver solver) {
        weightConfig = solver.getWeightConfig();
        constraints = new ArrayList<>();
        for (ConstraintOccurrence co : solver.getConstraints()) {
            constraints.add(new Constraint(co.getRuleId(), co.getConstraintType() == ConstraintType.HARD));
        }
        constraintsConfigListeners.constraintsChanged(this);
    }

    protected List<Constraint> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    public ListenerList<ConstraintsConfigListener> getConstraintsConfigListeners() {
        return constraintsConfigListeners;
    }
}
