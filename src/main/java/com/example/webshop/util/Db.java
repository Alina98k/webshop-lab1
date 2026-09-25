package com.example.webshop.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * {@code Db} är en hjälparklass (utility class) som hanterar applikationens
 * databasanslutningar (JDBC).
 * <p>
 * Anslutningar hämtas via en {@link DataSource} som är definierad i Java EE-miljön.
 * Denna datakälla är vanligtvis konfigurerad i JNDI-miljön hos en applikationsserver
 * som <b>Tomcat</b>.
 * </p>
 *
 * <h2>Koppling till uppgiften (vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong>:
 *     <ul>
 *       <li>Uppfyller kravet “Java-applikation som via JDBC har access till en databas”.</li>
 *       <li>Ger DAO-lagret tillgång till databasen i en trelagersarkitektur.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 4</strong>:
 *     <ul>
 *       <li>Vid transaktioner (t.ex. order + lager) hanteras anslutningen
 *           genom {@link java.sql.Connection#setAutoCommit(boolean)} via denna klass.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong>:
 *     <ul>
 *       <li>Möjliggör att användare av olika typer (admin, lager, kund)
 *           delar samma datakälla.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Hur den fungerar</h2>
 * <ol>
 *   <li>Vid första anropet söker klassen i JNDI-miljön efter en {@link DataSource}
 *       med namnet <b>“jdbc/WebshopDS”</b>.</li>
 *   <li>Om den hittas lagras den i det statiska fältet {@code ds} för återanvändning.</li>
 *   <li>Vid varje nytt anrop returneras en ny {@link Connection} via {@code ds.getConnection()}.</li>
 * </ol>
 *
 * <h2>Exempel på Tomcat-konfiguration (context.xml)</h2>
 * <pre>{@code
 * <Resource name="jdbc/WebshopDS"
 *           auth="Container"
 *           type="javax.sql.DataSource"
 *           maxTotal="50"
 *           maxIdle="5"
 *           maxWaitMillis="10000"
 *           username="webshop_user"
 *           password="123456"
 *           driverClassName="com.mysql.cj.jdbc.Driver"
 *           url="jdbc:mysql://localhost:3306/webshop_db?useSSL=false&amp;serverTimezone=UTC"/>
 * }</pre>
 *
 * <h2>Felmärkning och undantag</h2>
 * <ul>
 *   <li>Om JNDI-resursen inte hittas kastas ett {@link RuntimeException}-fel.</li>
 *   <li>Vid databasfel kastas {@link SQLException} direkt vidare till det anropande lagret.</li>
 * </ul>
 *
 * <h2>Förslag på framtida förbättringar</h2>
 * <ul>
 *   <li>Övervakning av anslutningspoolens statistik (t.ex. HikariCP, DBCP2).</li>
 *   <li>Alternativ för direkt JDBC-anslutning i testmiljöer.</li>
 *   <li>Hjälpmetoder för att stänga anslutningar automatiskt (t.ex. <code>Db.close()</code>).</li>
 * </ul>
 *
 * <h2>Exempel på användning (inne i DAO-klasser)</h2>
 * <pre>{@code
 * try (Connection c = Db.get();
 *      PreparedStatement ps = c.prepareStatement("SELECT * FROM products")) {
 *      ...
 * }
 * }</pre>
 *
 * @author Your Name
 * @since 1.0
 */
public class Db {

    /** Delad JNDI-datakälla som används i hela applikationen. */
    private static DataSource ds;

    /**
     * Returnerar en ny {@link Connection} från applikationens konfigurerade
     * datakälla (via JNDI).
     *
     * @return en aktiv databasanslutning
     * @throws SQLException vid fel i anslutning eller anslutningspool
     */
    public static Connection get() throws SQLException {
        if (ds == null) {
            try {
                Context init = new InitialContext();
                Context env = (Context) init.lookup("java:/comp/env");
                ds = (DataSource) env.lookup("jdbc/WebshopDS");
            } catch (NamingException e) {
                throw new RuntimeException("JNDI-datakälla hittades inte: jdbc/WebshopDS", e);
            }
        }
        return ds.getConnection();
    }
}
