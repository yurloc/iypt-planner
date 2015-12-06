package org.iypt.planner.opta.drools.solver;

import java.util.HashMap;
import java.util.Map;
import org.iypt.planner.api.constraints.WeightConfig;

/**
 *
 * @author jlocker
 */
public class DefaultWeightConfig implements WeightConfig {

    public DefaultWeightConfig() {
        weights = new HashMap<>();
    }
    private final Map<String, Integer> weights;

    @Override
    public void setWeight(String ruleId, int weight) {
        weights.put(ruleId, weight);
    }

    @Override
    public int getWeight(String ruleId) {
        if (!weights.containsKey(ruleId)) {
            return 1;
        }
        return weights.get(ruleId);
    }
}
