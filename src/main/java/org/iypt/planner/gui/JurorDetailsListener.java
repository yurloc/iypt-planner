package org.iypt.planner.gui;

interface JurorDetailsListener {

    void jurorChanged();

    void jurorAssignmentChanged();

    void jurorChangesSaved(JurorDetails details);

    void jurorAssignmentSelected(JurorAssignment assignment);

    public class Adapter implements JurorDetailsListener {

        @Override
        public void jurorChanged() {
            // do nothing
        }

        @Override
        public void jurorAssignmentChanged() {
            // do nothing
        }

        @Override
        public void jurorChangesSaved(JurorDetails details) {
            // do nothing
        }

        @Override
        public void jurorAssignmentSelected(JurorAssignment assignment) {
            // do nothing
        }
    }
}
