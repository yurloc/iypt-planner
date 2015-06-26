package org.iypt.planner.gui.swap;

public class SwapQueue {

    private SwapArgument source;
    private SwapArgument target;

    public void add(SwapArgument arg) {
        if (source == null) {
            source = arg;
        } else if (!source.getJuror().equals(arg.getJuror())) {
            target = source;
            source = arg;
        }
    }

    public SwapArgument getSource() {
        return source;
    }

    public SwapArgument getTarget() {
        return target;
    }

    public boolean isReady() {
        return source != null && target != null;
    }

    public void execute() {
        target.apply(source.getJuror());
        source.apply(target.getJuror());
        clear();
    }

    public void clear() {
        source = null;
        target = null;
    }
}
