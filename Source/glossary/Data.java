package glossary;

import java.io.*;
import java.util.*;

/**
 * Store data from a glossary file.
 */
public class Data extends Glossary {
    private TreeMap<String, String[]> data = new TreeMap<String, String[]>();

    /**
     * Copy data from superclass and read glossary data.
     * 
     * @param glossary
     */
    protected Data(Glossary glossary) {
        super(glossary);
        try {
            Read();
        } catch (IOException e) {
            System.out.println("(!) Error reading file.");
        }
    }

    /**
     * Read data from specified path using streams. The method will save the data
     * into a TreeMap&ltString, String[]&gt, with each represents the <i>
     * keyword</i> and the <i>meaning</i>.
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
        while (s.hasNextLine()) {
            // Split the keyword and meaning by the symbol '`'
            String line = s.nextLine();
            String sec[] = line.split("`", 2);
            sec[0] = sec[0].trim(); // Remove whitespaces
            if (sec.length >= 2) {
                // If there is a '`', split multiple meanings by symbol '|'
                String[] sec1 = sec[1].split("\\|");
                for (int i = 0; i < sec1.length; i++) {
                    sec1[i] = sec1[i].trim(); // Remove whitespaces
                }
                data.put(sec[0], sec1);
            } else if (sec.length == 1) {
                // If not, it's another meaning of the above keyword
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
                    // Start of file is a meaning of unknown keyword => Ignore
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
        while (s.hasNextLine()) {
            // Split the keyword and meaning by the symbol ','
            String line = s.nextLine();
            String sec[] = line.split(",", 2);
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
        if (file.createNewFile()) {
            System.out.println("(i) Created " + csvpath + ".");
        }
        FileWriter fw = new FileWriter(file);
        fw.write("Keyword,Meaning\n");
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
        System.out.println("(i) Printing content of Glossary...");
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            String[] values = entry.getValue();
            for (String str : values) {
                System.out.print(str + " || ");
            }
            System.out.println("");
        }
        System.out.println("");
    }

    /**
     * Get the path of the input file, discarding its extension.
     * 
     * @return the path leading to the input file, without the extension
     */
    private String getFileName() {
        return path.replaceFirst("[.][^.]+$", "");
    }
}
