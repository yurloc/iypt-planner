package org.iypt.planner.api.io.bias;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public interface BiasWriter {

    void write(Writer writer, Collection<Juror> jurors) throws IOException;
}
