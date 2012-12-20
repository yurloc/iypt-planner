package org.iypt.planner.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.constraint.ConstraintType;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.iypt.planner.solver.TournamentSolver;
import org.iypt.planner.solver.WeightConfig;

/**
 * This is a controller for constraint configuration model. It defines some beans ({@link Constraint}) to shield the skin from
 * the actual model's API and to simplify updating the model with user input. Besides that it just extends {@link Container}
 * and sets the custom skin that is able to display the model data.
 *
 * @see Container#setSkin(org.apache.pivot.wtk.Skin)
 * @author jlocker
 */
public class ConstraintsConfig extends Container {

    public WeightConfig getWeightConfig() {
        return weightConfig;
    }

    protected final class Constraint {

        private ConstraintOccurrence co;

        public Constraint(String name, ConstraintType type) {
            co = new IntConstraintOccurrence(name, type);
        }

        private Constraint(ConstraintOccurrence co) {
            this.co = co;
        }

        public String getName() {
            return co.getRuleId();
        }

        public int getWeight() {
            return weightConfig.getWeight(getName());
        }

        public ConstraintType getType() {
            return co.getConstraintType();
        }

        public void setWeight(int weight) {
            weightConfig.setWeight(getName(), weight);
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
    private TournamentSolver solver;
    private WeightConfig weightConfig;
    private List<Constraint> constraints = new ArrayList<>();
    private ConstraintsConfigListenerList constraintsConfigListeners = new ConstraintsConfigListenerList();

    public ConstraintsConfig() {
        setSkin(new ConstraintsConfigSkin());
    }

    public ConstraintsConfig(WeightConfig weightConfig) {
        this.weightConfig = weightConfig;
        setSkin(new ConstraintsConfigSkin());
    }

    public void setSolver(TournamentSolver solver) {
        this.solver = solver;
        this.weightConfig = solver.getWeightConfig();
        addConstraints(solver.getConstraints());
    }

    public void setWconfig(WeightConfig weightConfig) {
        this.weightConfig = weightConfig;
    }

    public void addConstraints(ConstraintOccurrence... constraints) {
        addConstraints(Arrays.asList(constraints));
    }

    public void addConstraints(Collection<ConstraintOccurrence> constraints) {
        for (ConstraintOccurrence co : constraints) {
            this.constraints.add(new Constraint(co));
        }
        constraintsConfigListeners.constraintsChanged(this);
    }

    protected List<Constraint> getConstraints() {
        return constraints;
    }

    public ListenerList<ConstraintsConfigListener> getConstraintsConfigListeners() {
        return constraintsConfigListeners;
    }
}
