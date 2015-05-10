package org.iypt.planner.gui;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Container;

/**
 *
 * @author jlocker
 */
public class JurorDetails extends Container {

    private static class JurorDetailsListenerList extends ListenerList<JurorDetailsListener> implements JurorDetailsListener {

        @Override
        public void jurorChanged() {
            for (JurorDetailsListener listener : this) {
                listener.jurorChanged();
            }
        }

        @Override
        public void jurorAssignmentChanged() {
            for (JurorDetailsListener listener : this) {
                listener.jurorAssignmentChanged();
            }
        }

        @Override
        public void jurorChangesSaved(JurorDetails details) {
            for (JurorDetailsListener listener : this) {
                listener.jurorChangesSaved(details);
            }
        }
    }

    private final JurorDetailsListenerList detailsListeners = new JurorDetailsListenerList();
    private JurorInfo jurorInfo;

    public JurorDetails() {
        setSkin(new JurorDetailsSkin());
    }

    public ListenerList<JurorDetailsListener> getDetailsListeners() {
        return detailsListeners;
    }

    public JurorInfo getJurorInfo() {
        return jurorInfo;
    }

    public void showJuror(JurorInfo jurorInfo) {
        this.jurorInfo = jurorInfo;
        detailsListeners.jurorChanged();
    }

    void changeAssignment(JurorAssignment assignment, int statusId) {
        JurorAssignment.Status newStatus = JurorAssignment.Status.values()[statusId];
        assignment.change(newStatus);
        detailsListeners.jurorAssignmentChanged();
    }

    void saveChanges() {
        detailsListeners.jurorChangesSaved(this);
    }

    void revertSchedule() {
        for (JurorAssignment assignment : jurorInfo.getSchedule()) {
            assignment.reset();
        }
        detailsListeners.jurorChanged();
    }
}
