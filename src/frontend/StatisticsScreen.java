package frontend;

import backend.Card;
import backend.CardManager;
import backend.CardSRS;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class StatisticsScreen {

    private final BorderPane root;
    private final CardManager cardManager;
    private final Stage stage;
    private final Consumer<Scene> sceneSwitcher;
    private final Scene previousScene;
    private Font itemFont;

    public StatisticsScreen(Stage stage, CardManager cardManager, Consumer<Scene> sceneSwitcher, Scene previousScene) {
        this.stage = stage;
        this.cardManager = cardManager;
        this.sceneSwitcher = sceneSwitcher;
        this.previousScene = previousScene;
        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#161b33"), CornerRadii.EMPTY, Insets.EMPTY)));

        loadResources();
        setupUI();
    }

    private void loadResources() {
        itemFont = Font.loadFont(getClass().getResource("/fonts/Montserrat-Regular.ttf").toExternalForm(), 16);
    }

    private void setupUI() {
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.TOP_LEFT);

        // ===== Back Button (icon only) =====
        ImageView backIcon = new ImageView(new Image(getClass().getResource("/icons/back.png").toExternalForm()));
        backIcon.setFitWidth(24);
        backIcon.setFitHeight(24);
        JFXButton backBtn = new JFXButton();
        backBtn.setGraphic(backIcon);
        backBtn.setStyle("-fx-background-color: transparent;");
        backBtn.setOnAction(e -> sceneSwitcher.accept(previousScene));

        // ===== Load Button =====
        JFXButton loadSrsBtn = new JFXButton("Zaladuj");
        loadSrsBtn.setFont(itemFont);
        loadSrsBtn.setStyle("-fx-background-color: #1e244f; -fx-text-fill: white;");
        loadSrsBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select SRS CSV File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                try {
                    if (cardManager.validateSRSFile(selectedFile)) {
                        // Replace existing srs.csv
                        java.nio.file.Files.copy(
                                selectedFile.toPath(),
                                new File("loc/srs.csv").toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        // Reload SRS in memory
                        cardManager.srsService.loadSRS();
                        System.out.println("SRS loaded successfully from: " + selectedFile.getAbsolutePath());
                    } else {
                        System.out.println("Selected file is not a valid SRS CSV.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JFXButton saveSrsBtn = new JFXButton("Zapisz");
        saveSrsBtn.setFont(itemFont);
        saveSrsBtn.setStyle("-fx-background-color: #1e244f; -fx-text-fill: white;");
        saveSrsBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save SRS Data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File destination = fileChooser.showSaveDialog(stage);

            if (destination != null) {
                try {
                    // Assuming srs.csv is in project root or resources folder
                    File source = new File("loc/srs.csv");
                    java.nio.file.Files.copy(
                            source.toPath(),
                            destination.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("SRS saved to: " + destination.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // ===== Card Counter =====
        int cardCount = cardManager.getAllCards().size();
        Label counterLabel = new Label("Fiszki: " + cardCount);
        counterLabel.setFont(itemFont);
        counterLabel.setTextFill(Color.WHITE);

        // ===== Top bar HBox =====
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox topBox = new HBox(10, backBtn, leftSpacer, counterLabel, rightSpacer, loadSrsBtn, saveSrsBtn);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));
        root.setTop(topBox);

        // ===== Table Header =====
        HBox header = new HBox(10);
        header.setPadding(new Insets(5, 10, 5, 10));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color: #5bc0be; -fx-border-width: 0 0 2 0;");

        Text nameHeader = new Text("Fiszka");
        nameHeader.setFont(itemFont);
        nameHeader.setFill(Color.WHITE);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Text repHeader = new Text("Powtorzenia");
        repHeader.setFont(itemFont);
        repHeader.setFill(Color.WHITE);

        Text easeHeader = new Text("Wspolczynnik uczenia");
        easeHeader.setFont(itemFont);
        easeHeader.setFill(Color.WHITE);

        header.getChildren().addAll(nameHeader, headerSpacer, repHeader, easeHeader);
        contentBox.getChildren().add(header);

        // ===== Populate rows =====
        List<Card> allCards = cardManager.getAllCards();
        for (Card c : allCards) {
            HBox row = createCardRow(c);
            contentBox.getChildren().add(row);
        }

        // ===== ScrollPane =====
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStylesheets().add(getClass().getResource("/css/scrollbar.css").toExternalForm());

        root.setCenter(scrollPane);
    }

    private HBox createCardRow(Card card) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #5bc0be; -fx-border-width: 0 0 1 0;");

        // Card Name
        Text name = new Text(card.pol);
        name.setFill(Color.WHITE);
        name.setFont(itemFont);
        name.wrappingWidthProperty().set(400); // wrap text nicely

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Repetitions
        CardSRS srs = cardManager.srsService.getSrsMap().get(card.id);
        String repetitions = srs != null ? String.valueOf(srs.repetitions) : "-";
        Text repText = new Text(repetitions);
        repText.setFill(Color.WHITE);
        repText.setFont(itemFont);

        // Ease factor
        String easeFactor = srs != null ? String.format("%.2f", srs.easeFactor) : "-";
        Text easeText = new Text(easeFactor);
        easeText.setFill(Color.WHITE);
        easeText.setFont(itemFont);

        row.getChildren().addAll(name, spacer, repText, easeText);
        return row;
    }

    public BorderPane getRoot() {
        return root;
    }
}
