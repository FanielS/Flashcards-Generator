package flashcards;

import java.io.*;
import java.util.*;

/**
 * This program is used to generate, save and manage flashcards.
 *
 * @author Faniel S. Abraham
 * @version 1.0
 * @since 2022-06-03
 */

public class Main {
    static LinkedHashMap<String, String> cards = new LinkedHashMap<>();
    static LinkedHashMap<String, String> checkerMap = new LinkedHashMap<>();
    static LinkedHashMap<String, Integer> mistakesMap = new LinkedHashMap<>();
    static List<String> log = new ArrayList<>();
    static String importFile;
    static String exportFile;


    public static void main(String[] args) {
        List<String> commandArgs = new ArrayList<>(Arrays.asList(args));

        //if -import argument is passed, do importing implicitly
        if (commandArgs.contains("-import")) {
            importFile = commandArgs.get(commandArgs.indexOf("-import") + 1);
            importCards(importFile);
        }

        //if -export argument is passed, do exporting implicitly
        if (commandArgs.contains("-export")) {
            exportFile = commandArgs.get(commandArgs.indexOf("-export") + 1);
        }

        boolean flag = true;
        while (flag) {
            output("\nInput the action (add, remove, ask, import, export, log, hardest card, reset stats, exit):");
            String option = input().toLowerCase();

            switch (option) {
                case "add":
                    add();
                    break;

                case "remove":
                    remove();
                    break;

                case "import":
                    importCards("prompt from user");
                    break;

                case "export":
                    export("prompt from user");
                    break;

                case "ask":
                    ask();
                    break;

                case "log":
                    log();
                    break;

                case "hardest card":
                    hardestCard();
                    break;

                case "reset stats":
                    reset();
                    break;

                default:
                    flag = false;
                    export(exportFile);
                    System.out.println("Bye bye!");
                    break;
            }
        }
    }

    /**
     * This method is used to add new cards from user.
     */
    static void add() {
        output("The card:");
        String term = input();
        if (cards.containsKey(term)) {
            output(String.format("The card \"%s\" already exists.\n", term));
        } else {
            output("The definition of the card:");
            String definition = input();
            if (cards.containsValue(definition)) {
                output(String.format("The definition \"%s\" already exists.\n", definition));
            } else {
                cards.put(term, definition);
                checkerMap.put(definition, term);
                output(String.format("The pair (\"%s\":\"%s\") has been added.\n", term, definition));
            }
        }
    }

    /**
     * This method is used to remove cards.
     */
    static void remove() {
        output("Which card?");
        String term = input();
        if (cards.containsKey(term)) {
            String definition = cards.get(term);
            cards.remove(term);
            mistakesMap.remove(term);
            checkerMap.remove(definition);   //definition is the key in checkerMap
            output("The card has been removed.");
        } else {
            output(String.format("Can't remove \"%s\": there is no such card.\n", term));
        }
    }

    /**
     * This method is used to import cards from a file.
     * @param fileName - represents one of the below two cases.
     *                 a) "prompt from user" is passed from main menu to suggest file name is inputted from user
     *                 b) file name is acquired from command arguments and passed implicitly without user interference.
     */
    static void importCards(String fileName) {
        if (fileName.equals("prompt from user")) {
            output("File Name:");
            fileName = input();
        }
        File file = new File(fileName);

        try (Scanner fScanner = new Scanner(file)) {
            int count = 0;
            while (fScanner.hasNextLine()) {
                String[] line = fScanner.nextLine().split(",");
                log.add(Arrays.toString(line));

                //if same card is already in memory, it should be replaced by the card from file.
                if (cards.containsKey(line[0])) {
                    cards.replace(line[0], line[1]);
                    checkerMap.replace(line[1], line[0]);
                    mistakesMap.replace(line[0], Integer.parseInt(line[2]));
                } else {
                    cards.put(line[0], line[1]);
                    checkerMap.put(line[1], line[0]);
                    mistakesMap.put(line[0], Integer.parseInt(line[2]));
                }
                count++;
            }
            output(String.format("%d cards have been loaded.\n", count));

        } catch (FileNotFoundException e) {
            output("File not found.");
        }
    }

