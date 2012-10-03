package org.iypt.planner.gui;

import java.io.IOException;
import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Label;
import org.iypt.planner.domain.Group;
import org.iypt.planner.domain.Round;
import org.iypt.planner.domain.Tournament;

/**
 *
 * @author jlocker
 */
public class RoundView extends BoxPane implements Bindable {

    // UI
    @BXML private Round round;
    @BXML private Label roundLabel;
    @BXML private BoxPane roundPane;

    // model?
    @BXML private Tournament tournament;
    private List<GroupView> groupList = new ArrayList<>();

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        roundLabel.setText("Round #" + round.getNumber());
        roundLabel.setTooltipText("Day " + round.getDay());
        roundLabel.setTooltipDelay(300);

        for (Group group : round.getGroups()) {
            BXMLSerializer bxmlSerializer = new BXMLSerializer();
            bxmlSerializer.getNamespace().put("group", group);
            bxmlSerializer.getNamespace().put("tournament", tournament);
            try {
                GroupView view = (GroupView) bxmlSerializer.readObject(RoundView.class, "group.bxml");
                groupList.add(view); //redundant?
                roundPane.add(view);
            } catch (IOException | SerializationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void update(Round r) {
        // TODO update all groups
    }

}
