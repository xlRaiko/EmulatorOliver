package com.eu.habbo.core;

import com.eu.habbo.Emulator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HighscoreDelete implements Runnable, DatabaseLoggable {

    private static final String DELETE_QUERY = "DELETE FROM `items_highscore_data` WHERE `item_id` = ?";

    private final int itemId;

    public HighscoreDelete(int itemId) {
        this.itemId = itemId;
    }

    @Override
    public String getQuery() {
        return HighscoreDelete.DELETE_QUERY;
    }

    @Override
    public void log(PreparedStatement statement) throws SQLException {
        statement.setInt(1, this.itemId);
        statement.addBatch();
    }

    @Override
    public void run() {
        Emulator.getDatabaseLogger().store(this);
    }
}