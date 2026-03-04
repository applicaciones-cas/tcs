package ph.com.guanzongroup.tcs.controller;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import ph.com.guanzongroup.cas.joborder.base.JobOrder;

/**
 * FXML Controller class
 *
 * @author Valencia, Maynard
 */
public class TCSDashBoardController implements Initializable {

    private GRider oApp;
    private JobOrder oTrans;

    private int pnRow = 0;
    private boolean pbLoaded = false;
    private Date pdPeriod = null;

    @FXML
    private AnchorPane mainAnchor;
    @FXML
    private TableView<JobOrderModel> tblJobOrder;
    @FXML
    private TableColumn tblColNo,
            tblColJobOrder, tblColCustomer,
            tblColMCModel, tblColMechanic,
            tblColServiceType, tblColProgress;

    @FXML
    private Label lblUser, lblBranch, lblDateTime,
            lblOngoingTime, lblOnGoingCount,
            lblQueueTime, lblQueueCount,
            lblFinishCount, lblFinishTime, lblProgress, lblProgressTime, lblVersion;
    @FXML
    private TextField txtJobOrder, txtCustomer, txtMCModel, txtMechanic, txtSerivceType;
    @FXML
    private ProgressBar pbProgress;
    @FXML
    private Button btnRetrieve, btnFilter, btnStart, btnPause, btnFinish;

    @FXML
    private FontAwesomeIconView iconPause;
    private boolean pbRunning = false;

    private Timeline globalClock;
    private ScheduledExecutorService autoSaveExecutor;
    private final ObservableList<JobOrderModel> JOList = FXCollections.observableArrayList();

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

        oTrans = new JobOrder(oApp, oApp.getBranchCode(), false);
        oTrans.setWithUI(true);

        initButtonClick();
        initTableGrid();
        clearFields();

