package org.iypt.planner.api.pdf;

public interface PdfExporter {

    void export(ExportRequest request) throws ExportException;
}
