package org.iypt.planner.gui;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.iypt.planner.Constants;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;

/**
 *
 * @author jlocker
 */
public class Constraint {

    private String name;
    private String type;
    private String weight;
    private int intWeight;
    private List<String> causes;
    private boolean hard;

    public Constraint() {
    }

    public Constraint(ConstraintMatch cm) {
        name = cm.getConstraintName();
        hard = cm.getScoreLevel() < 1;
        type = hard ? Constants.CONSTRAINT_TYPE_HARD : Constants.CONSTRAINT_TYPE_SOFT;
        intWeight = cm.getWeightAsNumber().intValue();
        weight = Integer.toString(intWeight);
        causes = new ArrayList<>();
        for (Object cause : cm.getJustificationList()) {
            causes.add(cause.toString());
        }
    }

    public int getIntWeight() {
        return intWeight;
    }

    public void setIntWeight(int intWeight) {
        this.intWeight = intWeight;
        weight = Integer.toString(intWeight);
    }

    public String getWeight() {
        return weight;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCauses() {
        return causes;
    }

    public boolean isHard() {
        return hard;
    }
}
