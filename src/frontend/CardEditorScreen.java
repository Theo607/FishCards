package frontend;

import backend.Card;
import backend.CardManager;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class CardEditorScreen {

    private Stage stage;
    private BorderPane root;
    private CardManager cardManager;
    private Consumer<Scene> sceneSwitcher;
    private Card card;
    private Font font;
    private final Scene previousScene;
    private File selectedImageFile = null;

    public CardEditorScreen(Stage stage, CardManager cardManager, Consumer<Scene> sceneSwitcher, Card card,
                            Scene previousScene) {
        this.stage = stage;
        this.cardManager = cardManager;
        this.sceneSwitcher = sceneSwitcher;
        this.card = card;
        this.previousScene = previousScene;
        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#161b33"), CornerRadii.EMPTY, Insets.EMPTY)));

        font = Font.loadFont(getClass().getResource("/fonts/Montserrat-Regular.ttf").toExternalForm(), 16);

        setupUI();
    }

    private void setupUI() {
        // ===== Image Preview =====
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-border-color: #5bc0be; -fx-border-width: 2; -fx-background-color: #1e244f;");

        File imgFile = null;
        if (card.imgp != null && !card.imgp.isEmpty()) {
            imgFile = new File("repo", card.imgp);
        }
        if (imgFile == null || !imgFile.exists()) {
            imageView.setImage(new Image(getClass().getResource("/icons/null.png").toExternalForm()));
        } else {
            imageView.setImage(new Image(imgFile.toURI().toString()));
        }

        VBox rightColumn = new VBox(imageView);
        rightColumn.setAlignment(Pos.TOP_CENTER);

        // ===== Left Column: Text Fields and Buttons =====
        VBox leftColumn = new VBox(20);
        leftColumn.setAlignment(Pos.TOP_CENTER);

        double fieldWidth = 300;

        // Polish text field
        VBox polBox = new VBox(5);
        Label polLabel = new Label("Polski:");
        polLabel.setFont(font);
        polLabel.setTextFill(Color.WHITE);
        TextField polField = new TextField(card.pol);
        polField.setPrefWidth(fieldWidth);
        polBox.getChildren().addAll(polLabel, polField);

        // Spanish text field
        VBox espBox = new VBox(5);
        Label espLabel = new Label("Hiszpanski:");
        espLabel.setFont(font);
        espLabel.setTextFill(Color.WHITE);
        TextField espField = new TextField(card.esp);
        espField.setPrefWidth(fieldWidth);
        espBox.getChildren().addAll(espLabel, espField);

        // Buttons
        JFXButton pickImageBtn = createButton("Wybierz obrazek", null);
        JFXButton saveBtn = createButton("Zapisz", null);

        // Uniform button width and centered text
        for (JFXButton btn : new JFXButton[]{pickImageBtn, saveBtn}) {
            btn.setPrefWidth(200);
            btn.setTextFill(Color.WHITE);
            btn.setAlignment(Pos.CENTER);
        }

        // Image picker action
        pickImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Wybierz obrazek");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImageFile = file;
                imageView.setImage(new Image(file.toURI().toString()));
            }
        });

        // Save action
        saveBtn.setOnAction(e -> {
            String newPol = polField.getText().trim();
            String newEsp = espField.getText().trim();
            String newImgPath = card.imgp;
            try {
                if (selectedImageFile != null) {
                    newImgPath = cardManager.imgService.saveImage(selectedImageFile);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            cardManager.updateCard(card.id, newPol, newEsp, newImgPath);
            cardManager.syncToRepo();
            cardManager.syncImages();
            sceneSwitcher.accept(previousScene);
        });

        leftColumn.getChildren().addAll(polBox, espBox, pickImageBtn, saveBtn);

        // ===== Main Layout =====
        HBox mainContainer = new HBox(30, leftColumn, rightColumn);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // ===== Back Button =====
        JFXButton backBtn = createIconButton("/icons/back.png", () -> sceneSwitcher.accept(previousScene));
        HBox topBox = new HBox(backBtn);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));

        root.setTop(topBox);
        root.setCenter(mainContainer);
    }

    private JFXButton createIconButton(String resourcePath, Runnable action) {
        JFXButton button = new JFXButton();
        Image iconImage = null;
        if (getClass().getResource(resourcePath) != null) {
            iconImage = new Image(getClass().getResource(resourcePath).toExternalForm());
        }
        ImageView icon = new ImageView(iconImage);
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        button.setGraphic(icon);
        button.setRipplerFill(Color.LIGHTGRAY);
        button.setStyle("-fx-background-color: transparent;");
        if (action != null)
            button.setOnAction(e -> action.run());
        return button;
    }

    private JFXButton createButton(String text, String iconPath) {
        JFXButton button = new JFXButton(text);
        button.setFont(font);
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: #1e244f; -fx-text-fill: white;");
        button.setPadding(new Insets(8, 20, 8, 20));
        button.setRipplerFill(Color.LIGHTGRAY);
        if (iconPath != null) {
            ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            button.setGraphic(icon);
            button.setGraphicTextGap(5);
        }
        return button;
    }

    public BorderPane getRoot() {
        return root;
    }
}