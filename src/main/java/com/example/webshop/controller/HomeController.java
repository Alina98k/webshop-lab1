package com.example.webshop.controller;

import com.example.webshop.model.Product;
import com.example.webshop.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "HomeController", urlPatterns = {"/home"})
public class HomeController extends HttpServlet {

    /**
     * Servicelagret som hämtar produkterna.
     * <p>Använder {@link ProductService#listActive()} för att endast returnera aktiva (säljbara) produkter.</p>
     */
    private final ProductService productService = new ProductService();

    /**
     * Hanterar förfrågan till startsidan.
     * <p>Hämtar listan över aktiva produkter, lägger den i förfrågningsattributet och
     * dirigerar till vyn <code>home.jsp</code>.</p>
     *
     * @param req  HTTP-förfrågan
     * @param resp HTTP-svar
     * @throws ServletException vid fel under dataåtkomst eller dirigering
     * @throws IOException      vid I/O-fel
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Product> products = productService.listActive();
            req.setAttribute("products", products);
            req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
