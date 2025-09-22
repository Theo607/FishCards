package frontend;

import backend.CardManager;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class Dashboard {

    private final Stage stage;
    private final BorderPane root;
    private Font buttonFont;

    private final Consumer<Scene> sceneSwitcher;
    private final CardManager cardManager;

    public Dashboard(Stage stage, CardManager cardManager, Consumer<Scene> sceneSwitcher) {
        this.stage = stage;
        this.cardManager = cardManager;
        this.sceneSwitcher = sceneSwitcher;

        root = new BorderPane();
        root.setStyle("-fx-background-color: #161b33;");

        loadResources();
        setupUI();
    }

    private void loadResources() {
        buttonFont = Font.loadFont(
                getClass().getResource("/fonts/Montserrat-Regular.ttf").toExternalForm(), 16);
    }

    private void setupUI() {
        // ===== Logo =====
        ImageView logo = new ImageView(
                new Image(getClass().getResource("/icons/logo.gif").toExternalForm()));
        logo.setFitWidth(200);
        logo.setPreserveRatio(true);

        VBox topBox = new VBox(20, logo);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(30, 0, 30, 0));
        root.setTop(topBox);

        // ===== Buttons =====
        JFXButton learnBtn = createButton("Nauka", "/icons/graduation-cap.png");
        JFXButton addBtn = createButton("Dodaj", "/icons/add.png");
        JFXButton editBtn = createButton("Edytuj", "/icons/edit.png");
        JFXButton statsBtn = createButton("Statystyki", "/icons/stats.png");
        JFXButton exitBtn = createButton("Wyjdz", "/icons/exit.png"); // new button

        // Button actions
        learnBtn.setOnAction(e -> {
            Scene currentScene = stage.getScene();
            ReviewScreen reviewScreen = new ReviewScreen(stage, cardManager, stage.getScene());
            sceneSwitcher.accept(new Scene(reviewScreen.getRoot(), 800, 600));
        });

        addBtn.setOnAction(e -> {
            AddCardScreen addCard = new AddCardScreen(stage, cardManager);
            sceneSwitcher.accept(new Scene(addCard.getRoot(), 800, 600));
        });

        editBtn.setOnAction(e -> {
            EditCardScreen editScreen = new EditCardScreen(stage, cardManager, sceneSwitcher, stage.getScene());
            sceneSwitcher.accept(new Scene(editScreen.getRoot(), 800, 600));
        });

        statsBtn.setOnAction(e -> {
            StatisticsScreen statsScreen = new StatisticsScreen(stage, cardManager, sceneSwitcher, stage.getScene());
            sceneSwitcher.accept(new Scene(statsScreen.getRoot(), 800, 600));
        });

        // Exit button closes the application
        exitBtn.setOnAction(e -> stage.close());

        VBox buttonBox = new VBox(20, learnBtn, addBtn, editBtn, statsBtn, exitBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 50, 0, 50));

        // Uniform fixed width for all buttons
        for (JFXButton btn : new JFXButton[] { learnBtn, addBtn, editBtn, statsBtn, exitBtn }) {
            btn.setPrefWidth(200);
        }

        root.setCenter(buttonBox);
    }

    private JFXButton createButton(String text, String iconPath) {
        JFXButton button = new JFXButton(text);

        if (buttonFont != null) {
            button.setFont(buttonFont);
        }

        if (iconPath != null) {
            ImageView icon = new ImageView(
                    new Image(getClass().getResource(iconPath).toExternalForm()));
            icon.setFitHeight(24);
            icon.setFitWidth(24);
            button.setGraphic(icon);
            button.setGraphicTextGap(10);
        }

        button.setStyle("-fx-background-color: #1e244f; -fx-text-fill: white;");
        button.setPadding(new Insets(10, 20, 10, 20));
        button.setRipplerFill(Color.LIGHTGRAY);

        return button;
    }

    public BorderPane getRoot() {
        return root;
    }
}
