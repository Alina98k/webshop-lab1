package com.example.webshop.ui;

import java.math.BigDecimal;

/** Separat visningsdata för en rad i varukorgen. */
public final class CartInfo {
    private final ProductInfo product;
    private final int qty;
    private final BigDecimal lineTotal;

    public CartInfo(ProductInfo product, int qty, BigDecimal lineTotal) {
        this.product = product;
        this.qty = qty;
        this.lineTotal = lineTotal;
    }

    public ProductInfo getProduct() { return product; }
    public int getQty() { return qty; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
