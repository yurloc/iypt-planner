package org.iypt.planner.api.domain;

import java.util.HashMap;
import java.util.Map;

public class BiasData {

    private final Map<Juror, Double> biases = new HashMap<>();

    public void setBias(Juror juror, double bias) {
        biases.put(juror, bias);
    }

    public double getBias(Juror juror) {
        return biases.get(juror);
    }
}
