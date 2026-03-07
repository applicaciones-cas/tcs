package ph.com.guanzongroup.tcs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import java.io.InputStream;
import org.guanzon.appdriver.agent.ShowMessageFX;

import org.guanzon.appdriver.base.GRider;
import ph.com.guanzongroup.tcs.controller.PITMonitorController;
import ph.com.guanzongroup.tcs.controller.TCSDashBoardController;

/**
 * JavaFX App
 */
public class App extends Application {

    public final static String pxeMainFormTitle = "Time Commitment Service System";
    public final static String pxeFolderView = "/ph/com/guanzongroup/tcs/view/";
    public final static String pxeMainForm = pxeFolderView + "TCSDashBoard.fxml";
    public final static String pxeMainMonitoringForm = pxeFolderView + "PitMonitor.fxml";
    public final static String pxeSubForm = pxeFolderView + "TCSDashBoard1366x768.fxml";
    public static GRider oApp;

    @Override
    public void start(Stage stage) throws Exception {

        // Get screen size FIRST
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        double screenWidth = bounds.getWidth();
        double screenHeight = bounds.getHeight();

        // Choose layout automatically
        String formToLoad;
        if (screenWidth <= 1366) {
            formToLoad = pxeSubForm;      // laptop layout
        } else {
            formToLoad = pxeMainForm;     // full HD / large monitor
        }

        FXMLLoader view = new FXMLLoader(getClass().getResource(formToLoad));
        TCSDashBoardController controller = new TCSDashBoardController();
        controller.setGRider(oApp);
        view.setController(controller);

        Parent parent = view.load();
        Scene scene = new Scene(parent);

        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.F4
                    && e.isAltDown()) {
                e.consume();
                ShowMessageFX.Information(stage,
                        "Please use the End of Day button to exit the application.",
                        "Exit Not Allowed", null);
            }
        });
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(pxeMainFormTitle);

        // Fit screen exactly
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);

        // App icon
        InputStream iconStream = getClass().getResourceAsStream(
                "/ph/com/guanzongroup/tcs/view/image/app_logotcs.png");
        stage.getIcons().add(new Image(iconStream));

        // Ensure whole app exits when main closes
        Platform.setImplicitExit(true);
        stage.setOnCloseRequest(e -> {
            e.consume(); // ← blocks the close
            Platform.runLater(()
                    -> ShowMessageFX.Information(stage,
                            "Please use the End of Day button to exit the application.",
                            "Exit Not Allowed", null)
            );
        });
        // Show main stage
        stage.show();

        // =====================================
        // OPEN CUSTOMER DISPLAY (SECOND MONITOR)
        // =====================================
        ObservableList<Screen> screens = Screen.getScreens();
        if (screens.size() > 1) {

            Screen customerScreen = screens.get(1);
            Rectangle2D bounds2 = customerScreen.getVisualBounds();

            FXMLLoader viewCustomer = new FXMLLoader(getClass().getResource(pxeMainMonitoringForm));
            PITMonitorController custController = new PITMonitorController();

            // After both controllers are created
            controller.setPITMonitorListener(custController);

            // SHARE SAME JOB ORDER INSTANCE
            custController.setJobOrder(controller.getJobOrder());
            custController.setJobOrderList(controller.getJobOrderList());
            viewCustomer.setController(custController);
            Parent customerRoot = viewCustomer.load();
            Scene customerScene = new Scene(customerRoot);
            Stage customerStage = new Stage();

            customerScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
                if ((e.getCode() == javafx.scene.input.KeyCode.F4 && e.isAltDown())
                        || (e.getCode() == javafx.scene.input.KeyCode.SPACE && e.isAltDown())) {
                    e.consume();
                }
            });
            customerStage.initStyle(StageStyle.UNDECORATED);

            // Make customer stage owned by main stage → single taskbar icon
            customerStage.initOwner(stage);

            customerStage.setScene(customerScene);

            // Fit second monitor exactly
            customerStage.setX(bounds2.getMinX());
            customerStage.setY(bounds2.getMinY());
            customerStage.setWidth(bounds2.getWidth());
            customerStage.setHeight(bounds2.getHeight());

            customerStage.setOnCloseRequest(e -> e.consume());
            customerStage.show();

        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void setGRider(GRider foValue) {
        oApp = foValue;
    }
}
