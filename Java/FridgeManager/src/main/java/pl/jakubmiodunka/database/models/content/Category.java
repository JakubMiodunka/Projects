package pl.jakubmiodunka.database.models.content;

/**
 * Model of product category imported from database.
 *
 * @author Jakub Miodunka
 */
public class Category {
    // Imported column values arranged as properties
    protected final long id;
    protected final String name;

    /**
     * @param id   Category ID.
     * @param name Category name.
     * */
    public Category(long id, String name) {
        // Properties init
        this.id = id;
        this.name = name;
    }

    /**
     * @return Category ID.
     * */
    public long getId() {
        return id;
    }

    /**
     * @return Category name.
     * */
    public String getName() {
        return name;
    }
}
