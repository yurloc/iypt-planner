package org.iypt.planner.gui;

import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.skin.ContainerSkin;

public class RoundDetailsSkin extends ContainerSkin implements RoundDetailsListener {

    private TablePane content;
    @BXML private Label optimalIndependentLabel;
    @BXML private Label maxJurySizeLabel;
    @BXML private Label idleLabel;
    @BXML private Label awayLabel;
    @BXML private TableView idleTableView;
    @BXML private TableView awayTableView;

    @Override
    public void install(Component component) {
        super.install(component);

        // get component and register skin as a listener
        final RoundDetails details = (RoundDetails) component;
        details.getListeners().add(this);

        // read BXML
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (TablePane) bxmlSerializer.readObject(RoundDetailsSkin.class, "round_details.bxml");
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // add it to container
        details.add(content);

        // initialize fields with elements from BXML
        bxmlSerializer.bind(this, RoundDetailsSkin.class);

        // register listeners
        idleTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                details.seatSelected((SeatInfo) tableView.getSelectedRow());
            }
        });
        idleTableView.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (component.isFocused()) {
                    details.seatSelected((SeatInfo) ((TableView) component).getSelectedRow());
                }
            }
        });
        awayTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                details.seatSelected((SeatInfo) tableView.getSelectedRow());
            }
        });
        awayTableView.getComponentStateListeners().add(new ComponentStateListener.Adapter() {
            @Override
            public void focusedChanged(Component component, Component obverseComponent) {
                if (component.isFocused()) {
                    details.seatSelected((SeatInfo) ((TableView) component).getSelectedRow());
                }
            }
        });

        roundChanged();
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
    public void roundChanged() {
        RoundDetails details = (RoundDetails) getComponent();
        optimalIndependentLabel.setText(details.getOptimalIndependentCount());
        maxJurySizeLabel.setText(details.getMaxJurySize());
        List<SeatInfo> idle = new ListAdapter<>(details.getIdle());
        List<SeatInfo> away = new ListAdapter<>(details.getAway());
        idleLabel.setText(String.format("Idle (%d)", idle.getLength()));
        awayLabel.setText(String.format("Away (%d)", away.getLength()));
        idleTableView.setTableData(idle);
        awayTableView.setTableData(away);
    }

    @Override
    public void seatSelected(SeatInfo seatInfo) {
        // do nothing
    }
}
