package org.iypt.planner.api.io.bias;

import java.io.Serializable;
import java.util.Comparator;

public class BiasComparator implements Comparator<Juror>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Juror o1, Juror o2) {
        int compare = Float.compare(o1.getBias(), o2.getBias());
        if (compare != 0) {
            return compare;
        }
        return o1.getLastName().compareTo(o2.getLastName());
    }
}
