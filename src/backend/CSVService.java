package backend;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import com.opencsv.exceptions.CsvValidationException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;


public class CSVService {
    private String filePath = "repo/data.csv";

    public void clearCSV() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write only the header row
            writer.writeNext(new String[] { "id", "pol", "esp", "imgp" });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendCard(Card c) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, true))) { // true = append mode
            String[] row = {
                    String.valueOf(c.id),
                    c.pol,
                    c.esp,
                    c.imgp
            };
            writer.writeNext(row);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Card[] readData() {
        List<Card> cards = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] row;
            boolean firstLine = true;

            while ((row = reader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (row[0].equalsIgnoreCase("id"))
                        continue;
                }

                int id = Integer.parseInt(row[0]);
                String pol = row[1];
                String esp = row[2];
                String imgp = row[3];

                Card card = new Card(id, pol, esp, imgp);
                cards.add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }

        return cards.toArray(new Card[0]);
    }

    public void deleteCard(int idToDelete) {
        List<Card> cards = new ArrayList<>(Arrays.asList(readData()));
        boolean found = false;

        for (Iterator<Card> it = cards.iterator(); it.hasNext();) {
            Card c = it.next();
            if (c.id == idToDelete) {
                it.remove();
                found = true;
                break;
            }
        }

        if (!found)
            return;

        // Write remaining cards back to CSV without changing IDs
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(new String[] { "id", "pol", "esp", "imgp" });
            for (Card c : cards) {
                writer.writeNext(new String[] { String.valueOf(c.id), c.pol, c.esp, c.imgp != null ? c.imgp : "" });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Card accessCard(int idToFind) {
        Card[] cards = readData(); // reuse your existing readData() function
        for (Card c : cards) {
            if (c.id == idToFind) {
                return c; // found, return the card
            }
        }
        return null; // not found
    }

    public void updateCard(Card updatedCard) {
        List<Card> cards = new ArrayList<>(Arrays.asList(readData()));
        boolean found = false;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).id == updatedCard.id) {
                cards.set(i, updatedCard);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Card with id " + updatedCard.id + " not found!");
            return;
        }
        writeAllCards(cards);
    }

    private void writeAllCards(List<Card> cards) {
        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write("id,pol,esp,imgp\n"); // header
            for (Card c : cards) {
                fw.write(c.id + "," + c.pol + "," + c.esp + "," + (c.imgp != null ? c.imgp : "") + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
