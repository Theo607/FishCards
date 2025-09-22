package frontend;

import backend.CardManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainFrame extends Application {

    private CardManager cardManager;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Initialize CardManager once here
        cardManager = new CardManager();
        cardManager.ensureFS();

        // Set application icon
        Image appIcon = new Image(getClass().getResource("/icons/fish_logo.png").toExternalForm());
        primaryStage.getIcons().add(appIcon);

        // Pass cardManager into Dashboard
        Dashboard dashboard = new Dashboard(primaryStage, cardManager, scene -> primaryStage.setScene(scene));
        Scene dashboardScene = new Scene(dashboard.getRoot(), 800, 600);

        primaryStage.setTitle("Fiszki");
        primaryStage.setScene(dashboardScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
