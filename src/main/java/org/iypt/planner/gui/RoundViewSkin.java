package org.iypt.planner.gui;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.skin.ContainerSkin;
import org.iypt.planner.domain.Group;

/**
 *
 * @author jlocker
 */
public class RoundViewSkin extends ContainerSkin implements RoundViewListener {

    private BoxPane content;

    @Override
    public void install(Component component) {
        super.install(component);
        RoundView round = (RoundView) component;
        round.getRoundViewListeners().add(this);
        content = new BoxPane(Orientation.HORIZONTAL);
        round.add(content);
        scheduleChanged(round);
    }

    @Override
    public int getPreferredWidth(int height) {
        return content.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return content.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return content.getPreferredSize();
    }

    @Override
    public void layout() {
        content.setLocation(0, 0);
        content.setSize(getWidth(), getHeight());
    }

    @Override
    public void scheduleChanged(RoundView round) {
        content.removeAll();
        for (Group group : round.getRound().getGroups()) {
            content.add(new GroupRoster(round.getSchedule(), group));
        }
    }
}
