package pl.jakubmiodunka.database.models.content;

import java.time.LocalDate;

/**
 * Model of the product imported from database.
 * Extends category model as some properties are common in both cases.
 *
 * @author Jakub Miodunka
 */
public class Product extends Category {
    // Imported column values arranged as properties
    private final String category;
    private final LocalDate expirationDate;

    /**
     * @param id             Product ID.
     * @param name           Product name.
     * @param category       Name of category, to which product belongs to.
     * @param expirationDate Date of product expiration.
     */
    public Product(long id, String name, String category, LocalDate expirationDate) {
        // Parent class contractor call
        super(id, name);

        // Further initialization of properties
        this.category = category;
        this.expirationDate = expirationDate;
    }

    /**
     * @return Name of category, to which product belongs to.
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return Date of product expiration.
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }
}
