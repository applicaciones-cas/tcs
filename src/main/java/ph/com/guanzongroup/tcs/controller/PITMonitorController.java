package ph.com.guanzongroup.tcs.controller;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRider;
import ph.com.guanzongroup.cas.joborder.base.JobOrder;
import static ph.com.guanzongroup.tcs.App.pxeFolderView;

/**
 * FXML Controller class
 *
 * @author Valencia, Maynard
 */
public class PITMonitorController implements Initializable, PITMonitorListener {

    private GRider oApp;
    private JobOrder oTrans;

    private int pnRow = 0;
    private boolean pbLoaded = false;
    private Date pdPeriod = null;

    @FXML
    private AnchorPane mainAnchor, acMainServicePanel;

    @FXML
    private AnchorPane acPit;

    @FXML
    private VBox vbServiceBay;

    @FXML
    private GridPane gpServiceBayList;

    @FXML
    private TableView<JobOrderModel> tblQueue;
    @FXML
    private TableView<JobOrderModel> tblFinish;

    @FXML
    private TableColumn tblColQueNo, tblColQueCustomer, tblColQueModel,
            tblColFnhNo, tblColFnhCustomer, tblColFnhModel;

    private final ObservableList<JobOrderModel> JOQueueList = FXCollections.observableArrayList();
    private final ObservableList<JobOrderModel> JOFinishList = FXCollections.observableArrayList();
    private ObservableList<JobOrderModel> JOList = FXCollections.observableArrayList();

    private final ObservableList<ModelServiceBayController> ctrlServiceBay = FXCollections.observableArrayList();
    private final ObservableList<ModelServiceBayController> ctrlMainService = FXCollections.observableArrayList();
    private Timeline pitSyncClock;
    private Timeline mainServiceRotateClock;
    private int currentMainServiceIndex = 0;
    private Timeline flashTimeline;
    private Timeline serviceReload;
    private boolean pbIsFlashing = false;

    private Stage getStage() {
        return (Stage) mainAnchor.getScene().getWindow();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initTableGrid();

        Platform.runLater(() -> {
            loadRecord();

            initMainServicePanel();
            initServicePitPanel();
            loadServicePit();
            startPitSyncClock();
            startMainServiceRotation();

        });
        pbLoaded = true;

    }

    @Override
    public void onJobOrderChanged(String fsTransNox, String fsAction) {
        Platform.runLater(() -> {

            flashMainServiceNotification(fsTransNox, fsAction);
            loadRecord();       // refresh queue & finish tables
            loadServicePit();   // refresh pit panels

        });
    }

    @Override
    public void onJobOrderChanged(int fnRow) {

    }

    public void setGRider(GRider foValue) {
        oApp = foValue;
    }

    public void setJobOrder(JobOrder foJobOrder) {
        oTrans = foJobOrder;
    }

    public void setJobOrderList(ObservableList<JobOrderModel> foJobOrder) {
        JOList = foJobOrder;
    }

