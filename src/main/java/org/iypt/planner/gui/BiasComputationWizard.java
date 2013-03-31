package org.iypt.planner.gui;

import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.TextInput;

/**
 *
 * @author jlocker
 */
public class BiasComputationWizard extends Sheet implements Bindable {

    @BXML private PushButton browseButton;
    @BXML private PushButton selectButton;
    @BXML private PushButton unselectButton;
    @BXML private PushButton cancelButton;
    @BXML private PushButton okButton;
    @BXML private ListView selectedListView;
    @BXML private ListView unselectedListView;
    @BXML private TextInput fileTextInput;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                BiasComputationWizard.this.close();
            }
        });
    }

}
