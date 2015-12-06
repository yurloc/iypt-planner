package org.iypt.planner.api.io.bias;

public class Juror {

    private final String firstName;
    private final String lastName;
    private final float bias;
    private final boolean juror;

    public Juror(String firstName, String lastName, float bias, boolean juror) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.bias = bias;
        this.juror = juror;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public float getBias() {
        return bias;
    }

    public boolean isJuror() {
        return juror;
    }
}