    public void initTableGrid() {

        tblColQueNo.setStyle("-fx-alignment: CENTER;");
        tblColQueCustomer.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        tblColQueModel.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");

        tblColQueNo.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index01"));
        tblColQueCustomer.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index02"));
        tblColQueModel.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index03"));


        /*making column's position uninterchangebale*/
        tblQueue.widthProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) {
                TableHeaderRow header = (TableHeaderRow) tblQueue.lookup("TableHeaderRow");
                header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        header.setReordering(false);
                    }
                });
            }
        });

        tblQueue.setItems(JOQueueList);

        tblColFnhNo.setStyle("-fx-alignment: CENTER;");
        tblColFnhCustomer.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        tblColFnhModel.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");

        tblColFnhNo.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index01"));
        tblColFnhCustomer.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index02"));
        tblColFnhModel.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index03"));

        /*making column's position uninterchangebale*/
        tblFinish.widthProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) {
                TableHeaderRow header = (TableHeaderRow) tblFinish.lookup("TableHeaderRow");
                header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        header.setReordering(false);
                    }
                });
            }
        });
        tblFinish.setItems(JOFinishList);

    }

    private void initMainServicePanel() {

        try {
            FXMLLoader fxLoader;
            fxLoader = new FXMLLoader(getClass().getResource(pxeFolderView + "/child/PitMonitorServiceBay.fxml"));
            fxLoader.load();
            ModelServiceBayController loController = fxLoader.getController();

            AnchorPane modelPane = loController.getMainAnchor();

            loController.setVisibleOther(false);
            loController.setTimer("READY");

            ctrlMainService.add(loController);
            acMainServicePanel.getChildren().add(modelPane);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initServicePitPanel() {
        try {
            if (oTrans.getJOServiceStatus() == null) {
                return;
            }
            double lnTotalPit = Double.parseDouble(oTrans.getJOServiceStatus("sTotalPitx").toString());
            int lnTotalCol = 1;
            if (lnTotalPit > 4) {
                lnTotalCol = 2;
            }
            int columns = (lnTotalPit <= 4) ? 1 : 2;
            for (int lnCtr = 0; lnCtr < lnTotalPit; lnCtr++) {
                int lnRow = lnCtr / columns;
                int lnCol = lnCtr % columns;

                try {

                    FXMLLoader fxLoader = new FXMLLoader(
                            getClass().getResource(
                                    pxeFolderView + "/child/PitMonitorServiceBayChild.fxml"));
                    fxLoader.load();

                    ModelServiceBayController loController
                            = fxLoader.getController();
                    AnchorPane modelPane = loController.getMainAnchor();

                    loController.setVisibleOther(false);
                    loController.setTimer("READY");

                    ctrlServiceBay.add(loController);

                    gpServiceBayList.add(modelPane, lnCol, lnRow);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(PITMonitorController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private int decimalHourToMinutes(double val) {
        return (int) Math.round(val * 60);
    }

    private void loadRecord() {
        if (serviceReload != null) {
            serviceReload.stop();
        }
        try {
            JOQueueList.clear();

            if (oTrans.RetrieveJobOrderListQueue()) {
                System.err.println("Start Adding Job Order Queue");
                System.err.println("Loop count = " + oTrans.getJobOrderQueueCount());
                for (int lnRow = 1; lnRow <= oTrans.getJobOrderQueueCount(); lnRow++) {
                    JOQueueList.add(new JobOrderModel(String.valueOf(lnRow),
                            (String) oTrans.getJobOrderQueue(lnRow, "xClientNm"),
                            (String) oTrans.getJobOrderQueue(lnRow, "sModelNme"), ""));
                }

                tblQueue.getSelectionModel().selectFirst();
            }

            JOFinishList.clear();
            if (oTrans.RetrieveJobOrderListFinish()) {
                System.err.println("Start Adding Job Order Finish");
                System.err.println("Loop count = " + oTrans.getJobOrderFinishCount());

                for (int lnRow = 1; lnRow <= oTrans.getJobOrderFinishCount(); lnRow++) {
                    JOFinishList.add(new JobOrderModel(String.valueOf(lnRow),
                            (String) oTrans.getJobOrderFinish(lnRow, "xClientNm"),
                            (String) oTrans.getJobOrderFinish(lnRow, "sModelNme"), ""));
                }

                tblFinish.getSelectionModel().selectFirst();
            }

            tblQueue.getSelectionModel().selectFirst();

        } catch (NullPointerException | SQLException e) {
            Platform.runLater(() -> ShowMessageFX.Warning(getStage(), e.getMessage(), "Warning", null));
            Logger.getLogger(PITMonitorController.class.getName()).log(Level.SEVERE, null, e);

        }
    }

    private void loadServicePit() {
        for (ModelServiceBayController ctrl : ctrlServiceBay) {
            ctrl.setCustomer("");
            ctrl.setMCModel("");
            ctrl.setPitNo("");
            ctrl.setTimer("READY");
            ctrl.setVisibleOther(false);
        }
        try {
            System.err.println("Start Adding Job Order to Service PIT");
            System.err.println("Loop count = " + oTrans.getJobOrderCount());

            double lnTotalPit = Double.parseDouble(oTrans.getJOServiceStatus("sTotalPitx").toString());
            double lnOnGoingPit = Double.parseDouble(oTrans.getJOServiceStatus("sOnGoingx").toString());

            if (lnOnGoingPit > 0) {
                for (int lnRowJO = 0; lnRowJO < oTrans.getJobOrderCount(); lnRowJO++) {
                    if (JOList.get(lnRowJO).getIndex08().isEmpty()) {
                        continue;
                    }

                    for (int lnRow = 0; lnRow < lnTotalPit; lnRow++) {
                        //03 CustomerName
                        //04 MCModel
                        //09 PitNumber
                        //11 Pit Counter
                        if (!ctrlServiceBay.get(lnRow).getPitNo().isEmpty()) {
                            continue;
                        }
                        ctrlServiceBay.get(lnRow).setCustomer(JOList.get(lnRowJO).getIndex03());
                        ctrlServiceBay.get(lnRow).setMCModel(JOList.get(lnRowJO).getIndex04());
                        if (JOList.get(lnRowJO).getIndex10().equals("1")) {

                            ctrlServiceBay.get(lnRow).setPitNo("PIT " + JOList.get(lnRowJO).getIndex09() + " (ON-HOLD)");
                        } else {
                            ctrlServiceBay.get(lnRow).setPitNo("PIT " + JOList.get(lnRowJO).getIndex09());
                        }
                        double lnRemainingCounter
                                = Double.parseDouble(JOList.get(lnRowJO).getIndex11());
                        if (lnRemainingCounter > 0) {
                            ctrlServiceBay.get(lnRow).setTimer(formatHoursToHHMMSS(lnRemainingCounter));
                            ctrlServiceBay.get(lnRow).setVisibleOther(true);

                        } else {
                            ctrlServiceBay.get(lnRow).setTimer("CHECKING");
                            ctrlServiceBay.get(lnRow).setVisibleOther(true);
                        }
                        break;
                    }

                }
            }

        } catch (NullPointerException | SQLException e) {
            Platform.runLater(() -> ShowMessageFX.Warning(getStage(), e.getMessage(), "Warning", null));
            Logger.getLogger(TCSDashBoardController.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private String formatHoursToHHMMSS(double hours) {

        int totalSeconds = (int) (hours * 3600);

        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private void startPitSyncClock() {
        if (pitSyncClock != null) {
            pitSyncClock.stop();
        }
        pitSyncClock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    loadServicePit();
                    syncMainServiceWithActivePit();
                }
                )
        );
        pitSyncClock.setCycleCount(Animation.INDEFINITE);
        pitSyncClock.play();
    }

    public void stopPitSyncClock() {
        if (pitSyncClock != null) {
            pitSyncClock.stop();
        }
    }

    private void startMainServiceRotation() {
        if (mainServiceRotateClock != null) {
            mainServiceRotateClock.stop();
        }

        mainServiceRotateClock = new Timeline(
                new KeyFrame(Duration.minutes(3), e -> rotateMainServicePanel())
        );
        mainServiceRotateClock.setCycleCount(Animation.INDEFINITE);
        mainServiceRotateClock.play();

        // Show first active pit immediately on start
        rotateMainServicePanel();
    }

    private void rotateMainServicePanel() {
        // Collect only active (filled) pit controllers
        List<ModelServiceBayController> activePits = new ArrayList<>();
        for (ModelServiceBayController ctrl : ctrlServiceBay) {
            if (ctrl.IsVisible()) { // only filled pits
                activePits.add(ctrl);
            }
        }

        ModelServiceBayController mainCtrl = ctrlMainService.get(0);

        // No active pits — show idle
        if (activePits.isEmpty()) {
            fadeTransition(mainCtrl, () -> {
                mainCtrl.setCustomer("");
                mainCtrl.setMCModel("");
                mainCtrl.setPitNo("");
                mainCtrl.setTimer("READY");
                mainCtrl.setVisibleOther(false);
            });
            currentMainServiceIndex = 0;
            return;
        }

        // Wrap index if needed
        if (currentMainServiceIndex >= activePits.size()) {
            currentMainServiceIndex = 0;
        }

        ModelServiceBayController source = activePits.get(currentMainServiceIndex);

        // Copy data from source pit to main panel with fade
        fadeTransition(mainCtrl, () -> {
            mainCtrl.setCustomer(source.getCustomer());
            mainCtrl.setMCModel(source.getMCModel());
            mainCtrl.setPitNo(source.getPitNo());
            mainCtrl.setTimer(source.getTimer());
            mainCtrl.setVisibleOther(true);
        });

        currentMainServiceIndex++;
    }

    private void fadeTransition(ModelServiceBayController ctrl, Runnable onSwitch) {
        AnchorPane pane = ctrl.getMainAnchor();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), pane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), pane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(e -> {
            onSwitch.run(); // swap data while invisible
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void syncMainServiceWithActivePit() {
        if (ctrlMainService.isEmpty()) {
            return;
        }

        ModelServiceBayController mainCtrl = ctrlMainService.get(0);
        String currentPit = mainCtrl.getPitNo();

        // Collect currently active pits
        List<ModelServiceBayController> activePits = new ArrayList<>();
        for (ModelServiceBayController ctrl : ctrlServiceBay) {
            if (ctrl.IsVisible()) {
                activePits.add(ctrl);
            }
        }

        // No active pits at all — show READY
        if (activePits.isEmpty()) {
            mainCtrl.setCustomer("");
            mainCtrl.setMCModel("");
            mainCtrl.setPitNo("");
            mainCtrl.setTimer("READY");
            mainCtrl.setVisibleOther(false);
            currentMainServiceIndex = 0;
            return;
        }

        // Check if current displayed pit is still active
        boolean currentPitStillActive = false;
        for (ModelServiceBayController ctrl : activePits) {
            if (ctrl.getPitNo().trim().equals(currentPit != null ? currentPit.trim() : "")) {
                currentPitStillActive = true;
                // Sync timer only — no need to rotate
                mainCtrl.setTimer(ctrl.getTimer());
                break;
            }
        }

        // Current pit finished or mainService is empty — rotate immediately
        if (!currentPitStillActive) {
            if (currentMainServiceIndex >= activePits.size()) {
                currentMainServiceIndex = 0;
            }

            ModelServiceBayController source = activePits.get(currentMainServiceIndex);
            mainCtrl.setCustomer(source.getCustomer());
            mainCtrl.setMCModel(source.getMCModel());
            mainCtrl.setPitNo(source.getPitNo());
            mainCtrl.setTimer(source.getTimer());
            mainCtrl.setVisibleOther(true);

            currentMainServiceIndex++;

            // Reset the 3-min rotation clock so it counts from this switch
            restartMainServiceRotationClock();
        }
    }

    private void restartMainServiceRotationClock() {
        if (mainServiceRotateClock != null) {
            mainServiceRotateClock.stop();
        }
        mainServiceRotateClock = new Timeline(
                new KeyFrame(Duration.minutes(2), e -> rotateMainServicePanel())
        );
        mainServiceRotateClock.setCycleCount(Animation.INDEFINITE);
        mainServiceRotateClock.play();
    }

    private void flashMainServiceNotification(String fsTransNox, String fsAction) {
        if (ctrlMainService.isEmpty()) {
            return;
        }

        // Find matching JO — snapshot data BEFORE loadRecord() clears it
        JobOrderModel targetJO = null;
        for (JobOrderModel jo : JOList) {
            if (jo.getIndex02().equals(fsTransNox)) {
                targetJO = jo;
                break;
            }
        }

        String lsCustomer = targetJO != null ? targetJO.getIndex03() : "";
        String lsMCModel = targetJO != null ? targetJO.getIndex04() : "";
        String lsPitNo = targetJO != null ? targetJO.getIndex09() : "";

        String lsCounter;
        double lnRemainingCounter
                = Double.parseDouble(targetJO.getIndex11());
        if (lnRemainingCounter > 0) {
            lsCounter = (formatHoursToHHMMSS(lnRemainingCounter));
        } else {
            lsCounter = "CHECKING";
        }
        String lsPitCounter = targetJO != null ? targetJO.getIndex11() : "";

        String lsStatusLabel;
        switch (fsAction) {
            case "STARTED":
                lsStatusLabel = "▶ STARTED";
                break;
            case "PAUSED":
                lsStatusLabel = "⏸ ON HOLD";
                break;
            case "RESUMED":
                lsStatusLabel = "▶ RESUMED";
                break;
            case "FINISHED":
                lsStatusLabel = "RELEASING";
                lsPitNo = "";
                break;
            default:
                lsStatusLabel = fsAction;
                break;
        }

        ModelServiceBayController mainCtrl = ctrlMainService.get(0);
        AnchorPane pane = mainCtrl.getMainAnchor();

        // Stop any previous flash cleanly BEFORE starting new one
        stopFlash();
        if (mainServiceRotateClock != null) {
            mainServiceRotateClock.stop();
        }
        if (pitSyncClock != null) {
            pitSyncClock.stop(); // ← also stop sync to prevent overwrite during flash
        }
        pbIsFlashing = true;

        // Set content once before flash starts
        mainCtrl.setCustomer(lsCustomer);
        mainCtrl.setMCModel(lsMCModel);
        mainCtrl.setPitNo(!lsPitNo.isEmpty() ? "PIT " + lsPitNo + "  |  " + lsStatusLabel : "");
        mainCtrl.setTimer(fsAction.equals("FINISHED") ? lsStatusLabel : lsCounter);
        mainCtrl.setVisibleOther(true);
        pane.setOpacity(1.0);

        // 6 flashes = alternating dim/bright, 400ms each
        final int maxFlashes = 10;
        final int[] flashCount = {0};

        flashTimeline = new Timeline(
                new KeyFrame(Duration.millis(600), e -> {
                    flashCount[0]++;
                    pane.setOpacity(flashCount[0] % 2 == 0 ? 1.0 : 0.15);
                    //Keep Refreshing Service pit
                    loadServicePit();
                    playNotificationSound();

                    // Re-fetch live JO from shared JOList every second
                    JobOrderModel liveJO = null;
                    for (JobOrderModel jo : JOList) {
                        if (jo.getIndex02().equals(fsTransNox)) {
                            liveJO = jo;
                            break;
                        }
                    }

                    if (liveJO == null) {
                        return; // JO no longer in list
                    }
                    try {
                        double lnRemain = Double.parseDouble(liveJO.getIndex11());
                        if (lsStatusLabel.equals("RELEASING")) {
                            mainCtrl.setTimer(lsStatusLabel); // finished — no countdown
                            return;
                        } else {
                            mainCtrl.setTimer(lnRemain > 0
                                    ? formatHoursToHHMMSS(lnRemain)
                                    : "");
                        }
                    } catch (NumberFormatException ignored) {
//                            mainCtrl.setTimer("CHECKING");
                    }
                })
        );
        flashTimeline.setCycleCount(maxFlashes);

        // Use setOnFinished — runs AFTER all cycles complete, not inside them
        flashTimeline.setOnFinished(e -> {
            pane.setOpacity(1.0);
            pbIsFlashing = false;
            flashTimeline = null;

            // Hold notification for 30 seconds, update timer every second
            serviceReload = new Timeline(
                    new KeyFrame(Duration.seconds(1), ev -> {
                        //Keep Refreshing Service pit
                        loadServicePit();
                        // Re-fetch live JO from shared JOList every second
                        JobOrderModel liveJO = null;
                        for (JobOrderModel jo : JOList) {
                            if (jo.getIndex02().equals(fsTransNox)) {
                                liveJO = jo;
                                break;
                            }
                        }

                        if (liveJO == null) {
                            return; // JO no longer in list
                        }
                        try {
                            double lnRemain = Double.parseDouble(liveJO.getIndex11());
                            if (lsStatusLabel.equals("RELEASING")) {
                                mainCtrl.setTimer(lsStatusLabel); // finished — no countdown
                                return;
                            } else {
                                mainCtrl.setTimer(lnRemain > 0
                                        ? formatHoursToHHMMSS(lnRemain)
                                        : "");
                            }
                        } catch (NumberFormatException ignored) {
//                            mainCtrl.setTimer("CHECKING");
                        }
                    })
            );

            // Hold notification for 30 seconds, then resume
            Timeline resumeDelay = new Timeline(
                    new KeyFrame(Duration.seconds(60), ev -> {
                        serviceReload.stop();
                        pitSyncClock.play();          // restart sync clock
                        loadServicePit();
                        syncMainServiceWithActivePit();
                        restartMainServiceRotationClock();
                    })
            );

            serviceReload.setCycleCount(Animation.INDEFINITE);
            serviceReload.play();

            resumeDelay.setCycleCount(1);
            resumeDelay.play();
        });

        flashTimeline.play();
    }

    private void stopFlash() {
        if (flashTimeline != null) {
            flashTimeline.stop();
            flashTimeline = null;
        }
    }

    private void playNotificationSound() {
        try {
            URL soundUrl = getClass().getResource(
                    "/ph/com/guanzongroup/tcs/view/sound/notify.wav");
            if (soundUrl == null) {
                return;
            }

            AudioClip clip = new AudioClip(soundUrl.toExternalForm());
            clip.play();
        } catch (Exception e) {
            Logger.getLogger(PITMonitorController.class.getName())
                    .log(Level.WARNING, null, e);
        }
    }
}
