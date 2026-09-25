package com.example.webshop.dao;

import com.example.webshop.model.Category;
import com.example.webshop.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * {@code CategoryDaoJdbc} är en konkret JDBC-implementation av {@link CategoryDao}-gränssnittet.
 *
 * <h2>Uppgiftskoppling (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong>:
 *     <ul>
 *       <li><em>3-lagersarkitektur</em> – Denna klass representerar datalagringslagret,
 *           kravet uppfylls genom flödet {@code Service → DAO → Database}.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 4</strong>:
 *     <ul>
 *       <li><em>Användarhantering och lagerstruktur</em> – Eftersom kategorier är en del
 *           av produktadministrationen, hanteras sambandet mellan produkter och kategorier via denna DAO.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong>:
 *     <ul>
 *       <li><em>Administrativa gränssnitt</em> – Kategorihantering (skapa/redigera/ta bort)
 *           i adminpanelen sker via dessa DAO-metoder.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Ansvar</h2>
 * <ul>
 *   <li>Lista, hämta, skapa, uppdatera och ta bort kategorier.</li>
 *   <li>Varje metod skapar en <strong>egen JDBC-anslutning</strong> och stänger den säkert.</li>
 * </ul>
 *
 * <h2>Arkitekturposition</h2>
 * <p>
 * Finns i applikationens <strong>DAO-lager</strong>.
 * Affärslogik hanteras av {@code CategoryService} och användarinteraktioner av
 * {@code AdminCategoryController}.
 * </p>
 *
 * <h2>Databasschema</h2>
 * <pre>
 * Tabell: categories
 * +----+-----------+
 * | id | name      |
 * +----+-----------+
 * </pre>
 *
 * <h2>Anslutningshantering</h2>
 * <p>
 * Varje operation använder en ny {@link Connection} som hämtas via {@link Db#get()}.
 * Tack vare try-with-resources stängs alla resurser automatiskt.
 * </p>
 *
 * <h2>Felhantering</h2>
 * <ul>
 *   <li>Alla JDBC-fel kastas som {@link SQLException}.</li>
 *   <li>Överliggande lager (Service, Controller) fångar dessa och visar
 *       användarvänliga felmeddelanden.</li>
 * </ul>
 *
 * @author Your Name
 * @since 1.0
 */
public class CategoryDaoJdbc implements CategoryDao {

    /**
     * Returnerar alla kategorier sorterade efter namn.
     *
     * @return Lista av {@link Category}
     * @throws SQLException vid databasfel
     */
    @Override
    public List<Category> findAll() throws SQLException {
        String sql = "SELECT id, name FROM categories ORDER BY name";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Category> list = new ArrayList<>();
            while (rs.next()) {
                Category x = new Category();
                x.setId(rs.getLong("id"));
                x.setName(rs.getString("name"));
                list.add(x);
            }
            return list;
        }
    }

    /**
     * Hämtar en kategori utifrån ID.
     *
     * @param id kategori-ID
     * @return {@link Category}-objekt eller {@code null} om inget hittas
     * @throws SQLException vid databasfel
     */
    @Override
    public Category findById(Long id) throws SQLException {
        String sql = "SELECT id, name FROM categories WHERE id=?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Category x = new Category();
                x.setId(rs.getLong("id"));
                x.setName(rs.getString("name"));
                return x;
            }
        }
    }

    /**
     * Skapar en ny kategori.
     *
     * @param c kategori som ska läggas till (endast {@code name} används)
     * @return det genererade ID:t från databasen
     * @throws SQLException vid fel eller om inget ID genereras
     */
    @Override
    public Long create(Category c) throws SQLException {
        String sql = "INSERT INTO categories(name) VALUES (?)";
        try (Connection cx = Db.get();
             PreparedStatement ps = cx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Kategori-ID genererades inte");
    }

    /**
     * Uppdaterar namnet på en befintlig kategori.
     *
     * @param c kategori som ska uppdateras (ID krävs)
     * @throws SQLException vid databasfel
     */
    @Override
    public void update(Category c) throws SQLException {
        String sql = "UPDATE categories SET name=? WHERE id=?";
        try (Connection cx = Db.get();
             PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setLong(2, c.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Tar bort en kategori utifrån ID.
     * <p>
     * Not: Om det finns produkter kopplade till denna kategori måste deras kategori-värde
     * sättas till {@code NULL} enligt databasens främmande nyckel-begränsning.
     * </p>
     *
     * @param id kategori-ID som ska tas bort
     * @throws SQLException vid databasfel
     */
    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id=?";
        try (Connection cx = Db.get();
             PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