    /**
     * This method is used to export data to a file.
     * @param fileName - represents one of the below three cases.
     *                 a) "prompt from user" is passed from main menu to suggest file name is inputted from user
     *                 b) file name is acquired from command arguments and passed implicitly without user interference.
     *                 c) null is passed to suggest don't export anything when called implicitly (i.e. when it's not case a & b)
     */
    static void export(String fileName) {
        if (fileName == null) {
            return;
        } else if (fileName.equals("prompt from user")) {
            output("File Name:");
            fileName = input();
        }
        File file = new File(fileName);

        try (PrintWriter writer = new PrintWriter(file)) {
            for (var card : cards.entrySet()) {
                writer.println(card.getKey() + "," + card.getValue() + "," + mistakesMap.getOrDefault(card.getKey(), 0));
                log.add(card.getKey() + "," + card.getValue() + "," + mistakesMap.getOrDefault(card.getKey(), 0));
            }
            output(String.format("%d cards have been saved.", cards.size()));
        } catch (Exception e) {
            output("Something went wrong!");
        }
    }

    /**
     * This method is used to ask a user about their knowledge of the cards.
     */
    static void ask() {
        output("How many times to ask?");
        int n = Integer.parseInt(input());
        int count = 0;

        for (var card : cards.entrySet()) {
            output(String.format("Print the definition of \"%s\":\n", card.getKey()));
            String definition = input();
            if (card.getValue().equals(definition)) {
                output("Correct!");
            } else if (cards.containsValue(definition)) {
                output(String.format("Wrong. The right answer is \"%s\", but your definition " + "is correct for \"%s\".\n", card.getValue(), checkerMap.get(definition)));
                mistakesMap.put(card.getKey(), mistakesMap.getOrDefault(card.getKey(), 0) + 1);
            } else {
                output(String.format("Wrong. The right answer is \"%s\".\n", card.getValue()));
                mistakesMap.put(card.getKey(), mistakesMap.getOrDefault(card.getKey(), 0) + 1);
            }
            count++;
            if (count == n) {
                break;
            }
        }
    }

    /**
     * This method is used to print to standard output and printed data to log file.
     * @param output - anything that needs to be printed.
     */
    static void output(String output) {
        System.out.println(output);
        log.add(output);
    }

    /**
     * This method is used to read data from standard input and record it in log-file.
     * @return - the data that was read.
     */
    static String input() {
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        log.add(input);
        return input;
    }

    /**
     * This method is used to record any input and output activity by the program.
     */
    static void log() {
        output("File name:");
        String fileName = input();

        try (PrintWriter writer = new PrintWriter(fileName)) {
            for (String activity : log) {
                writer.println(activity);
            }
            System.out.println("The log has been saved.");
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }

    /**
     * This method is used to reset the statistics to zero. The statistics is the number of
     * mistakes make by the user for each card.
     */
    static void reset() {
        mistakesMap.clear();
        output("Card statistics have been reset.");
    }

    /**
     * Ths method finds the hardest cards based on the number of errors made in answering them.
     */
    static void hardestCard() {
        if (mistakesMap.isEmpty()) {
            output("There are no cards with errors.");
            return;
        }

        List<String> maxKeys = new ArrayList<>();
        int maxError = Collections.max(mistakesMap.values());

        for (var card : mistakesMap.entrySet()) {
            if (card.getValue() == maxError) {
                maxKeys.add(String.format("\"%s\"", card.getKey()));
            }
        }
        if (maxKeys.size() == 1) {
            output(String.format("The hardest card is \"%s\". You have %d errors answering it.", maxKeys.get(0), maxError));
        } else {
            String terms = String.join(", ", maxKeys);
            output(String.format("The hardest cards are %s. You have %d errors answering them.", terms, maxError));
        }
    }
}

