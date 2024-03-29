import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// References are marked with 'REF' keywords

/**
 * Store data from a glossary file.
 */
public class Glossary {
    public String path;
    private TreeMap<String, String[]> data = new TreeMap<String, String[]>();
    private TreeMap<Integer, String> data_id = new TreeMap<Integer, String>();
    private LinkedHashMap<Integer, String> search_history = new LinkedHashMap<Integer, String>();
    public Boolean modified = false;

    /**
     * Constructor to get file path and read glossary data.
     * 
     * @param path the path of the glossary file
     */
    public Glossary(String path) {
        this.path = path.replace('\\', '/');
        try {
            Read();
        } catch (IOException e) {
            System.out.println("(!) Error reading file.");
        }
    }

    /**
     * Read data from specified path using streams. The method will save the data
     * into a TreeMap&ltString, String[]&gt, with each represents the <i>
     * keyword</i> and the <i>definition</i>.
     * 
     * @throws IOException
     */
    public void Read() throws IOException {
        // Check for a csv file
        String csvpath = getFileName() + ".csv";
        File file = new File(csvpath);
        if (file.exists()) {
            ReadCsv(csvpath);
            return;
        }
        // If a csv file doesn't exist, read from user's file and create one
        System.out.println("(@) Reading from '" + path + "'...");
        FileInputStream fis = new FileInputStream(path);
        Scanner s = new Scanner(fis, "UTF-8");
        Integer id = 0;
        if (s.hasNextLine()) {
            // Skip columns name
            s.nextLine();
        }
        while (s.hasNextLine()) {
            // Split the keyword and definition by the symbol '`'
            String line = s.nextLine();
            String[] sec = line.split("`", 2);
            sec[0] = sec[0].trim(); // Remove whitespaces
            if (sec.length >= 2) {
                // If there is a '`', split multiple meanings by symbol '|'
                String[] sec1 = sec[1].split("\\|");
                for (int i = 0; i < sec1.length; i++) {
                    sec1[i] = sec1[i].trim(); // Remove whitespaces
                }
                data.put(sec[0], sec1);
                data_id.put(id++, sec[0]);
            } else if (sec.length == 1) {
                // If not, it's another definition of the above keyword
                try {
                    // Split multiple meanings and joins the ones added before
                    // REF: https://www.javatpoint.com/how-to-merge-two-arrays-in-java
                    String[] extArray = sec[0].split("\\|");

                    String last = data.lastKey();
                    int lastLength = data.get(last).length, extLength = extArray.length;

                    String[] lastValue = data.get(last), newValue = new String[lastLength + extLength];
                    System.arraycopy(lastValue, 0, newValue, 0, lastLength);
                    System.arraycopy(extArray, 0, newValue, lastLength, extLength);

                    data.replace(last, newValue);
                } catch (NoSuchElementException e) {
                    // Start of file is a definition of unknown keyword => Ignore
                    System.out.println("(!) Start of file has unknown texts, ignored.");
                }
            }
        }
        if (fis != null) {
            fis.close();
        }
        if (s != null) {
            s.close();
        }
        Write();
        System.out.println("(i) Done.\n");
    }

    /**
     * Read data from existing csv file, instead of the user's file.
     * 
     * @param csvpath path to the csv file
     * @throws IOException
     */
    private void ReadCsv(String csvpath) throws IOException {
        System.out.println("(@) Reading from '" + csvpath + "'...");
        FileInputStream fis = new FileInputStream(csvpath);
        Scanner s = new Scanner(fis, "UTF-8");
        Integer id = 0;
        if (s.hasNextLine()) {
            // Skip columns name
            s.nextLine();
        }
        while (s.hasNextLine()) {
            // Split the keyword and definition by the symbol ','
            String line = s.nextLine();
            String[] sec = line.split(",", 2);
            // Split multiple meanings by symbol '|'
            data.put(sec[0], sec[1].split("\\|"));
            data_id.put(id++, sec[0]);
        }
        if (fis != null) {
            fis.close();
        }
        if (s != null) {
            s.close();
        }
        System.out.println("(i) Done.\n");
    }

