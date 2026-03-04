package ph.com.guanzongroup.tcs;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.InputStream;
import static javafx.application.Application.launch;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import org.guanzon.appdriver.base.GRider;
import ph.com.guanzongroup.tcs.controller.TCSDashBoardController;

/**
 * JavaFX App
 */
public class App extends Application {

    public final static String pxeMainFormTitle = "Time Commitment Service System";
    public final static String pxeFolderView = "/ph/com/guanzongroup/tcs/view/";
    public final static String pxeMainForm = pxeFolderView + "TCSDashBoard.fxml";
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

        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(pxeMainFormTitle);

        // Fit screen exactly
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);

        // App icon
        InputStream iconStream
                = getClass().getResourceAsStream(
                        "/ph/com/guanzongroup/tcs/view/image/app_logotcs.png");

        stage.getIcons().add(new Image(iconStream));

        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void setGRider(GRider foValue) {
        oApp = foValue;
    }
}
