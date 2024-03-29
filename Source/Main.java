import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

// References are marked with 'REF' keywords

/* Percentage of references (line refereced / total lines):
 * Main: 10 lines (1 REF) -> ~= 3.2%
 * Glossary: 10+1+1+2 =14 lines (4 REF) -> ~= 1.5%
 * ---
 * TOTAL: 4.6%
*/

public class Main {
    public static final Scanner sc = new Scanner(System.in, "UTF-8");

    public static void main(String[] args) {
        System.out.println("\n---- WELCOME TO URBAN GLOSSARY ----\n");

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
            // REF: https://stackabuse.com/java-list-files-in-a-directory/
            String[] files;
            File dir = new File("./Data");
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    return !name.startsWith(".") && !name.endsWith(".hist.csv") && name.endsWith(".csv");
                }
            };
            files = dir.list(filter);
            switch (files.length) {
                case 0:
                    // Do nothing
                    break;
                case 1:
                    // There's only 1 file -> use it
                    path = "./Data/" + files[0];
                    path = path.replaceFirst("[.][^.]+$", "") + ".txt";
                    f = new File("./Data/" + files[0]);
                    System.out.println("(i) Found glossary: " + files[0]);
                    break;
                default:
                    // There're many files -> ask user
                    System.out.print("(i) Found glossary: ");
                    for (String file : files) {
                        System.out.print(file + " ");
                    }
                    System.out.println("(?) Choose one to open...");
                    while (!f.isFile()) {
                        System.out.print(" > ");
                        path = sc.nextLine();
                        f = new File(path);
                    }
                    break;
            }
        }
        while (!f.isFile()) {
            System.out.println("(?) Enter import path...");
            System.out.print(" > ");
            path = sc.nextLine();
            f = new File(path);
            if (!f.isFile()) {
                System.out.println("(!) File not exist.");
            }
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
        String cmd = String.join(" ", args); // Put args to cmd
        Boolean listening = true;
        printHelp(""); // Print command list first
        while (listening) {
            if (cmd.isEmpty()) {
                System.out.print(" >> ");
                cmd = sc.nextLine();
            }
            // If user enters without a command -> do nothing
            if (cmd.isEmpty()) {
                continue;
            } else {
                System.out.println();
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
                        System.out.println("(!) Unknown subcommand '" + subargs[0] + "'.");
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
                    } else if (subargs[0].equals("")) {
                        System.out.println("(!) Missing subcommand. Try 'search key <term>' or 'search def <term>'.");
                    } else {
                        System.out.println("(!) Unknown subcommand '" + subargs[0]
                                + "' Try 'search key <term>' or 'search def <term>'.");
                    }
                    break;

                case "add":
                case "a":
                    subargs = args[1].split(" ", 2);
                    if (subargs.length == 1) {
                        subargs = new String[] { subargs[0], "" };
                    }
                    if (subargs[0].equals("")) {
                        glossary.AddSlang("", "");
                    } else if (subargs.length == 2) {
                        glossary.AddSlang(subargs[0], subargs[1]);
                    } else {
                        System.out.println("(!) Missing definition for '" + subargs[0] + "'.");
                    }
                    break;

                case "edit":
                case "e":
                    subargs = args[1].split(" ", 2);
                    glossary.EditSlang(subargs[0]);
                    break;

                case "delete":
                case "d":
                    subargs = args[1].split(" ", 2);
                    glossary.DeleteSlang(subargs[0]);
                    break;

                case "reset":
                case "r":
                    glossary.Reset();
                    break;

                case "onthisday":
                case "o":
                    glossary.Random();
                    break;

                case "game":
                case "g":
                    subargs = args[1].split(" ", 3);
                    if (subargs.length == 1) {
                        subargs = new String[] { subargs[0], "5", "" };
                    } else if (subargs.length == 2) {
                        subargs = new String[] { subargs[0], subargs[1], "" };
                    }
                    glossary.Game(subargs[0], Integer.parseInt(subargs[1]));
                    break;

                case "quit":
                case "q":
                    // Check for changes in glossary
                    if (glossary.modified) {
                        listening = false;
                        String option;
                        System.out.println("(?) There are unsaved changes, do you want to save? (Y/n/c)");
                        do {
                            System.out.print(" > ");
                            option = Main.sc.nextLine();
                            switch (option) {
                                case "yes":
                                case "y":
                                case "":
                                    try {
                                        glossary.Write();
                                    } catch (IOException e) {
                                        System.out.println("(!) Error reading file.");
                                    }
                                    option = "c";
                                    break;

                                case "no":
                                case "n":
                                    // Stop listening for commands -> exit method
                                    option = "c";
                                    break;

                                case "cancel":
                                case "c":
                                    listening = true;
                                    break;

                                default:
                                    System.out.println("(!) Unknown option '" + option + "'.");
                                    break;
                            }
                        } while (option != "c");
                    } else {
                        listening = false;
                    }
                    break;

                default:
                    System.out.println("(!) Unknown command '" + args[0] + "'.");
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
                System.out.println("(i) - (a)dd: Add a slang word.");
                System.out.println("(i) - (e)dit: Edit a slang word.");
                System.out.println("(i) - (d)elete: Delete a slang word.");
                System.out.println("(i) - (r)eset: Reset the glossary to the original data.");
                System.out.println("(i) - (o)nthisday: Output a random slang word.");
                System.out.println("(i) - (g)ame: Play a game.");
                System.out.println("(i) - (q)uit: Quit the program.");
                break;

            case "print":
            case "p":
                System.out.println("(i) Print commands (print <type>):");
                System.out.println("(i) - print: Output all entries in the glossary.");
                System.out.println("(i) - print search: Output search history.");
                break;

            case "search":
            case "s":
                System.out.println("(i) Search commands (search <type>):");
                System.out.println("(i) - search key: Search entries by keyword (case-insensitive).");
                System.out.println("(i) - search def: Search entries by definition (case-insensitive).");
                break;

            case "add":
            case "a":
                System.out.println("(i) Add commands (add <key> <def>):");
                System.out.println("(i) - add: Ask for keyword and definition to add to glossary.");
                System.out.println("(i) - add <key> <def>: Add <key> and <def> to glossary.");
                break;

            case "edit":
            case "e":
                System.out.println("(i) Edit commands (edit <key>):");
                System.out.println("(i) - edit: Enter edit menu and ask for a keywword.");
                System.out.println("(i) - edit <key>: Enter edit menu for <key>.");
                break;

            case "delete":
            case "d":
                System.out.println("(i) Delete commands (delete <key>):");
                System.out.println("(i) - delete: Ask for keyword to delete from glossary.");
                System.out.println("(i) - delete <key>: Delete slang word <key> from glossary.");
                break;

            case "game":
            case "g":
                System.out.println("(i) Game commands (game <type> <stages>):");
                System.out.println("(i) - game: Enter game menu.");
                System.out.println("(i) - game key: Guessing slang words.");
                System.out.println("(i) - game def: Definition quiz.");
                System.out.println("(i) - game key 8: Guessing 8 slang words.");
                System.out.println("(i) - game def 12: Definition quiz with 12 questions.");
                break;

            default:
                System.out.println("(i) No help exists for entered command.");
                break;
        }
    }
}