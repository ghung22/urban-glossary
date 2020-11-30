import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FilenameFilter;

public class Main {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("\n---- WELCOME TO URBAN DICTIONARY ----\n");

        Glossary glossary = getGlossary(args);
        if (args.length != 0) {
            if (glossary.path == args[0]) {
                // Remove the path argument
                args = Arrays.copyOfRange(args, 1, args.length);
            }
        }
        getCommand(glossary, args);

        sc.close();
    }

    /**
     * Init a glossary database. If no existing glossary found, ask the user to
     * input the path of the new glossary.
     * 
     * @param args get file name for terminal arguments
     * @return the glossasy object
     */
    private static Glossary getGlossary(String[] args) {
        if (args.length == 0) {
            args = new String[] { "" };
        }
        String path = args[0];
        File f = new File(path);
        if (!f.isFile()) {
            // Check for existing csv files in Data directory
            String[] files;
            File dir = new File("./Data");
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    return !name.startsWith(".") && name.endsWith(".csv");
                }
            };
            files = dir.list(filter);
            switch (files.length) {
                case 0:
                    break;
                case 1:
                    // There's only 1 file -> use it
                    path = "./Data/" + files[0];
                    f = new File("./Data/" + files[0]);
                    System.out.println("(i) Found glossary: " + files[0]);
                    break;
                default:
                    // There're many files -> ask user
                    System.out.print("(i) Existing glossary: ");
                    for (String file : files) {
                        System.out.print(file + " ");
                    }
                    System.out.println("(i) Choose one file to open...");
                    while (!f.isFile()) {
                        System.out.print("file > ");
                        path = sc.nextLine();
                        f = new File(path);
                    }
                    break;
            }
        }
        while (!f.isFile()) {
            System.out.print("path > ");
            path = sc.nextLine();
            f = new File(path);
        }
        return new Glossary(path);
    }

    /**
     * Continously get commands from user and run the corresponding methods
     * 
     * @param glossary the glossary object
     * @param args     arguments entered when launched the program
     */
    private static void getCommand(Glossary glossary, String[] args) {
        String cmd = String.join(" ", args);  // Put args to cmd
        Boolean listening = true;
        printHelp("");  // Print command list first
        while (listening) {
            if (cmd.isEmpty()) {
                System.out.print(" > ");
                cmd = sc.nextLine();
            }
            // Split into [<command>, <arguments>]
            args = cmd.split(" ", 2);
            if (args.length == 1) {
                args = new String[] { args[0], "" };
            }
            // For spliting into [<subcommand>, <arguments>]
            String[] subargs;
            switch (args[0]) {
                case "help":
                case "h":
                    subargs = args[1].split(" ", 2);
                    if (subargs.length == 1) {
                        subargs = new String[] { subargs[0], "" };
                    }
                    printHelp(subargs[0]);
                    break;
                case "print":
                case "p":
                    subargs = args[1].split(" ", 2);
                    if (subargs.length == 1) {
                        subargs = new String[] { subargs[0], "" };
                    }
                    if (subargs[0].isEmpty()) {
                        glossary.Print();
                    } else if (subargs[0].equals("search")) {
                        glossary.PrintSearchHistory();
                    } else {
                        System.out.println("(!) Unknown subcommand \"" + subargs[0] + "\".");
                    }
                    break;

                case "search":
                case "s":
                    subargs = args[1].split(" ", 2);
                    if (subargs.length == 1) {
                        subargs = new String[] { subargs[0], "" };
                    }
                    if (subargs[0].equals("key")) {
                        glossary.SearchKeyword(subargs[1]);
                    } else if (subargs[0].equals("def")) {
                        glossary.SearchDefinition(subargs[1]);
                    } else {
                        System.out.println("(!) Unknown subcommand \"" + subargs[0] + "\".");
                    }
                    break;

                case "quit":
                case "q":
                    listening = false;
                    break;

                default:
                    System.out.println("(!) Unknown command \"" + args[0] + "\".");
                    break;
            }
            cmd = "";
        }
    }

    /**
     * Output a help infomation to terminal.
     * 
     * @param help desired section to get from help
     */
    private static void printHelp(String help) {
        switch (help) {
            case "":
                System.out.println("(i) Commands (enter 'help <command>' for more details of that command):");
                System.out.println("(i) - (h)elp: Print this help.");
                System.out.println("(i) - (p)rint: Output data to terminal.");
                System.out.println("(i) - (s)earch: Search entries by keyword/definition");
                System.out.println("(i) - (q)uit: Quit the program.");
                break;
            case "print":
            case "p":
                System.out.println("(i) Print commands (print <subcommand>):");
                System.out.println("(i) - <no subcommand>: Output all entries in the glossary.");
                System.out.println("(i) - search: Output search history.");
                break;
            case "search":
            case "s":
                System.out.println("(i) Search commands (print <subcommand>):");
                System.out.println("(i) - key: Search entries by keyword (case-insensitive, whole words).");
                System.out.println("(i) - def: Search entries by definition (case-insensitive).");
                break;
            default:
                System.out.println("(i) No help exists for entered command.");
                break;
        }
    }
}

// TODO: Detect change in txt => update csv
// TODO: by kw and def haven't yet filtered
// TODO: search getCommand(), get remaining words to search