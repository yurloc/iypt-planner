package org.iypt.planner.gui;

import java.util.Arrays;
import java.util.Collection;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;
import org.drools.definition.rule.Rule;
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

        private String name;
        private int weight;

        public Constraint(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            weightConfig.setWeight(name, weight);
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
    private WeightConfig weightConfig;
    private HashSet<Constraint> constraints = new HashSet<>();
    private ConstraintsConfigListenerList constraintsConfigListeners = new ConstraintsConfigListenerList();

    public ConstraintsConfig() {
        setSkin(new ConstraintsConfigSkin());
    }

    public ConstraintsConfig(WeightConfig weightConfig) {
        this.weightConfig = weightConfig;
        setSkin(new ConstraintsConfigSkin());
    }

    public void setWconfig(WeightConfig weightConfig) {
        this.weightConfig = weightConfig;
    }

    public void addConstraints(String... constraints) {
        addConstraints(Arrays.asList(constraints));
    }

    public void addConstraints(Collection<String> constraints) {
        for (String constraint : constraints) {
            this.constraints.add(new Constraint(constraint, weightConfig.getWeight(constraint)));
        }
        constraintsConfigListeners.constraintsChanged(this);
    }

    public void addConstraintsFromRules(Collection<Rule> rules) {
        for (Rule rule : rules) {
            addConstraints(rule.getName());
        }
    }

    protected HashSet<Constraint> getConstraints() {
        return constraints;
    }

    public ListenerList<ConstraintsConfigListener> getConstraintsConfigListeners() {
        return constraintsConfigListeners;
    }
}
