package com.eu.habbo.core;

import com.eu.habbo.Emulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CounterIpsScheduler {
    public static int Counter(String userIP){
        int count = 0;
        try (Connection connect = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement pS1 = connect.prepareStatement("SELECT COUNT(*) AS countRow FROM users WHERE ip_current = ? AND online = '1' LIMIT 1")) {
                pS1.setString(1, userIP);
                ResultSet rs1 = pS1.executeQuery();
                rs1.next();
                count = rs1.getInt("countRow");
                rs1.close();
            }
        } catch(SQLException ignored) {}

        return count;
    }
}
