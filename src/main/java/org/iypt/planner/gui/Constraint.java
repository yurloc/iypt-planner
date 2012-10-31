package org.iypt.planner.gui;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.drools.planner.core.score.constraint.UnweightedConstraintOccurrence;

/**
 *
 * @author jlocker
 */
public class Constraint {

    private String name;
    private String type;
    private String weight;
    private List<String> causes;

    public Constraint() {
    }

    public Constraint(ConstraintOccurrence co) {
        this.name = co.getRuleId();
        this.type = co.getConstraintType().toString().replaceFirst("NEGATIVE_", "-");
        if (co instanceof UnweightedConstraintOccurrence) {
            this.weight = "";
        } else if (co instanceof IntConstraintOccurrence) {
            this.weight = Integer.toString(((IntConstraintOccurrence) co).getWeight());
        } else {
            throw new UnsupportedOperationException("Constraint type (" + co.getClass() + ") not supported yet.");
        }
        causes = new ArrayList<>();
        for (Object cause : co.getCauses()) {
            causes.add(cause.toString());
        }
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
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
}
