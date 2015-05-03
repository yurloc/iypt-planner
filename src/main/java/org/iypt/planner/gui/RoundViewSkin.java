package org.iypt.planner.gui;

import java.util.List;
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
    private GroupRoster[] views;

    @Override
    public void install(Component component) {
        super.install(component);

        // get component and register skin as a listener
        RoundView round = (RoundView) component;
        round.getRoundViewListeners().add(this);

        // create content and add it to component
        content = new BoxPane(Orientation.HORIZONTAL);
        round.add(content);

        // initialize group views
        List<Group> groups = round.getRound().getGroups();
        views = new GroupRoster[groups.size()];
        for (int i = 0; i < views.length; i++) {
            GroupRoster view = new GroupRoster(round.getSchedule(), groups.get(i));
            views[i] = view;
            content.add(view);
        }

        // TODO register group listeners
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
        List<Group> groups = round.getRound().getGroups();
        for (int i = 0; i < views.length; i++) {
            views[i].update(groups.get(i));
        }
    }
}
