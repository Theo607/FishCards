package frontend;

import backend.Card;
import backend.CardManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class AddCardScreen {

    private final Stage stage;
    private final BorderPane root;
    private final CardManager cardManager;
    private File selectedImage;

    private boolean polFieldIsTop = true;

    public AddCardScreen(Stage stage, CardManager cardManager) {
        this.stage = stage;
        this.cardManager = cardManager;
        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#161b33"), CornerRadii.EMPTY, Insets.EMPTY)));
        setupUI();
    }

    private void setupUI() {
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(30));
        mainBox.setAlignment(Pos.CENTER);

        Font defaultFont = Font.font("Arial", 18);

        // ===== Back Button =====
        Button backBtn = new Button();
        backBtn.setStyle("-fx-background-color: transparent;");
        ImageView backIcon = new ImageView(new Image(getClass().getResource("/icons/back.png").toExternalForm()));
        backIcon.setFitWidth(24);
        backIcon.setFitHeight(24);
        backBtn.setGraphic(backIcon);
        backBtn.setOnAction(e -> {
            Dashboard dashboard = new Dashboard(stage, cardManager, scene -> stage.setScene(scene));
            stage.setScene(new Scene(dashboard.getRoot(), 800, 600));
        });
        HBox topBox = new HBox(backBtn);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));
        root.setTop(topBox);

        // ===== Input Fields =====
        TextField polField = new TextField();
        polField.setPromptText("Polski");
        polField.setFont(defaultFont);
        polField.setMaxWidth(400);

        TextField espField = new TextField();
        espField.setPromptText("Hiszpanski");
        espField.setFont(defaultFont);
        espField.setMaxWidth(400);

        Button swapBtn = new Button();
        swapBtn.setStyle("-fx-background-color: transparent;");
        ImageView swapIcon = new ImageView(new Image(getClass().getResource("/icons/swap.png").toExternalForm()));
        swapIcon.setFitWidth(24);
        swapIcon.setFitHeight(24);
        swapBtn.setGraphic(swapIcon);
        swapBtn.setOnAction(e -> {
            String tempPrompt = polField.getPromptText();
            polField.setPromptText(espField.getPromptText());
            espField.setPromptText(tempPrompt);

            String tempText = polField.getText();
            polField.setText(espField.getText());
            espField.setText(tempText);

            polFieldIsTop = !polFieldIsTop;
        });

        VBox inputBox = new VBox(10, polField, swapBtn, espField);
        inputBox.setAlignment(Pos.CENTER);

        // ===== Buttons =====
        Button imageBtn = new Button("Wybierz obrazek");
        imageBtn.setFont(Font.font("Arial", 16));
        imageBtn.setTextFill(Color.WHITE);
        imageBtn.setPrefWidth(150);
        imageBtn.setStyle("-fx-background-color: #1e244f;");

        Button submitBtn = new Button("Dodaj fiszke");
        submitBtn.setFont(Font.font("Arial", 16));
        submitBtn.setTextFill(Color.WHITE);
        submitBtn.setPrefWidth(150);
        submitBtn.setStyle("-fx-background-color: #1e244f;");
        submitBtn.setOnAction(e -> {
            Card c = new Card();
            c.id = cardManager.getAllCards().size();

            if (polFieldIsTop) {
                c.pol = polField.getText();
                c.esp = espField.getText();
            } else {
                c.pol = espField.getText();
                c.esp = polField.getText();
            }

            if (selectedImage != null)
                cardManager.addCardWithImage(c, selectedImage);
            else
                cardManager.addCard(c);

            cardManager.syncImages();
            cardManager.syncToRepo();

            Dashboard dashboard = new Dashboard(stage, cardManager, scene -> stage.setScene(scene));
            stage.setScene(new Scene(dashboard.getRoot(), 800, 600));
        });

        VBox buttonBox = new VBox(10, imageBtn, submitBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // ===== Image Preview =====
        ImageView imagePreview = new ImageView(new Image(getClass().getResource("/icons/null.png").toExternalForm()));
        imagePreview.setFitWidth(120);
        imagePreview.setFitHeight(120);
        imagePreview.setPreserveRatio(true);
        imagePreview.setSmooth(true);

        imageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Wybierz obrazek");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImage = file;
                imagePreview.setImage(new Image(file.toURI().toString()));
            }
        });

        VBox imageBox = new VBox(imagePreview);
        imageBox.setAlignment(Pos.CENTER);

        // ===== Bottom HBox: buttons left, image right =====
        HBox bottomBox = new HBox(20, buttonBox, imageBox);
        bottomBox.setAlignment(Pos.CENTER);

        // ===== Assemble main box =====
        mainBox.getChildren().addAll(inputBox, bottomBox);
        root.setCenter(mainBox);
    }

    public BorderPane getRoot() {
        return root;
    }
}
