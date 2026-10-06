package com.example.webshop.service;

import com.example.webshop.dao.ProductDaoJdbc;
import com.example.webshop.entities.Product;
import com.example.webshop.ui.ProductInfo;
import java.util.ArrayList;
import java.util.List;

/** Hämtar produkter och omvandlar dem till överföringsobjekt för presentationen. */
public class ProductService {
    private final ProductDaoJdbc productDao = new ProductDaoJdbc();

    /** Hämtar alla produkter som separata visningsobjekt. */
    public List<ProductInfo> listAll() {
        List<ProductInfo> result = new ArrayList<>();
        for (Product product : productDao.findAll()) {
            result.add(toInfo(product));
        }
        return result;
    }

    /** Hämtar visningsdata för en produkt, eller null om den saknas. */
    public ProductInfo get(Long id) {
        Product product = productDao.findById(id);
        return product == null ? null : toInfo(product);
    }

    static ProductInfo toInfo(Product product) {
        return new ProductInfo(product.getId(), product.getName(),
                product.getDescription(), product.getPrice());
    }
}
