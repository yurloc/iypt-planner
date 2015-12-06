package org.iypt.planner.api.constraints;

/**
 *
 * @author jlocker
 */
public interface WeightConfig {

    int getWeight(String ruleId);

    void setWeight(String ruleId, int weight);
}
