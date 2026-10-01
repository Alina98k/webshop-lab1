package com.example.webshop.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hjälpklass för att skapa databasanslutningar
 */
public class Db {
    private static DataSource ds;
    /**
     * Hämtar en anslutning till databasen från den konfigurerade datakällan.
     *
     * @return en anslutning till databasen
     */
    public static Connection get() {
        if (ds == null) {
            try {
                Context init = new InitialContext();
                Context env = (Context) init.lookup("java:/comp/env");
                ds = (DataSource) env.lookup("jdbc/WebshopDS");
            } catch (NamingException e) {
                throw new RuntimeException("JNDI-datakälla hittades inte: jdbc/WebshopDS", e);
            }
        }

        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Kunde inte skapa databasanslutning", e);
        }
    }
}