        loadUserInfo();
        setAppVersion("V1");
        getTime();
        Platform.runLater(() -> {
            Stage stage = getStage();
            stage.setOnCloseRequest(e -> stopAutoSaveThread());
        });
        pbLoaded = true;
    }

    public void setGRider(GRider foValue) {
        oApp = foValue;
    }

    private void initButtonClick() {

        btnRetrieve.setOnAction(this::cmdButton_Click);
        btnFilter.setOnAction(this::cmdButton_Click);
        btnStart.setOnAction(this::cmdButton_Click);
        btnPause.setOnAction(this::cmdButton_Click);
        btnFinish.setOnAction(this::cmdButton_Click);

    }

    public void initTableGrid() {

        tblColNo.setStyle("-fx-alignment: CENTER;");
        tblColJobOrder.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        tblColCustomer.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        tblColMCModel.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        tblColMechanic.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        tblColServiceType.setStyle("-fx-alignment:CENTER-LEFT;-fx-padding: 0 0 0 5;");
        tblColProgress.setStyle("-fx-alignment: CENTER-LEFT;");

        tblColNo.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index01"));
        tblColJobOrder.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index02"));
        tblColCustomer.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index03"));
        tblColMCModel.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index04"));
        tblColMechanic.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index05"));
        tblColServiceType.setCellValueFactory(new PropertyValueFactory<JobOrderModel, String>("index06"));
        tblColProgress.setCellFactory(col
                -> new TableCell<JobOrderModel, String>() {

            private final ProgressBar progressBar = new ProgressBar();
            private final Label percentLabel = new Label();
            private final Label pitLabel = new Label();
            private final Label timeLabel = new Label();

            private final VBox box = new VBox(2);
            private final StackPane stack = new StackPane();

            {
                progressBar.setPrefWidth(115);
                progressBar.getStyleClass().add("progress-bar");

                percentLabel.getStyleClass().add("progress-label");
                pitLabel.getStyleClass().add("pit-label");
                timeLabel.getStyleClass().add("time-label");

                stack.getChildren().addAll(progressBar, percentLabel);
//                timeLabel.setAlignment(Pos.BOTTOM_RIGHT); 
                box.setAlignment(Pos.CENTER_LEFT);
                timeLabel.setAlignment(Pos.CENTER_RIGHT);
                timeLabel.setMaxWidth(Double.MAX_VALUE);
                box.getChildren().addAll(pitLabel, stack, timeLabel);
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || getIndex() < 0) {
                    setGraphic(null);
                    return;
                }

                JobOrderModel jo
                        = getTableView().getItems().get(getIndex());

                String ref = jo.getIndex08();

                // =========================
                // IF ON QUEUE
                // =========================
                if (ref == null || ref.trim().isEmpty()) {
                    if (jo.getIndex12().isEmpty()) {
                        progressBar.setProgress(0);
                        percentLabel.setText("ON - QUEUE");
                        double remain = 0;
                        try {
                            remain = Double.parseDouble(jo.getIndex07());
                        } catch (Exception e) {

                            remain = 0.0;
                        }
                        int mins = decimalHourToMinutes(remain);
                        pitLabel.setText("     ");
                        timeLabel.setText(formatHoursToHHMMSS(remain));

                        setGraphic(box);
                        return;
                    } else {
                        progressBar.setProgress(1);
                        percentLabel.setText("FINISHED");
                        pitLabel.setText("     ");
                        timeLabel.setText("");

                        setGraphic(box);
                        return;
                    }
                }

                try {
                    double total
                            = Double.parseDouble(jo.getIndex07());

                    double remain
                            = Double.parseDouble(jo.getIndex11());

                    double progress = 0;

                    if (total > 0) {
                        progress = (total - remain) / total;
                    }

                    progress = Math.max(0,
                            Math.min(progress, 1));

                    progressBar.setProgress(progress);

                    int percent = (int) (progress * 100);
                    int mins = decimalHourToMinutes(remain);
                    percentLabel.setText(percent + "%");

                    // PIT NUMBER
                    pitLabel.setText("PIT : " + jo.getIndex09());

                    timeLabel.setText("     " + formatHoursToHHMMSS(remain));
//                    // TIME REMAINING
//                    int mins = decimalHourToMinutes(remain);
//                    timeLabel.setText(formatMinutes(mins));
                } catch (Exception e) {
                    progressBar.setProgress(0);
                    percentLabel.setText("0%");
                }

                setGraphic(box);
            }
        });
        /*making column's position uninterchangebale*/
        tblJobOrder.widthProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) {
                TableHeaderRow header = (TableHeaderRow) tblJobOrder.lookup("TableHeaderRow");
                header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        header.setReordering(false);
                    }
                });
            }
        });
        tblJobOrder.setItems(JOList);

    }

    private int decimalHourToMinutes(double val) {
        return (int) Math.round(val * 60);
    }

    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        try {
            switch (lsButton) {
                case "btnRetrieve":
                    if (oTrans.RetrieveJobOrderList()) {
                        loadRecord();

                    } else {
                        ShowMessageFX.Information(getStage(), oTrans.getMessage(), "Information", null);
                    }
                    break;
                case "btnFilter":
                    if (oTrans.RetrieveJobOrderListRemaining()) {
                        loadRecord();

                    } else {
                        ShowMessageFX.Information(getStage(), oTrans.getMessage(), "Information", null);
                    }
                    break;
                case "btnStart":
                    //selection of pit
                    if (oTrans.showPitSelection()) {
                        //retrieve to refresh it data
                        if (oTrans.RetrieveJobOrderList()) {
                            loadRecord();
                        }
                    } else {

                        ShowMessageFX.Information(getStage(), oTrans.getMessage(), "Information", null);
                    }
                    break;
                case "btnPause":
                    //selection of pit
                    if (btnPause.getText().equalsIgnoreCase("resume")) {
                        if (oTrans.ResumeService()) {
                            //retrieve to refresh it data
                            if (oTrans.RetrieveJobOrderList()) {
                                loadRecord();
                            }
                        } else {
                            ShowMessageFX.Information(getStage(), oTrans.getMessage(), "Information", null);
                        }
                        break;
                    }
                    if (oTrans.PauseService()) {
                        //retrieve to refresh it data
                        if (oTrans.RetrieveJobOrderList()) {
                            loadRecord();
                        }
                    } else {

                        ShowMessageFX.Information(getStage(), oTrans.getMessage(), "Information", null);
                    }
                    break;

                case "btnFinish":
                    if (oTrans.FinishService()) {
                        if (oTrans.RetrieveJobOrderList()) {
                            loadRecord();
                        }
                    } else {
                        ShowMessageFX.Information(getStage(), oTrans.getMessage(), "Information", null);
                    }
                    break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            ShowMessageFX.Warning(getStage(), e.getMessage(), "Warning", null);
        }
    }

    @FXML
    private void tblJobOrder_Click(MouseEvent event) {

        try {
            pnRow = tblJobOrder.getSelectionModel().getSelectedIndex();
            if (pnRow >= 0) {

            }

            if (oTrans.OpenTransaction(JOList.get(pnRow).getIndex02())) {
                loadSelectedTransaction();

            }
        } catch (SQLException ex) {
            ShowMessageFX.Warning(getStage(), ex.getMessage(), "Warning", null);
        }
    }

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        if (!pbLoaded) {
            return;
        }

