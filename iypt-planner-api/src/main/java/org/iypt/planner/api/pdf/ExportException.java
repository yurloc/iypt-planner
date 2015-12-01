package org.iypt.planner.api.pdf;

public class ExportException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>ExportException</code> without detail message.
     */
    public ExportException() {
    }

    /**
     * Constructs an instance of <code>ExportException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ExportException(String msg) {
        super(msg);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportException(Throwable cause) {
        super(cause);
    }
}
