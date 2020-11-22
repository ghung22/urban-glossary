package glossary;

public class Glossary {
    protected String path = "";
    public Data data = null;

    /**
     * Constructor to get file path and read glossary data.
     * 
     * @param path the path of the glossary file
     */
    public Glossary(String path) {
        this.path = path.replace('\\', '/');
        data = new Data(this);
    }

    /**
     * Copy constructor for subclasses to use.
     * 
     * @param glossary the superclass
     */
    protected Glossary(Glossary glossary) {
        path = glossary.path;
    }
}
