package org.iypt.planner.api.io;

import java.io.IOException;
import java.io.Writer;
import org.iypt.planner.api.domain.Schedule;

public interface ScheduleExporter {

    void write(Writer writer, Schedule schedule) throws IOException;
}
