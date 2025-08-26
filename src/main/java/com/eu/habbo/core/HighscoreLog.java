package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HighscoreLog implements Runnable, DatabaseLoggable {

    private static final String INSERT_QUERY = "INSERT INTO `items_highscore_data` (`item_id`, `user_ids`, `score`, `is_win`, `timestamp`) VALUES (?, ?, ?, ?, ?)";

    private final int itemId;
    private final String winners;
    private final int score;
    private final boolean win;
    private final int timestamp;

    public HighscoreLog(int itemId, String winners, int score, boolean win, int timestamp) {
        this.itemId = itemId;
        this.winners = winners;
        this.score = score;
        this.win = win;
        this.timestamp = timestamp;
    }

    @Override
    public String getQuery() {
        return HighscoreLog.INSERT_QUERY;
    }

    @Override
    public void log(PreparedStatement statement) throws SQLException {
        statement.setInt(1, this.itemId);
        statement.setString(2, this.winners);
        statement.setInt(3, this.score);
        statement.setBoolean(4, this.win);
        statement.setInt(5, this.timestamp);
        statement.addBatch();
    }

    @Override
    public void run() {
        Emulator.getDatabaseLogger().store(this);
    }
}