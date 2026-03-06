package ph.com.guanzongroup.tcs.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Maynard
 */
public class ModelServiceBayController implements Initializable {

    @FXML
    private AnchorPane mainAnchor;
    @FXML
    private Label lblPit;
    @FXML
    private Label lblTimer;
    @FXML
    private Label lblCustomer;
    @FXML
    private Label lblMCModel;
    private boolean isVisible = false;

    /**
     * Initializes the controller class.
     */
    public AnchorPane getMainAnchor() {
        return mainAnchor;
    }

    public void setVisible(boolean fbVisible) {
        mainAnchor.setVisible(fbVisible);
    }

    public String getPitNo() {
        return lblPit.getText();
    }

    public void setPitNo(String fsPitNo) {
        lblPit.setText(fsPitNo);
    }

    public void setVisiblePitNo(boolean fbPitNo) {
        lblPit.setVisible(fbPitNo);
    }

    public String getTimer() {
        return lblTimer.getText();
    }

    public void setTimer(String fsTimer) {
        lblTimer.setText(fsTimer);
    }

    public void setVisibleTimer(boolean fbTimer) {
        lblTimer.setVisible(fbTimer);
    }

    public String getCustomer() {
        return lblCustomer.getText();
    }

    public void setCustomer(String fsCustomer) {
        lblCustomer.setText(fsCustomer);
    }

    public void setVisibleCustomer(boolean fbCustomer) {
        lblCustomer.setVisible(fbCustomer);
    }

    public String getMCModel() {
        return lblMCModel.getText();
    }

    public void setMCModel(String fsMCModel) {
        lblMCModel.setText(fsMCModel);
    }

    public void setVisibleMCModel(boolean fbMCModel) {
        lblMCModel.setVisible(fbMCModel);
    }

    public void setVisibleOther(boolean fbVisible) {
        lblMCModel.setVisible(fbVisible);
        lblCustomer.setVisible(fbVisible);
        lblPit.setVisible(fbVisible);
        isVisible = fbVisible;
    }

    public boolean IsVisible() {
        return isVisible;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
