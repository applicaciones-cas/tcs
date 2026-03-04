package ph.com.guanzongroup.tcs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javafx.application.Application;
import org.guanzon.appdriver.base.GRider;

public class LetMeInn {

    public static void main(String[] args) {
        String path;
        String lsTemp;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            path = "D:/GGC_Maven_Systems";
            lsTemp = "D:/temp";
        } else {
            path = "/srv/GGC_Maven_Systems";
            lsTemp = "/srv/temp";
        }
        System.setProperty("sys.default.path.config", path);
        System.setProperty("sys.default.path.metadata", System.getProperty("sys.default.path.config") + "/config/metadata/new/");
        System.setProperty("sys.default.path.temp", lsTemp);

        if (!loadProperties()) {
            System.err.println("Unable to load config.");
            System.exit(1);
        } else {
            System.out.println("Config file loaded successfully.");
        }

        GRider instance = new GRider("IntegSys");

        if (!instance.logUser("IntegSys", "M001000001")) {
            System.err.println(instance.getMessage());
            System.exit(1);
        }
        if (!instance.loadEnv("IntegSys")) {
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }

        App poApp = new App();
        poApp.setGRider(instance);

        Application.launch(poApp.getClass());
    }
    
    
    private static boolean loadProperties() {
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream(System.getProperty("sys.default.path.config") + "/config/cas.properties"));
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}


