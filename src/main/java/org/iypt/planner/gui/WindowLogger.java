package org.iypt.planner.gui;

import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Window;
import org.slf4j.Logger;

import static java.lang.String.format;

/**
 *
 * @author jlocker
 */
public class WindowLogger {

    private final Logger delegate;
    private final Window window;

    public WindowLogger(Logger delegate, Window window) {
        this.delegate = delegate;
        this.window = window;
    }

    public void error(String string, Throwable thrwbl) {
        delegate.error(string, thrwbl);
        Alert.alert(MessageType.ERROR, format("%s: %s", string, thrwbl), window);
    }

    void info(String string) {
        delegate.info(string);
        Alert.alert(MessageType.INFO, string, window);
    }
}
