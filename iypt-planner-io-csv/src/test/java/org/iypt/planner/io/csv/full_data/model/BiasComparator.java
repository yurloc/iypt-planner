package org.iypt.planner.io.csv.full_data.model;

import java.io.Serializable;
import java.util.Comparator;

public class BiasComparator implements Comparator<Juror>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Juror o1, Juror o2) {
        int compare = Float.compare(o1.getAverageBias(), o2.getAverageBias());
        if (compare != 0) {
            return compare;
        }
        return o1.getLastName().compareTo(o2.getLastName());
    }
}
