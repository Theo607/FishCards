package backend;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class SRSService {

    private final String filePath = "loc/srs.csv";
    private final Map<Integer, CardSRS> srsMap = new HashMap<>();

    private static final int SESSION_LIMIT = 30; // Max cards per session
    private static final int NEW_CARD_LIMIT = 5; // Max new cards per session

    public SRSService() {
        try {
            loadSRS();
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, CardSRS> getSrsMap() {
        return srsMap;
    }

    public void loadSRS() throws IOException, CsvValidationException {
        srsMap.clear();
        File file = new File(filePath);
        if (!file.exists())
            return;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] row;
            boolean firstLine = true;
            while ((row = reader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                int cardId = Integer.parseInt(row[0]);
                int repetitions = Integer.parseInt(row[1]);
                double easeFactor = Double.parseDouble(row[2]);
                int interval = Integer.parseInt(row[3]);
                LocalDate nextReview = LocalDate.parse(row[4]);

                CardSRS srs = new CardSRS(cardId);
                srs.repetitions = repetitions;
                srs.easeFactor = easeFactor;
                srs.interval = interval;
                srs.nextReview = nextReview;

                srsMap.put(cardId, srs);
            }
        }
    }

    public void saveSRS() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(new String[] { "cardId", "repetitions", "easeFactor", "interval", "nextReview" });
            for (CardSRS srs : srsMap.values()) {
                writer.writeNext(new String[] {
                        String.valueOf(srs.cardId),
                        String.valueOf(srs.repetitions),
                        String.valueOf(srs.easeFactor),
                        String.valueOf(srs.interval),
                        srs.nextReview.toString()
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CardSRS getSRSForCard(int cardId) {
        return srsMap.computeIfAbsent(cardId, CardSRS::new);
    }

    public void removeSRS(int cardId) {
        srsMap.remove(cardId);
        saveSRS();
    }

    public void cardReset(int cardId) {
        CardSRS srs = getSRSForCard(cardId);
        srs.repetitions = 0;
        srs.easeFactor = 2.5;
        srs.interval = 1;
        srs.nextReview = LocalDate.now();
        saveSRS();
    }

    public void reviewCard(int cardId, int quality) {
        CardSRS srs = getSRSForCard(cardId);
        srs.updateSRS(quality);
        saveSRS();
    }

    /**
     * Return cards for review, prioritizing due and new cards, randomized slightly
     */

    public List<CardSRS> getDueCards() {
        List<CardSRS> sessionList = new ArrayList<>();
        int newCardCount = 0;

        // Step 1: Collect new cards first (never reviewed)
        List<CardSRS> newCards = new ArrayList<>();
        for (CardSRS srs : srsMap.values()) {
            if (!srs.hasBeenReviewed()) {
                newCards.add(srs);
            }
        }
        Collections.shuffle(newCards); // randomize new cards

        int halfSession = SESSION_LIMIT / 2;
        for (CardSRS srs : newCards) {
            if (newCardCount >= halfSession || sessionList.size() >= SESSION_LIMIT)
                break;
            sessionList.add(srs);
            newCardCount++;
        }

        // Step 2: Collect due cards (already reviewed and due)
        List<CardSRS> dueCards = new ArrayList<>();
        for (CardSRS srs : srsMap.values()) {
            if (srs.hasBeenReviewed() && srs.isDue() && !sessionList.contains(srs)) {
                dueCards.add(srs);
            }
        }
        // Sort due cards by nextReview ascending
        dueCards.sort(Comparator.comparing(srs -> srs.nextReview));

        for (CardSRS srs : dueCards) {
            if (sessionList.size() >= SESSION_LIMIT)
                break;
            sessionList.add(srs);
        }

        // Step 3: Optional minor shuffle to avoid strict repetition
        Collections.shuffle(sessionList);

        return sessionList;
    }

    public boolean validateSRSFile(File file) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] row;
            boolean firstLine = true;
            while ((row = reader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (row.length != 5)
                    return false;
                try {
                    Integer.parseInt(row[0]);
                    Integer.parseInt(row[1]);
                    Double.parseDouble(row[2]);
                    Integer.parseInt(row[3]);
                    LocalDate.parse(row[4]);
                } catch (Exception e) {
                    return false;
                }
            }
        } catch (CsvValidationException e) {
            return false;
        }
        return true;
    }
}
