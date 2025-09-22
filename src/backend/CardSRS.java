package backend;

import java.time.LocalDate;

public class CardSRS {
    public int cardId; // reference to Card.id
    public int repetitions; // number of successful reviews
    public double easeFactor; // ease factor (default 2.5)
    public int interval; // interval in days
    public LocalDate nextReview;

    public CardSRS(int cardId) {
        this.cardId = cardId;
        this.repetitions = 0;
        this.easeFactor = 2.5;
        this.interval = 1;
        this.nextReview = LocalDate.now();
    }

    public boolean hasBeenReviewed() {
        return repetitions > 0;
    }

    /**
     * Update SRS fields using SM-2 algorithm
     * 
     * @param quality - user recall rating (0-5)
     */
    public void updateSRS(int quality) {
        if (quality < 3) {
            repetitions = 0;
            interval = 1;
        } else {
            repetitions += 1;
            easeFactor = Math.max(1.3, easeFactor + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            if (repetitions == 1)
                interval = 1;
            else if (repetitions == 2)
                interval = 6;
            else
                interval = (int) Math.round(interval * easeFactor);
        }
        nextReview = LocalDate.now().plusDays(interval);
    }

    /**
     * Check if card is due for review today
     */
    public boolean isDue() {
        return !nextReview.isAfter(LocalDate.now());
    }
}
