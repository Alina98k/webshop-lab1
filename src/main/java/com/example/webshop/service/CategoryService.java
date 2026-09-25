package com.example.webshop.service;

import com.example.webshop.dao.CategoryDao;
import com.example.webshop.dao.CategoryDaoJdbc;
import com.example.webshop.model.Category;

import java.sql.SQLException;
import java.util.List;

/**
 * {@code CategoryService} ansvarar för hantering av produktkategorier.
 * Databasoperationer utförs via {@link CategoryDao} och servicen
 * erbjuder ett enkelt API till controllerlagret.
 *
 * <h2>Uppgiftsrelation (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong>:
 *     <ul>
 *       <li>Kategorihantering är inte ett krav i denna nivå,
 *           men utgör grunden för produktklassificering.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 4</strong>:
 *     <ul>
 *       <li>I “Varulager” (produktlager) används denna service
 *           för att koppla produkter till rätt kategori.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong>:
 *     <ul>
 *       <li>Möjlighet att skapa, redigera och ta bort kategorier
 *           i admin-gränssnittet tillhandahålls av denna service.</li>
 *       <li>Uppfyller direkt kravet <em>“Lägga till och editera varor och varukategorier”</em>.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Trelagersarkitektur</h2>
 * <pre>
 * Controller → Service → DAO → Database
 *
 * AdminCategoryController  →  CategoryService  →  CategoryDaoJdbc
 * </pre>
 *
 * <h2>Huvudsakliga ansvarsområden</h2>
 * <ul>
 *   <li>Hämta lista över alla kategorier</li>
 *   <li>Hämta en specifik kategori via ID</li>
 *   <li>Skapa ny kategori</li>
 *   <li>Uppdatera befintlig kategori</li>
 *   <li>Ta bort kategori</li>
 * </ul>
 *
 * <h2>Felkänslighet</h2>
 * <ul>
 *   <li>Alla databasfel kastas som {@link SQLException} till högre lager.</li>
 *   <li>I controllerlagret hanteras dessa ofta som {@code ServletException}.</li>
 * </ul>
 *
 * <h2>Framtida förbättringar</h2>
 * <ul>
 *   <li>Kontrollera om kategorin innehåller produkter innan radering.</li>
 *   <li>Lägg till stöd för underkategorier (hierarkisk struktur).</li>
 *   <li>Utöka med beskrivning, bild eller sorteringsordning.</li>
 * </ul>
 *
 * <h2>Exempel på användning (i Controller)</h2>
 * <pre>{@code
 * CategoryService categoryService = new CategoryService();
 *
 * // Lista kategorier
 * List<Category> list = categoryService.list();
 *
 * // Lägg till ny kategori
 * categoryService.create("Elektronik");
 *
 * // Uppdatera kategori
 * categoryService.update(1L, "Datorer");
 *
 * // Ta bort kategori
 * categoryService.delete(2L);
 * }</pre>
 *
 * @author Your Name
 * @since 1.0
 */
public class CategoryService {

    /** DAO-objekt för databasåtkomst. */
    private final CategoryDao dao = new CategoryDaoJdbc();

    /**
     * Returnerar alla kategorier.
     *
     * @return lista av {@link Category}-objekt
     * @throws SQLException vid databasfel
     */
    public List<Category> list() throws SQLException { return dao.findAll(); }

    /**
     * Returnerar en kategori utifrån dess ID.
     *
     * @param id kategori-ID
     * @return {@link Category} eller {@code null} om ingen hittas
     * @throws SQLException vid databasfel
     */
    public Category get(Long id) throws SQLException { return dao.findById(id); }

    /**
     * Skapar en ny kategori.
     *
     * @param name kategorinamnet
     * @return det genererade kategori-ID:t
     * @throws SQLException vid databasfel
     */
    public Long create(String name) throws SQLException {
        Category c = new Category();
        c.setName(name);
        return dao.create(c);
    }

    /**
     * Uppdaterar en befintlig kategori.
     *
     * @param id   kategori-ID
     * @param name nytt kategorinamn
     * @throws SQLException vid databasfel
     */
    public void update(Long id, String name) throws SQLException {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        dao.update(c);
    }

    /**
     * Tar bort en kategori.
     *
     * @param id kategori-ID
     * @throws SQLException vid databasfel
     */
    public void delete(Long id) throws SQLException { dao.delete(id); }
}
