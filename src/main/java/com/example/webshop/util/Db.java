package com.example.webshop.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
public class Db {
    private static DataSource ds;
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
