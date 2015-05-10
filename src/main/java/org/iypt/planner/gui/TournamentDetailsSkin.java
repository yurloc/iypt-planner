package org.iypt.planner.gui;

import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.skin.ContainerSkin;

public class TournamentDetailsSkin extends ContainerSkin implements TournamentDetailsListener {

    private TablePane content;
    @BXML private Label totalJurorsLabel;
    @BXML private Spinner juryCapacitySpinner;
    @BXML private Label totalSeatsLabel;
    @BXML private Label totalMandaysLabel;
    @BXML private Label optimalLoadLabel;

    @Override
    public void install(Component component) {
        super.install(component);

        // get component and register skin as a listener
        final TournamentDetails details = (TournamentDetails) component;
        details.getListeners().add(this);

        // read BXML
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (TablePane) bxmlSerializer.readObject(TournamentDetailsSkin.class, "tournament_details.bxml");
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // add it to container
        details.add(content);

        // initialize fields with elements from BXML
        bxmlSerializer.bind(this, TournamentDetailsSkin.class);

        // register listeners
        juryCapacitySpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
                details.setCapacity((Integer) spinner.getSelectedItem());
            }
        });

        tournamentChanged();
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
    public void capacityChanged(int capacity) {
        // do nothing
    }

    @Override
    public void tournamentChanged() {
        TournamentDetails details = (TournamentDetails) getComponent();
        totalJurorsLabel.setText(details.getTotalJurors());
        juryCapacitySpinner.setSelectedItem(details.getJuryCapacity());
        totalSeatsLabel.setText(details.getTotalSeats());
        totalMandaysLabel.setText(details.getTotalMandays());
        optimalLoadLabel.setText(details.getOptimalLoad());
    }

    @Override
    public void enabledStateChanged() {
        juryCapacitySpinner.setEnabled(getComponent().isEnabled());
    }
}
