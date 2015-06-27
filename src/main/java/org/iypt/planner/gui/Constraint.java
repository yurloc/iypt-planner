package org.iypt.planner.gui;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.optaplanner.core.impl.score.constraint.ConstraintOccurrence;
import org.optaplanner.core.impl.score.constraint.ConstraintType;
import org.optaplanner.core.impl.score.constraint.IntConstraintOccurrence;
import org.optaplanner.core.impl.score.constraint.UnweightedConstraintOccurrence;

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

    public Constraint(ConstraintOccurrence co) {
        name = co.getRuleId();
        hard = co.getConstraintType() == ConstraintType.HARD;
        type = co.getConstraintType().toString();
        if (co instanceof UnweightedConstraintOccurrence) {
            intWeight = 0;
            weight = "";
        } else if (co instanceof IntConstraintOccurrence) {
            intWeight = ((IntConstraintOccurrence) co).getWeight();
            weight = Integer.toString(intWeight);
        } else {
            throw new UnsupportedOperationException("Constraint type (" + co.getClass() + ") not supported yet.");
        }
        causes = new ArrayList<>();
        for (Object cause : co.getCauses()) {
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
