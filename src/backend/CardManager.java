package backend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullResult;

public class CardManager {

    public SRSService srsService = new SRSService();
    public Credentials cred = new Credentials();
    public GitManager man = new GitManager();
    public CSVService csvService = new CSVService();
    public ImageService imgService = new ImageService();

    public CardManager() {
    }

    /** Ensure repo and loc folder exists */
    public void ensureFS() {
        try {
            String username = cred.getUsername();
            String password = cred.getToken();
            String link = cred.getLink();

            File repoFolder = new File("repo");

            if (repoFolder.exists() && repoFolder.isDirectory()) {
                File gitDir = new File(repoFolder, ".git");
                if (gitDir.exists()) {
                    man.openRepository("repo");
                    man.pull(username, password);
                } else {
                    deleteFolder(repoFolder);
                    man.cloneRepository(link, "repo", username, password);
                }
            } else {
                man.cloneRepository(link, "repo", username, password);
            }

            // Ensure loc folder and SRS CSV exist
            File locFolder = new File("loc");
            if (!locFolder.exists())
                locFolder.mkdirs();

            File srsFile = new File("loc/srs.csv");
            if (!srsFile.exists()) {
                if (srsFile.createNewFile()) {
                    try (FileWriter fw = new FileWriter(srsFile)) {
                        fw.write("cardId,repetitions,easeFactor,interval,nextReview\n");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                deleteFolder(f);
            }
        }
        folder.delete();
    }

    /** Add card with image */
    public void addCardWithImage(Card c, File imageFile) {
        if (imageFile != null) {
            try {
                c.imgp = imgService.saveImage(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        addCard(c);
    }

    /** Add a card and initialize its SRS */
    public void addCard(Card c) {
        if (c.id <= 0) {
            c.id = getNextCardId();
        }

        csvService.appendCard(c);

        // Only create SRS if it doesn't exist yet
        if (!srsService.getSrsMap().containsKey(c.id)) {
            srsService.getSRSForCard(c.id);
            srsService.saveSRS();
        }
    }

    /** Update card fields */
    public void updateCard(int cardId, String newPol, String newEsp, String newFP) {
        Card c = getCard(cardId);
        if (c == null)
            return;

        if ((newPol != null && !newPol.equals(c.pol)) || (newEsp != null && !newEsp.equals(c.esp))) {
            srsService.cardReset(cardId);
        }

        if (newPol != null)
            c.pol = newPol;
        if (newEsp != null)
            c.esp = newEsp;
        if (newFP != null && !newFP.isEmpty())
            c.imgp = newFP; // don't overwrite empty

        csvService.updateCard(c);
        srsService.getSRSForCard(c.id); // ensure SRS exists
        srsService.saveSRS();
    }

    /** Remove a card and its SRS */
    /** Remove a card and its SRS */
    public void removeCard(int cardId) {
        Card c = getCard(cardId);
        if (c == null)
            return;

        // Delete image if exists
        if (c.imgp != null && !c.imgp.isEmpty()) {
            File imgFile = new File("repo", c.imgp);
            if (imgFile.exists())
                imgFile.delete();
        }

        // Delete card from CSV without reassigning IDs
        csvService.deleteCard(cardId);

        // Remove SRS
        srsService.removeSRS(cardId);
    }

    /** Get card by ID */
    public Card getCard(int cardId) {
        return csvService.accessCard(cardId);
    }

    /** Get all cards as list */
    public List<Card> getAllCards() {
        Card[] cards = csvService.readData();
        return cards != null ? Arrays.asList(cards) : new ArrayList<>();
    }

    private void mergeCSVConflicts() {
        try {
            // 1. Load local CSV (current in-memory cards)
            List<Card> localCards = getAllCards();

            // 2. Load remote/conflicted CSV file (repo/data.csv)
            File conflictedCSV = new File("repo/data.csv");
            if (!conflictedCSV.exists())
                return;

            List<Card> remoteCards = new ArrayList<>();
            try (Scanner sc = new Scanner(conflictedCSV)) {
                boolean firstLine = true;
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    } // skip header
                    if (line.startsWith("<<<<<<<") || line.startsWith("=======") || line.startsWith(">>>>>>>"))
                        continue; // skip Git conflict markers

                    String[] parts = line.split(",", -1);
                    if (parts.length < 4)
                        continue;
                    try {
                        int id = Integer.parseInt(parts[0]);
                        remoteCards.add(new Card(id, parts[1], parts[2], parts[3]));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // 3. Merge by card ID: local wins if IDs overlap
            Map<Integer, Card> merged = new HashMap<>();
            for (Card c : remoteCards)
                merged.put(c.id, c);
            for (Card c : localCards)
                merged.put(c.id, c);

            // 4. Sort by ID for consistent CSV order
            List<Card> sorted = new ArrayList<>(merged.values());
            sorted.sort(Comparator.comparingInt(c -> c.id));

            // 5. Rewrite CSV
            csvService.clearCSV();
            for (Card c : sorted)
                csvService.appendCard(c);

            System.out.println("CSV conflicts merged successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get cards for review up to limit, no duplicates */
    public List<Card> getCardsForReview(int limit) {
        List<Card> allCards = getAllCards();
        List<Card> reviewCards = new ArrayList<>();
        Set<Integer> addedIds = new HashSet<>();

        // 1. Get due cards from SRS
        List<CardSRS> dueSRS = srsService.getDueCards();
        List<Card> dueCards = new ArrayList<>();
        Map<Integer, Card> allCardsMap = new HashMap<>();
        for (Card c : allCards)
            allCardsMap.put(c.id, c);

        for (CardSRS srs : dueSRS) {
            Card c = allCardsMap.get(srs.cardId);
            if (c != null)
                dueCards.add(c);
        }
        Collections.shuffle(dueCards); // randomize order of due cards
        for (Card c : dueCards) {
            if (reviewCards.size() >= limit)
                break;
            if (addedIds.add(c.id))
                reviewCards.add(c);
        }

        // 2. Add new cards (not yet in SRS)
        List<Card> newCards = new ArrayList<>();
        for (Card c : allCards) {
            if (!srsService.getSrsMap().containsKey(c.id))
                newCards.add(c);
        }
        Collections.shuffle(newCards); // randomize new cards
        for (Card c : newCards) {
            if (reviewCards.size() >= limit)
                break;
            if (addedIds.add(c.id))
                reviewCards.add(c);
        }

        // 3. Fill with any remaining cards (if limit not reached)
        List<Card> remainingCards = new ArrayList<>(allCards);
        Collections.shuffle(remainingCards); // randomize remaining
        for (Card c : remainingCards) {
            if (reviewCards.size() >= limit)
                break;
            if (addedIds.add(c.id))
                reviewCards.add(c);
        }

        return reviewCards;
    }

    // Inside CardManager class
    /**
     * Validate an SRS CSV file format before loading it.
     * 
     * @param file The file to validate
     * @return true if valid, false otherwise
     * @throws IOException if the file cannot be read
     */
    public boolean validateSRSFile(File file) throws IOException {
        return srsService.validateSRSFile(file);
    }

    /** Sync CSV to Git repo */
    public void syncToRepo() {
        try {
            String username = cred.getUsername();
            String password = cred.getToken();
            man.openRepository("repo");

            // 1. Pull latest changes
            PullResult pullResult = man.pull(username, password);

            // 2. If conflicts detected, auto-merge CSV
            if (pullResult != null && pullResult.getMergeResult() != null &&
                    pullResult.getMergeResult().getMergeStatus() == MergeStatus.CONFLICTING) {
                System.out.println("Merge conflicts detected. Auto-merging CSV...");
                mergeCSVConflicts();
            }

            // 3. Commit local changes (including auto-merged CSV)
            man.commitFiles(List.of("data.csv"), "sync cards CSV");

            // 4. Push to remote
            man.push(username, password);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Sync images to Git repo */
    public void syncImages() {
        try {
            String username = cred.getUsername();
            String password = cred.getToken();
            man.openRepository("repo");

            List<Card> allCards = getAllCards();
            List<String> filesToCommit = new ArrayList<>();

            for (Card c : allCards) {
                if (c.imgp != null && !c.imgp.isEmpty()) {
                    File imgFile = new File("repo", c.imgp);
                    if (imgFile.exists())
                        filesToCommit.add(c.imgp);
                }
            }

            if (!filesToCommit.isEmpty()) {
                man.commitFiles(filesToCommit, "sync images");
                man.push(username, password);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Review a card and update SRS */
    public void reviewCard(int cardId, int quality) {
        srsService.reviewCard(cardId, quality);
        srsService.saveSRS();
    }

    /** Get next available card ID */
    public int getNextCardId() {
        return getAllCards().stream().mapToInt(c -> c.id).max().orElse(0) + 1;
    }
}
