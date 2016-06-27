package org.iypt.planner.domain;

import java.util.Objects;

/**
 *
 * @author jlocker
 */
public final class Jury {

    private Group group;

    protected String coords() {
        return group.coords();
    }

    @Override
    public String toString() {
        return String.format("Jury %s", coords());
    }

    /**
     * Get the group this jury is assigned to.
     *
     * @return the group this jury is assigned to
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Set the group this jury is assigned to.
     *
     * @param group the group this jury is assigned to
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.group);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Jury other = (Jury) obj;
        if (!Objects.equals(this.group, other.group)) {
            return false;
        }
        return true;
    }

}
