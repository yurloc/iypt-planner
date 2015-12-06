package org.iypt.planner.api.constraints;

public class ConstraintRule {

    private final String name;
    private final String type;

    public ConstraintRule(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
