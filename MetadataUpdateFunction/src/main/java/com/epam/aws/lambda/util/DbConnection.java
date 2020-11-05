package com.epam.aws.lambda.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private final String url = System.getenv("RDS_HOST");
    private final String user = System.getenv("RDS_USERNAME");
    private final String password = System.getenv("RDS_PASSWORD");

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }
}
