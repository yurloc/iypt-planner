package org.iypt.planner.solver;

/**
 *
 * @author jlocker
 */
public interface WeightConfig {

    int getWeight(String ruleId);
    void setWeight(String ruleId, int weight);
}
