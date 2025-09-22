package frontend;

import backend.Card;
import backend.CardManager;
import com.jfoenix.controls.JFXButton;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class ReviewScreen {

    private final BorderPane root;
    private final CardManager cardManager;
    private final Stage stage;
    private final Scene previousScene;

    private List<Card> reviewCards;
    private int currentIndex = 0;

    private final VBox centerBox;
    private final Text doneCounter;
    private final Text todoCounter;
    private final Text allCounter;
    private final Text currentCounter;

    private final Font font;

    private final Map<Card, Integer> sessionRatings = new HashMap<>();
    private final List<Card> retryCards = new ArrayList<>();

    private final HBox starsBox = new HBox(10); // persistent stars box
    private JFXButton checkBtn;
    private JFXButton nextBtn;
    private JFXButton prevBtn;

    public ReviewScreen(Stage stage, CardManager cardManager, Scene previousScene) {
        this.stage = stage;
        this.cardManager = cardManager;
        this.previousScene = previousScene;

        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#161b33"), CornerRadii.EMPTY, Insets.EMPTY)));

        font = Font.loadFont(getClass().getResource("/fonts/Montserrat-Regular.ttf").toExternalForm(), 24);

        reviewCards = new ArrayList<>(cardManager.getCardsForReview(30)); // Session limit

        doneCounter = new Text();
        todoCounter = new Text();
        allCounter = new Text();
        currentCounter = new Text();

        centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        root.setCenter(centerBox);

        starsBox.setAlignment(Pos.CENTER);

        setupTopBar();
        updateCardDisplay();
        setupKeyBindings();
    }

    private void setupTopBar() {
        ImageView backIcon = new ImageView(new Image(getClass().getResource("/icons/back.png").toExternalForm()));
        backIcon.setFitWidth(24);
        backIcon.setFitHeight(24);

        JFXButton backBtn = new JFXButton();
        backBtn.setGraphic(backIcon);
        backBtn.setStyle("-fx-background-color: transparent;");
        backBtn.setFocusTraversable(false);
        backBtn.setOnAction(e -> {
            sessionRatings.forEach((card, rating) -> cardManager.reviewCard(card.id, rating));
            stage.setScene(previousScene);
        });

        Font counterFont = Font.loadFont(getClass().getResource("/fonts/Montserrat-Regular.ttf").toExternalForm(), 22);
        Arrays.asList(doneCounter, todoCounter, allCounter, currentCounter)
              .forEach(t -> { t.setFont(counterFont); t.setFill(Color.WHITE); });

        HBox topBox = new HBox(30, backBtn, doneCounter, todoCounter, allCounter, currentCounter);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(15, 20, 15, 20));
        root.setTop(topBox);
    }

    private void updateCardDisplay() {
        centerBox.getChildren().clear();

        if (reviewCards.isEmpty()) {
            endSession();
            return;
        }

        if (currentIndex < 0) currentIndex = 0;
        if (currentIndex >= reviewCards.size()) currentIndex = reviewCards.size() - 1;

        Card currentCard = reviewCards.get(currentIndex);
        CardDisplay cardDisplay = new CardDisplay(currentCard, font);

        // Buttons
        prevBtn = createNavButton("Poprzedni");
        checkBtn = createNavButton("Sprawdz");
        nextBtn = createNavButton("Nastepny");

        HBox btnBox = new HBox(20, prevBtn, checkBtn, nextBtn);
        btnBox.setAlignment(Pos.CENTER);

        int previousRating = sessionRatings.getOrDefault(currentCard, 0);

        checkBtn.setOnAction(e -> {
            cardDisplay.revealTranslation();
            showStars();
        });

        prevBtn.setOnAction(e -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateCardDisplay();
            }
        });

        nextBtn.setOnAction(e -> {
            if (currentIndex < reviewCards.size() - 1) {
                currentIndex++;
                updateCardDisplay();
            }
        });

        centerBox.getChildren().addAll(cardDisplay, btnBox, starsBox);
        starsBox.getChildren().clear(); // clear stars for new card
        updateCounters();
    }

    private JFXButton createNavButton(String text) {
        JFXButton btn = new JFXButton(text);
        btn.setFont(font);
        btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        btn.setFocusTraversable(false);
        return btn;
    }

    private void showStars() {
    starsBox.getChildren().clear();

    Card currentCard = reviewCards.get(currentIndex);
    int currentRating = sessionRatings.getOrDefault(currentCard, 0);

    for (int i = 1; i <= 5; i++) {
        final int rating = i;
        boolean filled = i <= currentRating;
        ImageView starImg = new ImageView(new Image(getClass()
                .getResource(filled ? "/icons/star_filled.png" : "/icons/star.png").toExternalForm()));
        starImg.setFitWidth(40);
        starImg.setFitHeight(40);

        JFXButton starBtn = new JFXButton();
        starBtn.setGraphic(starImg);
        starBtn.setStyle("-fx-background-color: transparent;");
        starBtn.setFocusTraversable(false);

        starBtn.setOnAction(ev -> {
            sessionRatings.put(currentCard, rating);
            if (rating < 3 && !retryCards.contains(currentCard)) retryCards.add(currentCard);
            updateCounters();
            showStars(); // refresh stars immediately
            // auto-advance after short pause
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(e -> {
                currentIndex++;
                if (currentIndex >= reviewCards.size()) {
                    if (!retryCards.isEmpty()) {
                        Collections.shuffle(retryCards);
                        reviewCards = new ArrayList<>(retryCards);
                        retryCards.clear();
                        currentIndex = 0;
                        updateCardDisplay();
                    } else {
                        endSession();
                    }
                } else {
                    updateCardDisplay();
                }
            });
            pause.play();
        });

        starsBox.getChildren().add(starBtn);
    }
}


    private void rateCurrentCard(int rating) {
        Card currentCard = reviewCards.get(currentIndex);
        sessionRatings.put(currentCard, rating);

        if (rating < 3 && !retryCards.contains(currentCard))
            retryCards.add(currentCard);

        updateCounters();

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            currentIndex++;
            if (currentIndex >= reviewCards.size()) {
                if (!retryCards.isEmpty()) {
                    Collections.shuffle(retryCards);
                    reviewCards = new ArrayList<>(retryCards);
                    retryCards.clear();
                    currentIndex = 0;
                    updateCardDisplay();
                } else {
                    endSession();
                }
            } else {
                updateCardDisplay();
            }
        });
        pause.play();
    }

    private void updateCounters() {
        long doneCount = sessionRatings.values().stream().filter(r -> r > 0).count();
        int remaining = reviewCards.size() - currentIndex + retryCards.size();

        doneCounter.setText("Zrobione: " + doneCount);
        todoCounter.setText("Do Zrobienia: " + remaining);
        currentCounter.setText("Obecna: " + (currentIndex + 1));
        allCounter.setText("Wszystkie: " + reviewCards.size() +
                (retryCards.isEmpty() ? "" : " + " + retryCards.size()));
    }

    private void endSession() {
        sessionRatings.forEach((card, rating) -> cardManager.reviewCard(card.id, rating));

        centerBox.getChildren().clear();
        Text finished = new Text("Koniec!");
        finished.setFill(Color.WHITE);
        finished.setFont(font);
        centerBox.getChildren().add(finished);

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> stage.setScene(previousScene));
        pause.play();
    }

    private void setupKeyBindings() {
        root.setOnKeyPressed(event -> {
            if (reviewCards.isEmpty()) return;

            switch (event.getCode()) {
                case SPACE -> { if (checkBtn != null) checkBtn.fire(); event.consume(); }
                case RIGHT -> { if (nextBtn != null) nextBtn.fire(); event.consume(); }
                case LEFT -> { if (prevBtn != null) prevBtn.fire(); event.consume(); }
                case DIGIT1 -> rateCurrentCard(1);
                case DIGIT2 -> rateCurrentCard(2);
                case DIGIT3 -> rateCurrentCard(3);
                case DIGIT4 -> rateCurrentCard(4);
                case DIGIT5 -> rateCurrentCard(5);
            }
        });
    }

    public BorderPane getRoot() {
        return root;
    }
}
