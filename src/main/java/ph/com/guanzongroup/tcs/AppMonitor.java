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
public class AppMonitor extends Application {

    public final static String pxeMainFormTitle = "Time Commitment Service System";
    public final static String pxeFolderView = "/ph/com/guanzongroup/tcs/view/";
    public final static String pxeMainForm = pxeFolderView + "TCSDashBoard.fxml";
    public static GRider oApp;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader view = new FXMLLoader();
        view.setLocation(getClass().getResource(pxeMainForm));

        TCSDashBoardController controller = new TCSDashBoardController();
        controller.setGRider(oApp);

        view.setController(controller);
        Parent parent = view.load();
        Scene scene = new Scene(parent);

        //get the screen size
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
//        stage.getIcons().add(new Image(pxeStageIcon));
        stage.setTitle(pxeMainFormTitle);

        // set stage as maximized but not full screen
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        InputStream iconStream = getClass().getResourceAsStream("/ph/com/guanzongroup/tcs/view/image/app_logotcs.png");
        Image icon = new Image(iconStream);
        stage.getIcons().add(icon);
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