    /**
     * Write the TreeMap into a csv file for faster loading in the future. The name
     * of the csv file will be the same as the input file.
     * 
     * @throws IOException
     */
    public void Write() throws IOException {
        String csvpath = getFileName() + ".csv";
        File file = new File(csvpath);
        System.out.println("(@) Writing to '" + csvpath + "'...");
        FileWriter fw = null;
        if (file.createNewFile()) {
            System.out.println("(i) Created '" + csvpath + "'.");
            fw = new FileWriter(file);
            fw.write("Keyword,Definition\n");
        }
        if (fw == null) {
            fw = new FileWriter(file);
        }
        // REF: https://www.geeksforgeeks.org/how-to-iterate-over-a-treemap-in-java/
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            fw.write(entry.getKey() + ",");
            String[] values = entry.getValue();
            for (String str : values) {
                fw.write(str + "|");
            }
            fw.write("\n");
        }
        if (fw != null) {
            fw.close();
        }
    }

    /**
     * Print everything in the glossary.
     */
    public void Print() {
        Print(null);
    }

    /**
     * Print everything in the specified sub-glossary.
     * 
     * @param map the TreeMap to be printed
     */
    private void Print(TreeMap<String, String[]> map) {
        if (map == null) {
            System.out.println("(i) Printing content of Glossary...");
            map = data;
        }
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            String[] values = entry.getValue();
            for (String str : values) {
                System.out.print(str + " || ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Get the path of the input file, discarding its extension.
     * 
     * @return the path leading to the input file, without the extension
     */
    private String getFileName() {
        // REF:
        // https://stackoverflow.com/questions/924394/how-to-get-the-filename-without-the-extension-in-java
        return path.replaceFirst("[.][^.]+$", "");
    }

    /**
     * Search for the exact keyword in the data (case-insensitive).
     * 
     * @param term search term
     * @return a TreeMap of all found results
     */
    public TreeMap<String, String[]> SearchKeyword(String term) {
        // Add to search history
        if (!search_history.containsKey(0) && !search_history.containsValue(term)) {
            search_history.put(0, term);
        }
        TreeMap<String, String[]> results = new TreeMap<String, String[]>();
        System.out.println("(@) Searching for " + term + " as keyword...");
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(term)) {
                continue;
            }
            results.put(entry.getKey(), entry.getValue());
        }
        System.out.println("(i) Done.");
        if (results.isEmpty()) {
            System.out.println("(!) Found no results.\n");
        } else {
            System.out.println("(i) The following results are found:");
            Print(results);
        }
        WriteSearchHistory();
        return results;
    }

    /**
     * Search for a term in the definition in the data (case-insensitive).
     * 
     * @param term search term
     * @return a TreeMap of all found results
     */
    public TreeMap<String, String[]> SearchDefinition(String term) {
        // Add to search history
        if (!search_history.containsKey(1) && !search_history.containsValue(term)) {
            search_history.put(1, term);
        }
        TreeMap<String, String[]> results = new TreeMap<String, String[]>();
        System.out.println("(@) Searching for " + term + " as definition...");
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            String[] values = entry.getValue();
            Boolean found = false;
            for (String str : values) {
                if (str.toLowerCase().contains(term.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Current entry doesn't contain the search phrase => Skip
                continue;
            }
            results.put(entry.getKey(), entry.getValue());
        }
        System.out.println("(i) Done.");
        if (results.isEmpty()) {
            System.out.println("(!) Found no results.");
        } else {
            System.out.println("(i) The following results are found:");
            Print(results);
        }
        WriteSearchHistory();
        return results;
    }

    /**
     * Get search history from a file. The file will have the same name as the
     * glossary file, with the extension ".hist.csv". The variable search_history is
     * a LinkedHashMap (it'll retain inserted order). Each entry is an Integer (0:
     * keyword search, 1: definition search) and a String (search term).
     */
    public void ReadSearchHistory() {
        System.out.println("(@) Getting search history...");
        String csvpath = getFileName() + ".hist.csv";
        File file = new File(csvpath);
        if (!file.exists()) {
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            Scanner s = new Scanner(fis, "UTF-8");
            if (s.hasNextLine()) {
                // Skip columns name
                s.nextLine();
            }
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] sec = line.split(",", 2);
                search_history.put(Integer.parseInt(sec[0]), sec[1]);

            }
            if (fis != null) {
                fis.close();
            }
            if (s != null) {
                s.close();
            }
        } catch (IOException e) {
            System.out.println("(!) Error reading history file.");
        }
    }

    /**
     * Write all search terms entered during this session into a history file. If
     * the file is missing, it will be created.
     */
    public void WriteSearchHistory() {
        String csvpath = getFileName() + ".hist.csv";
        File file = new File(csvpath);
        FileWriter fw = null;
        try {
            if (file.createNewFile()) {
                // If history file not found, create new with 2 columns: Code and Term
                fw = new FileWriter(file);
                fw.write("Code,Term\n");
                if (fw != null) {
                    fw.close();
                }
            }
            fw = new FileWriter(file, true);
            for (Map.Entry<Integer, String> entry : search_history.entrySet()) {
                fw.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            if (fw != null) {
                fw.close();
            }
        } catch (IOException e) {
            System.out.println("(!) Error writing history file.");
        }
    }

    /**
     * Print search history to console.
     */
    public void PrintSearchHistory() {
        System.out.println("(i) Printing search history...");
        for (Map.Entry<Integer, String> entry : search_history.entrySet()) {
            String type = entry.getKey() == 0 ? "keyword" : "definition";
            System.out.print("By " + type + ": ");
            System.out.println(entry.getValue());
        }
        System.out.println();
    }

    /**
     * Add a new slang word to glossary. If an entry exist, ask the user to decide
     * whether they want to overwrite or append the definition.
     * 
     * @param key keyword to add to glossary
     * @param def definition for that keyword
     */
    public void AddSlang(String key, String def) {
        Boolean added = false, exist = false;
        // Enter data (if no args given)
        if (key == "" && def == "") {
            System.out.println("(?) Enter keyword...");
            System.out.print(" > ");
            key = Main.sc.nextLine();
            System.out.println("(?) Enter definition...");
            System.out.print(" > ");
            def = Main.sc.nextLine();
        }
        // Check existing
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            if (entry.getKey().equals(key)) {
                exist = true;
                String option = "";
                System.out.print("(?) Found an existing entry '" + entry.getKey() + "': ");
                for (String str : entry.getValue()) {
                    System.out.print(str + " || ");
                }
                System.out.println();
                System.out.println("(?) Do you want to overwrite? (y/N/a/?)");
                do {
                    System.out.print(" > ");
                    option = Main.sc.nextLine();
                    switch (option) {
                        case "yes":
                        case "y":
                            // Overwrite the entry
                            data.replace(entry.getKey(), new String[] { def });
                            added = true;
                            break;

                        case "no":
                        case "n":
                        case "":
                            System.out.println("(i) Adding cancelled.");
                            break;

                        case "append":
                        case "a":
                            // Append new definition to keyword (Duplicate slang word)
                            String[] oldVal = entry.getValue(), newVal = new String[oldVal.length + 1];
                            System.arraycopy(oldVal, 0, newVal, 0, oldVal.length);
                            newVal[oldVal.length] = def;
                            data.replace(entry.getKey(), newVal);
                            added = true;
                            break;

                        case "help":
                        case "h":
                        case "?":
                            System.out.println(
                                    "(i) Options: y = yes, n = no (default), a = append definition, ? = show this help.");
                            break;

                        default:
                            System.out.println("(!) Unknown option '" + option + "'.");
                            option = "?";
                            break;
                    }
                } while (option == "?");
                break;
            }
        }
        if (added) {
            System.out.println("(i) Slang word updated to glossary.");
            modified = true;
        } else if (!exist) {
            data.put(key, new String[] { def });
            System.out.println("(i) Slang word added to glossary.");
            modified = true;
        }
        System.out.println();
    }

    /**
     * Edit the definitions of a slang word if the provided keyword existed. The
     * program will enter edit mode for the keyword, the user wil be able to change
     * a definition, or delete it.
     * 
     * @param key the keyword existing in the glossary
     */
    public void EditSlang(String key) {
        Boolean exist = false;
        // Enter data (if no args given)
        if (key == "") {
            System.out.println("(?) Enter keyword...");
            System.out.print(" > ");
            key = Main.sc.nextLine();
        }
        // Check existing
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            if (entry.getKey().equals(key)) {
                exist = true;
                String cmd = "";
                String[] args, subargs;
                System.out.println("(i) Found " + entry.getKey() + ": ");
                String[] val = entry.getValue();
                for (int i = 0; i < val.length; i++) {
                    System.out.println("(i) - " + (i + 1) + ". " + val[i]);
                }
                System.out.println("(i) ----");
                System.out.println("(i) Edit commands:");
                System.out.println("(i) - (h)elp: Print this help.");
                System.out.println("(i) - (p)rint: Print the definitions.");
                System.out.println("(i) - (c)hange <id> <def>: Change the <id>th definition with <def>.");
                System.out.println("(i) - (d)elete <id>: Delete the <id>th definition.");
                System.out.println("(i) - (q)uit: Quit the edit menu.");
                Boolean listening = true;
                while (listening) {
                    System.out.print(" e> ");
                    cmd = Main.sc.nextLine();
                    // Split into [<command>, <arguments>]
                    args = cmd.split(" ", 2);
                    if (args.length == 1) {
                        args = new String[] { args[0], "" };
                    }
                    Integer id; // Index of definition
                    String option; // Confirmation variable
                    switch (args[0]) {
                        case "help":
                        case "h":
                            System.out.println("(i) Edit commands:");
                            System.out.println("(i) - (h)elp: Print this help.");
                            System.out.println("(i) - (p)rint: Print the definitions.");
                            System.out.println("(i) - (c)hange <id> <def>: Change the <id>th definition with <def>.");
                            System.out.println("(i) - (d)elete <id>: Delete the <id>th definition.");
                            System.out.println("(i) - (q)uit: Quit the edit menu.");
                            break;

                        case "print":
                        case "p":
                        case "":
                            for (int i = 0; i < val.length; i++) {
                                System.out.println("(i) - " + (i + 1) + ". " + val[i]);
                            }
                            System.out.println("----");
                            break;

                        case "change":
                        case "c":
                            subargs = args[1].split(" ", 2);
                            if (subargs.length < 2) {
                                System.out.println("(!) Missing arguments. Correct syntax is 'change <id> <def>'.");
                            } else {
                                id = Integer.parseInt(subargs[0]);
                                if (id < 1 || id > val.length) {
                                    System.out.println(
                                            "(!) Invalid index, the possible range is [1," + val.length + "].");
                                } else {
                                    val[--id] = subargs[1];
                                    data.replace(entry.getKey(), val);
                                    System.out.println("(i) Definition changed.");
                                }
                            }
                            modified = true;
                            break;

                        case "delete":
                        case "d":
                            // Split into 2 to discard any remaining arguments in subargs[1]
                            subargs = args[1].split(" ", 2);
                            id = Integer.parseInt(subargs[0]);
                            if (id < 1 || id > val.length) {
                                System.out.println("(!) Invalid index, the possible range is [1," + val.length + "].");
                            } else {
                                System.out.println("(@) Deleting '" + val[--id] + "'...");
                                System.out.println("(?) Do you want to delete this definition? (y/N)");
                                do {
                                    System.out.print(" > ");
                                    option = Main.sc.nextLine();
                                    switch (option) {
                                        case "yes":
                                        case "y":
                                            String[] newVal = new String[val.length - 1];
                                            System.arraycopy(val, 0, newVal, 0, id);
                                            System.arraycopy(val, id + 1, newVal, id, val.length - id - 1);
                                            val = newVal;
                                            data.replace(entry.getKey(), val);
                                            System.out.println("(i) Definition deleted.");
                                            break;

                                        case "no":
                                        case "n":
                                        case "":
                                            // Do nothing
                                            break;

                                        default:
                                            System.out.println("(!) Unknown option '" + option + "'.");
                                            option = "?";
                                            break;
                                    }
                                } while (option == "?");
                            }
                            modified = true;
                            break;

                        case "quit":
                        case "q":
                            listening = false;
                            break;

                        default:
                            System.out.println("(!) Unknown command '" + cmd + "'.");
                            break;
                    }
                }
                break;
            }
        }
        if (!exist) {
            System.out.println("(!) Slang word " + key + " not found.");
        }
        System.out.println();
    }

    /**
     * Delete a slang word from glossary. If an entry exist, ask the user to
     *  confirm deletion. If not, abort.
     * 
     * @param key keyword to remove from glossary
     */
    public void DeleteSlang(String key) {
        Boolean deleted = false, exist = false;
        // Enter data (if no args given)
        if (key == "") {
            System.out.println("(?) Enter keyword...");
            System.out.print(" > ");
            key = Main.sc.nextLine();
        }
        // Check existing
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            if (entry.getKey().equals(key)) {
                exist = true;
                String option = "";
                System.out.print("(?) Found '" + entry.getKey() + "': ");
                for (String str : entry.getValue()) {
                    System.out.print(str + " || ");
                }
                System.out.println();
                System.out.println("(?) Are you sure to delete? (y/N)");
                do {
                    System.out.print(" > ");
                    option = Main.sc.nextLine();
                    switch (option) {
                        case "yes":
                        case "y":
                            data.remove(key);
                            deleted = true;
                            break;

                        case "no":
                        case "n":
                        case "":
                            System.out.println("(i) Deleting cancelled.");
                            break;

                        default:
                            System.out.println("(!) Unknown option '" + option + "'. Valid ones are 'y' and 'n'.");
                            option = "?";
                            break;
                    }
                } while (option == "?");
                break;
            }
        }
        if (deleted) {
            System.out.println("(i) Slang word deleted from glossary.");
            modified = true;
        } else if (!exist) {
            System.out.println("(i) Slang word not exists.");
        }
        System.out.println();
    }

    /**
     * Restore the old glossary by re-reading the 'path'. This will override the
     * current csv with a new one.
     */
    public void Reset() {
        String csvpath = getFileName() + ".csv";
        System.out.println("(?) Do you want to reset the glossary? All changes made will be lost. (y/N)");
        String option;
        do {
            System.out.print(" > ");
            option = Main.sc.nextLine();
            switch (option) {
                case "yes":
                case "y":
                    try {
                        Files.delete(Path.of(csvpath));
                        Read();
                    } catch (IOException e) {
                        System.out.println("(!) Error reading file.");
                    }
                    break;

                case "no":
                case "n":
                case "":
                    System.out.println("(i) Reseting cancelled.");
                    System.out.println();
                    break;

                default:
                    System.out.println("(!) Unknown option '" + option + "'.");
                    option = "?";
                    break;
            }
        } while (option == "?");
    }

    /**
     * Output a random slang word.
     */
    public void Random() {
        // REF:
        // https://stackoverflow.com/questions/12385284/how-to-select-a-random-key-from-a-hashmap-in-java/12385392
        Random random = new Random();
        String key = data_id.get(random.nextInt(data_id.size()));
        TreeMap<String, String[]> randomMap = new TreeMap<String, String[]>();
        randomMap.put(key, data.get(key));
        System.out.println("(i) On this day slang word:");
        System.out.print("(i) ");
        Print(randomMap);
    }

    public void Game(String type, Integer stages) {
        Boolean done = false;
        Integer maxStage = 20, minStage = 1, score = 0;
        String cmd;
        String[] args;
        if (type == "") {
            type = "key";
        }
        System.out.println("(i) Game commands:");
        System.out.println("(i) - (p)lay: Play the game.");
        System.out.println("(i) - (c)hange key/def: Change game.");
        System.out.println("(i) - (s)etstages <number>: Set number of stage, possible range is [" + minStage + ","
                + maxStage + "].");
        System.out.println("(i) - (h)elp: Print this help.");
        System.out.println("(i) - (q)uit: Quit game menu.");
        System.out.println("(i) ----");
        while (!done) {
            System.out.println("(i) Current game: " + type);
            System.out.println("(i) Stages: " + stages);
            System.out.println("(i) Last score: " + score);
            System.out.println("(i) ----");
            System.out.print(" g> ");
            cmd = Main.sc.nextLine();
            switch (cmd) {
                case "play":
                case "p":
                    if (type == "key") {
                        score = GameKey(stages);
                    } else {
                        score = GameDef(stages);
                    }
                    score = score * 100 / stages;
                    System.out.println("-- Game complete! Your score: " + score + ".");
                    break;

                case "change":
                case "c":
                    args = cmd.split(" ", 3);
                    cmd = args[1];
                    break;

                case "setstages":
                case "s":
                    args = type.split(" ", 3);
                    stages = Integer.parseInt(args[1]);
                    if (stages < minStage) {
                        System.out.println("(!) Number too small, raised to " + minStage + ".");
                        stages = minStage;
                    } else if (stages > maxStage) {
                        System.out.println("(!) Number too large, capped at " + maxStage + ".");
                        stages = maxStage;
                    }
                    break;

                case "help":
                case "h":
                    System.out.println("(i) Game commands:");
                    System.out.println("(i) - (p)lay: Play the game.");
                    System.out.println("(i) - (c)hange key/def: Change game.");
                    System.out.println("(i) - (s)etstages <number>: Set number of stage, possible range is [" + minStage
                            + "," + maxStage + "].");
                    System.out.println("(i) - (h)elp: Print this help.");
                    System.out.println("(i) - (q)uit: Quit game menu.");
                    System.out.println("(i) ----");
                    break;

                case "quit":
                case "q":
                    done = true;
                    break;

                default:
                    System.out.println("(!) Unknown command '" + cmd + "'.");
                    break;
            }
        }
    }

    /**
     * Generate a randomized list of unique glossary entries for a quiz. Using
     * HashMap instead of TreeMap to store unsorted data. If glossary size is
     * smaller than stages, there will be duplicate entries.
     * 
     * @param stages size of the return HashMap
     * @return a HashMap containing random glossary entries
     */
    private HashMap<String, String> GenerateQuiz(Integer stages) {
        Random random = new Random();
        String key;
        HashMap<String, String> quizMap = new HashMap<String, String>();
        Boolean overlap = false;
        if (data.size() < stages) {
            overlap = true;
        }
        while (quizMap.size() < stages) {
            key = data_id.get(random.nextInt(data_id.size()));
            if (overlap || !quizMap.containsKey(key)) {
                // Get random def
                Integer id = random.nextInt(data.get(key).length);
                quizMap.put(key, data.get(key)[id]);
            }
        }
        return quizMap;
    }

    /**
     * Initialize quiz game: guessing slang words and return the result.
     * 
     * @param stages number of questions
     * @return the score (correct answers)
     */
    private Integer GameKey(Integer stages) {
        System.out.println("-- Welcome to Quiz Game: Slang word");
        HashMap<String, String> quizMap = GenerateQuiz(stages);
        return GameStart(stages, quizMap, "key");
    }

    /**
     * Initialize quiz game: definition quiz and return the result.
     * 
     * @param stages number of questions
     * @return the score (correct answers)
     */
    private Integer GameDef(Integer stages) {
        System.out.println("-- Welcome to Quiz Game: Definition");
        HashMap<String, String> quizMap = GenerateQuiz(stages);
        return GameStart(stages, quizMap, "def");
    }

    /**
     * Start the game and return number of correct answers.
     * 
     * @param stages  number of questions
     * @param quizMap availible questions
     * @param type    type of game
     * @return the score (correct answers)
     */
    private Integer GameStart(Integer stages, HashMap<String, String> quizMap, String type) {
        Random random = new Random();
        String[] key = quizMap.keySet().toArray(new String[stages]), def = quizMap.values().toArray(new String[stages]);
        Integer score = 0;
        for (int i = 0; i < quizMap.size(); i++) {
            // Ask questions
            if (type == "key") {
                System.out.println((i + 1) + ". " + def[i] + ":");
            } else {
                System.out.println((i + 1) + ". What is " + key[i] + "?");
            }
            Integer ans = random.nextInt(4);
            ArrayList<String> ansStrings = new ArrayList<String>();
            // Create options
            for (int a = 0; a < 4; a++) {
                if (a == ans) {
                    // The correct answer
                    ansStrings.add(key[i]);
                } else {
                    Boolean added = false;
                    while (!added) {
                        // The wrong answers
                        String badKey = data_id.get(random.nextInt(data_id.size()));
                        if (type == "key") {
                            if (!ansStrings.contains(badKey) || (data.size() < stages)) {
                                ansStrings.add(badKey);
                                added = true;
                            }
                        } else {
                            Integer id = random.nextInt(data.get(badKey).length);
                            String badDef = data.get(badKey)[id];
                            if (!ansStrings.contains(badDef) || (data.size() < stages)) {
                                ansStrings.add(badDef);
                                added = true;
                            }
                        }
                    }
                }
                switch (a) {
                    case 0:
                        System.out.print("A. ");
                        break;

                    case 1:
                        System.out.print("B. ");
                        break;

                    case 2:
                        System.out.print("C. ");
                        break;

                    case 3:
                        System.out.print("D. ");
                        break;
                }
                System.out.println(ansStrings.get(a) + ".");
            }
            String option = "";
            while (option == "") {
                System.out.print(" > ");
                option = Main.sc.nextLine();
                switch (option) {
                    case "a":
                    case "A":
                    case "1":
                        option = "0";
                        break;

                    case "b":
                    case "B":
                    case "2":
                        option = "1";
                        break;

                    case "c":
                    case "C":
                    case "3":
                        option = "2";
                        break;

                    case "d":
                    case "D":
                    case "4":
                        option = "3";
                        break;

                    default:
                        System.out.println("(!) Unknown option '" + option + "'. Valid ones are A/B/C/D/1/2/3/4.");
                        option = "";
                        break;
                }
            }
            // Check if chosen answer is the correct one
            // (also accounted for duplicated answers).
            Integer opt = Integer.parseInt(option);
            if (ansStrings.get(opt) == ansStrings.get(ans)) {
                System.out.println(" * CORRECT!!!");
                score++;
            } else {
                System.out.print(" * Wrong answer... The correct one is ");
                switch (ans) {
                    case 0:
                        System.out.print("A. ");
                        break;

                    case 1:
                        System.out.print("B. ");
                        break;

                    case 2:
                        System.out.print("C. ");
                        break;

                    case 3:
                        System.out.print("D. ");
                        break;
                }
                System.out.println(ansStrings.get(ans) + ".");
            }
        }
        return score;
    }
}
