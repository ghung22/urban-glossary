import java.io.*;
import java.util.*;

/**
 * Store data from a glossary file.
 */
public class Glossary {
    public String path;
    private TreeMap<String, String[]> data = new TreeMap<String, String[]>();
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
        System.out.println("(@) Reading from " + path + "...");
        FileInputStream fis = new FileInputStream(path);
        Scanner s = new Scanner(fis, "UTF-8");
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
            } else if (sec.length == 1) {
                // If not, it's another definition of the above keyword
                try {
                    // Split multiple meanings and joins the ones added before
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
        System.out.println("(@) Reading from " + csvpath + "...");
        FileInputStream fis = new FileInputStream(csvpath);
        Scanner s = new Scanner(fis, "UTF-8");
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
        System.out.println("(@) Writing to " + csvpath + "...");
        FileWriter fw = null;
        if (file.createNewFile()) {
            System.out.println("(i) Created " + csvpath + ".");
            fw = new FileWriter(file);
            fw.write("Keyword,Definition\n");
        }
        if (fw == null) {
            fw = new FileWriter(file);
        }
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
                return;
            }
            if (fw == null) {
                fw = new FileWriter(file);
            }
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
                String option = "";
                System.out.print("(?) Found an existing entry: " + entry.getKey() + ": ");
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
                            System.out.println("(!) Unknown command \"" + option + "\".");
                            option = "?";
                            break;
                    }
                } while (option == "?");
                exist = true;
                modified = true;
                break;
            }
        }
        if (added) {
            System.out.println("(i) Slang word updated to glossary.");
        } else if (!exist) {
            data.put(key, new String[] { def });
            System.out.println("(i) Slang word added to glossary.");
        }
        System.out.println();
    }
}