//        try {
        TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();

        if (!nv) {
            /*Lost Focus*/
            switch (lnIndex) {

            }
        } else { //Focus
            switch (lnIndex) {

            }
            txtField.selectAll();
        }
//        } catch (SQLException ex) {
//            Platform.runLater(() -> {
//                ShowMessageFX.Warning(getStage(), ex.getMessage(), "Catch Error", null);
//            });
//
//        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();

        switch (event.getCode()) {
            case F3:
                switch (lnIndex) {

                }
            case ENTER:
            case DOWN:
                CommonUtils.SetNextFocus(txtField);
                break;
            case UP:
                CommonUtils.SetPreviousFocus(txtField);
        }
//            ShowMessageFX.Warning(getStage(), e.getMessage(), "Warning", null);

    }

    private void clearFields() {
        lblOngoingTime.setText("00:00:00");
        lblOnGoingCount.setText("0");
        lblQueueTime.setText("00:00:00");
        lblQueueCount.setText("0");
        lblFinishCount.setText("0");
        lblFinishTime.setText("00:00:00");
        lblProgress.setText("0.00%");
        lblProgressTime.setText("00:00:00");
        pbProgress.setProgress(0);

        txtJobOrder.setText("");
        txtCustomer.setText("");
        txtMCModel.setText("");
        txtMechanic.setText("");
        txtSerivceType.setText("");
        btnStart.setVisible(false);
        btnPause.setVisible(false);
        btnFinish.setVisible(false);

    }

    private void loadRecord() {

        try {
            JOList.clear();
            stopAutoSaveThread();
            System.err.println("Start Adding Job Order to JO Lists");
            System.err.println("Loop count = " + oTrans.getJobOrderCount());

            for (int lnRow = 1; lnRow <= oTrans.getJobOrderCount(); lnRow++) {
                JOList.add(new JobOrderModel(String.valueOf(lnRow),
                        (String) oTrans.getJobOrder(lnRow, "sTransNox"),
                        (String) oTrans.getJobOrder(lnRow, "xClientNm"),
                        (String) oTrans.getJobOrder(lnRow, "sModelNme"),
                        (String) oTrans.getJobOrder(lnRow, "xMechncNm"),
                        (String) oTrans.getJobOrder(lnRow, "sJobDescr"),
                        oTrans.getJobOrder(lnRow, "nEstTimex").toString(),
                        (String) oTrans.getJobOrder(lnRow, "sReferNox"),
                        (String) oTrans.getJobOrder(lnRow, "sPITNmbrx"),
                        (String) oTrans.getJobOrder(lnRow, "cPausedxx"),
                        oTrans.getJobOrder(lnRow, "nRemainxx").toString(),
                        oTrans.getJobOrder(lnRow, "dJobEndxx").toString()));
            }
            clearFields();
            //Retrieve All JO Status every 1 mins
            if (oTrans.RetrieveServiceStatus()) {
                loadStatusService();

            }
            startGlobalClock();
            startAutoSaveThread();

//            pbRunning = false;
//            vbProgress.setVisible(false);
//            ptTimeline.stop();
        } catch (NullPointerException | SQLException e) {
            Platform.runLater(() -> ShowMessageFX.Warning(getStage(), e.getMessage(), "Warning", null));
            Logger.getLogger(TCSDashBoardController.class.getName()).log(Level.SEVERE, null, e);
//            pbRunning = false;
//            vbProgress.setVisible(false);
//            ptTimeline.stop();
        }
    }

    private void loadSelectedTransaction() throws SQLException {

        clearFields();
        //Retrieve All JO Status every 1 mins
        loadStatusService();

        JobOrderModel jo
                = tblJobOrder.getSelectionModel().getSelectedItem();

        if (jo == null) {
            return;
        }

        if (jo.getIndex10().equals(RecordStatus.ACTIVE)) {
            btnPause.setText("RESUME");
            iconPause.setGlyphName("PLAY");
        } else {
            iconPause.setGlyphName("PAUSE");
            btnPause.setText("PAUSE");
        }

        // ================= BUTTON DISPLAY =================
        btnStart.setVisible(jo.getIndex08().isEmpty());
        btnPause.setVisible(!jo.getIndex08().isEmpty());
        btnFinish.setVisible(!jo.getIndex08().isEmpty());

        btnStart.setManaged(jo.getIndex08().isEmpty());
        btnPause.setManaged(!jo.getIndex08().isEmpty());
        btnFinish.setManaged(!jo.getIndex08().isEmpty());

        // ================= BASIC INFO =================
        txtJobOrder.setText(jo.getIndex02());
        txtCustomer.setText(jo.getIndex03());
        txtMCModel.setText(jo.getIndex04());
        txtMechanic.setText(jo.getIndex05());
        txtSerivceType.setText(jo.getIndex06());

        // ================= TIME VALUES =================
        double total = 0;
        double remain = 0;
        try {
            total = Double.parseDouble(jo.getIndex07());
            remain = Double.parseDouble(jo.getIndex11());
        } catch (Exception e) {
            remain = 0;

        }

        // ================= QUEUE CHECK =================
        boolean onQueue
                = jo.getIndex08() == null
                || jo.getIndex08().trim().isEmpty();

        if (onQueue) {
            if (jo.getIndex12().isEmpty()) {
                lblProgress.setText("ON - QUEUE");
                pbProgress.setProgress(0);
                lblProgressTime.setText(formatHoursToHHMMSS(total - remain));
                return;
            } else {
                lblProgress.setText("FINISH");
                pbProgress.setProgress(1);
                lblProgressTime.setText("");
                btnStart.setVisible(false);
                return;
            }
        }

        // ================= PROGRESS COMPUTATION =================
        double progress = 0;

        if (total > 0) {
            progress = (total - remain) / total;
        }

        // clamp value between 0 and 1
        progress = Math.max(0, Math.min(1, progress));
        pbProgress.setProgress(progress);
        lblProgress.setText((int) (progress * 100) + "%");
        lblProgressTime.setText(formatHoursToHHMMSS(remain));

    }

    private void loadStatusService() throws SQLException {

        lblQueueCount.setText(oTrans.getJOServiceStatus("sQueCount").toString());
        lblQueueTime.setText(formatHoursToHHMMSS(Double.parseDouble(oTrans.getJOServiceStatus("nQueTimex").toString())));
        lblOnGoingCount.setText(oTrans.getJOServiceStatus("sOnGoingx").toString() + " | " + oTrans.getJOServiceStatus("sTotalPitx").toString());
        lblOngoingTime.setText(formatHoursToHHMMSS(Double.parseDouble(oTrans.getJOServiceStatus("nOnGgTime").toString())));
        lblFinishCount.setText(oTrans.getJOServiceStatus("sFinished").toString());
        lblFinishTime.setText(formatHoursToHHMMSS(Double.parseDouble(oTrans.getJOServiceStatus("nFnshTime").toString())));

    }

    private void startGlobalClock() {

        if (globalClock != null) {
            globalClock.stop();
        }

        globalClock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateAllProgress())
        );

        globalClock.setCycleCount(Animation.INDEFINITE);
        globalClock.play();
    }

    private void updateAllProgress() {

        JobOrderModel selected
                = tblJobOrder.getSelectionModel().getSelectedItem();

        int rowIndex = 1;

        for (JobOrderModel jo : JOList) {

            // skip queue (no PIT assigned)
            if (jo.getIndex09() == null || jo.getIndex09().isEmpty()) {
                rowIndex++;
                continue;
            }

            // skip paused
            if (RecordStatus.ACTIVE.equals(jo.getIndex10())) {
                rowIndex++;
                continue;
            }

            try {

                double total = Double.parseDouble(jo.getIndex07());
                double remain = Double.parseDouble(jo.getIndex11());

                if (total <= 0) {
                    rowIndex++;
                    continue;
                }

                if (remain > 0) {

                    // subtract 1 second
                    remain -= (1.0 / 3600.0);

                    if (remain < 0) {
                        remain = 0;
                    }

                    // update model
                    jo.setIndex11(String.format("%.4f", remain));
                    // update DB (CORRECT ROW)
                    oTrans.setJobOrder(rowIndex, "nRemainxx", remain);

                }

                // ===== UPDATE SELECTED PANEL =====
                if (selected == jo) {

                    double progress = (total - remain) / total;

                    pbProgress.setProgress(progress);

                    // STATUS DISPLAY
                    if (jo.getIndex08() == null || jo.getIndex08().isEmpty()) {

                        if (jo.getIndex12() == null || jo.getIndex12().isEmpty()) {
                            lblProgress.setText("ON - QUEUE");
                        } else {
                            lblProgress.setText("FINISHED");
                            pbProgress.setProgress(1);
                            lblProgressTime.setText("");
                        }

                    } else {
                        lblProgress.setText((int) (progress * 100) + "%");
                    }

                    lblProgressTime.setText(
                            formatHoursToHHMMSS(remain)
                    );
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            rowIndex++;
        }

        // refresh once only
        tblJobOrder.refresh();
    }

    private String formatHoursToHHMMSS(double hours) {

        int totalSeconds = (int) (hours * 3600);

        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private void loadUserInfo() {
        lblUser.setText(oApp.getLogName() + " || " + oApp.getBranchName());

    }

    private void setAppVersion(String fsValue) {
        lblVersion.setText("Guanzon Time Commitment Service " + fsValue);
    }

    private void getTime() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Calendar cal = Calendar.getInstance();
            int second = cal.get(Calendar.SECOND);

            Date date = new Date();
            String strTimeFormat = "hh:mm:";
            String strDateFormat = "MMMM dd, yyyy";
            String secondFormat = "ss";

            DateFormat timeFormat = new SimpleDateFormat(strTimeFormat + secondFormat);
            DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

            String formattedTime = timeFormat.format(date);
            String formattedDate = dateFormat.format(date);

            lblDateTime.setText(formattedDate + " || " + formattedTime);
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void saveServiceBay() {

        try {
            System.out.println("Auto-saving Service Bay...");
            if (!oTrans.saveServiceBay()) {
                System.err.println("Save failed. Reloading...");
                if (oTrans.RetrieveJobOrderList()) {

                    // UI updates MUST be on FX thread
                    Platform.runLater(() -> {
                        loadRecord();
                        tblJobOrder.refresh();
                    });
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(TCSDashBoardController.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private void startAutoSaveThread() {

        autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();

        autoSaveExecutor.scheduleAtFixedRate(() -> {

            saveServiceBay();

        }, 60, 60, TimeUnit.SECONDS);
        // delay 60 sec, repeat every 60 sec
    }

    public void stopAutoSaveThread() {
        if (autoSaveExecutor != null && !autoSaveExecutor.isShutdown()) {
            autoSaveExecutor.shutdownNow();
        }
    }
}
