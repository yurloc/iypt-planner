package org.iypt.planner.gui;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;

public class PlannerApplication extends Application.Adapter {

    private PlannerWindow window = null;

    public static void main(String[] args) {
        DesktopApplicationContext.main(PlannerApplication.class, args);
    }

    @Override
    public void startup(Display display, Map<String, String> properties)
            throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (PlannerWindow) bxmlSerializer.readObject(PlannerApplication.class, "planner.bxml");
        window.open(display);
        window.initializeSolver();
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }
}
