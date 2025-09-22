package frontend;

import backend.Card;
import backend.CardManager;
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
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class EditCardScreen {

    private final Stage stage;
    private final BorderPane root;
    private final CardManager cardManager;
    private final Consumer<Scene> sceneSwitcher;
    private final Scene previousScene;
    private final Font itemFont;

    public EditCardScreen(Stage stage, CardManager cardManager, Consumer<Scene> sceneSwitcher, Scene previousScene) {
        this.stage = stage;
        this.cardManager = cardManager;
        this.sceneSwitcher = sceneSwitcher;
        this.previousScene = previousScene;

        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#161b33"), CornerRadii.EMPTY, Insets.EMPTY)));

        itemFont = Font.loadFont(getClass().getResource("/fonts/Montserrat-Regular.ttf").toExternalForm(), 16);

        setupUI();
    }

    private void setupUI() {
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.TOP_LEFT);

        // ===== Back Button =====
        ImageView backIcon = new ImageView(new Image(getClass().getResource("/icons/back.png").toExternalForm()));
        backIcon.setFitWidth(24);
        backIcon.setFitHeight(24);
        javafx.scene.control.Button backBtn = new javafx.scene.control.Button();
        backBtn.setGraphic(backIcon);
        backBtn.setStyle("-fx-background-color: transparent;");
        backBtn.setOnAction(e -> sceneSwitcher.accept(previousScene));

        // ===== Card Counter =====
        Label counterLabel = new Label("Fiszki: " + cardManager.getAllCards().size());
        counterLabel.setFont(itemFont);
        counterLabel.setTextFill(Color.WHITE);

        HBox topBox = new HBox(10, backBtn, counterLabel);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));
        root.setTop(topBox);

        // ===== Table Header =====
        HBox header = new HBox(10);
        header.setPadding(new Insets(5));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color: #5bc0be; -fx-border-width: 0 0 2 0;");

        Label nameHeader = new Label("Fiszka");
        nameHeader.setTextFill(Color.WHITE);
        nameHeader.setFont(itemFont);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label editHeader = new Label("Edytuj");
        editHeader.setTextFill(Color.WHITE);
        editHeader.setFont(itemFont);

        Label removeHeader = new Label("Usun");
        removeHeader.setTextFill(Color.WHITE);
        removeHeader.setFont(itemFont);

        header.getChildren().addAll(nameHeader, headerSpacer, editHeader, removeHeader);
        contentBox.getChildren().add(header);

        // ===== List all cards =====
        List<Card> allCards = cardManager.getAllCards();
        for (Card c : allCards) {
            HBox row = createCardRow(c, contentBox, counterLabel);
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

    private HBox createCardRow(Card card, VBox contentBox, Label counterLabel) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #5bc0be; -fx-border-width: 0 0 1 0;");

        // ===== Card Text =====
        Label nameLabel = new Label(card.pol);
        nameLabel.setFont(itemFont);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(500);

        // Spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ===== Edit Button =====
        JFXButton editBtn = createIconButton("/icons/pencil.png");
        editBtn.setOnAction(e -> {
            Scene currentScene = stage.getScene();
            CardEditorScreen editor = new CardEditorScreen(stage, cardManager, sceneSwitcher, card, currentScene);
            sceneSwitcher.accept(new Scene(editor.getRoot(), 800, 600));
        });

        // ===== Remove Button =====
        JFXButton removeBtn = createIconButton("/icons/trash.png");
        removeBtn.setOnAction(e -> {
            ((ImageView) removeBtn.getGraphic()).setImage(new Image(getClass().getResource("/icons/trash_red.png").toExternalForm()));
            cardManager.removeCard(card.id);
            cardManager.syncToRepo();
            cardManager.syncImages();
            contentBox.getChildren().remove(row);
            counterLabel.setText("Fiszki: " + (contentBox.getChildren().size() - 1));
        });

        row.getChildren().addAll(nameLabel, spacer, editBtn, removeBtn);
        return row;
    }

    private JFXButton createIconButton(String iconPath) {
        JFXButton button = new JFXButton();
        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        button.setGraphic(icon);
        button.setRipplerFill(Color.LIGHTGRAY);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    public BorderPane getRoot() {
        return root;
    }
}
