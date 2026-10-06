package com.example.webshop.ui;

import java.math.BigDecimal;

/** Överföringsobjekt med en separat kopia av produktens visningsdata. */
public final class ProductInfo {
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;

    public ProductInfo(Long id, String name, String description, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
}
