package org.iypt.planner.gui;

public enum InputFile {
    TEAMS("teams", ".*team.*\\.csv"),
    JURORS("jurors", ".*jur(or|ydat).*\\.csv"),
    BIASES("biases", ".*bias.*\\.csv"),
    SCHEDULE("schedule", ".*schedule.*\\.csv");

    private String name;
    private String matchExpression;

    private InputFile(String name, String keySubstring) {
        this.name = name;
        this.matchExpression = keySubstring;
    }

    public String getName() {
        return name;
    }

    public boolean matches(String fileName) {
        return fileName.matches(matchExpression);
    }
}
