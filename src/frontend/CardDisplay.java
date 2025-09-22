package frontend;

import java.io.File;

import backend.Card;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.Label;

public class CardDisplay extends VBox {

    private Label polLabel;
    private Label espLabel;
    private ImageView imageView;

    public CardDisplay(Card card, Font font) {
        setAlignment(Pos.CENTER);
        setSpacing(15);

        Image img;

        // Safely load card image or fallback to null.png
        if (card.imgp != null && !card.imgp.isEmpty()) {
            File imgFile = new File("repo/" + card.imgp);
            if (imgFile.exists()) {
                img = new Image(imgFile.toURI().toString());
            } else {
                img = new Image(getClass().getResource("/icons/null.png").toExternalForm());
            }
        } else {
            img = new Image(getClass().getResource("/icons/null.png").toExternalForm());
        }

        imageView = new ImageView(img);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Polish word
        polLabel = new Label(card.pol != null ? card.pol : "");
        polLabel.setFont(font);
        polLabel.setTextFill(Color.WHITE);

        // Spanish word (hidden initially)
        espLabel = new Label(card.esp != null ? card.esp : "");
        espLabel.setFont(font);
        espLabel.setTextFill(Color.WHITE);
        espLabel.setVisible(false);

        getChildren().addAll(imageView, polLabel, espLabel);
    }

    public void revealTranslation() {
        polLabel.setVisible(false);
        espLabel.setVisible(true);
    }
}
