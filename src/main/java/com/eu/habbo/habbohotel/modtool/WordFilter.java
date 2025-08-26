package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Message;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.friends.FriendChatMessageComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.plugin.events.users.UserTriggerWordFilterEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.regex.Pattern;

public class WordFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordFilter.class);

    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    //Configuration. Loaded from database & updated accordingly.
    public static boolean ENABLED_FRIENDCHAT = true;
    public static String DEFAULT_REPLACEMENT = "bobba";
    protected THashSet<WordFilterWord> autoReportWords = new THashSet<>();
    protected THashSet<WordFilterWord> hideMessageWords = new THashSet<>();
    protected THashSet<WordFilterWord> words = new THashSet<>();

    public WordFilter() {
        long start = System.currentTimeMillis();
        this.reload();
        LOGGER.info("WordFilter -> Loaded! (" + (System.currentTimeMillis() - start) + " MS)");
    }

    private static String stripDiacritics(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
        return str;
    }

    public synchronized void reload() {
        if (!Emulator.getConfig().getBoolean("hotel.wordfilter.enabled"))
            return;

        this.autoReportWords.clear();
        this.hideMessageWords.clear();
        this.words.clear();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet set = statement.executeQuery("SELECT * FROM wordfilter")) {
                while (set.next()) {
                    WordFilterWord word;

                    try {
                        word = new WordFilterWord(set);
                    } catch (SQLException e) {
                        LOGGER.error("Caught SQL exception", e);
                        continue;
                    }

                    if (word.autoReport)
                        this.autoReportWords.add(word);
                    else if (word.hideMessage)
                        this.hideMessageWords.add(word);

                    this.words.add(word);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public static String normalise(String message) {
        String newMessage = message.toLowerCase(); // lowerCase to better practice.

        // Composite Filters.
        newMessage = newMessage.replace("/\\", "a").replace("/-/", "h")
                .replace("i-i", "h").replace("l-l", "h").replace("!-l", "h")
                .replace("i3", "b").replace("l3", "b")
                .replace("|3", "b").replace("]-[", "h")
                .replace("\\/", "v").replace("[3", "b");

        // Check Spaces.
        newMessage = newMessage.replace(" ", "").replace(" ", "")
                .replace("-", "").replace("(", "").replace(")", "")
                .replace("[", "").replace("]", "").replace("—", "")
                .replace(":", "").replace(";", "").replace("/", "")
                .replace("*", "").replace("`", "").replace(",", "")
                .replace(".", "").replace("_", "").replace("⋆", "")
                .replace("͙", "");

        // A
        newMessage = newMessage.replace("á", "a").replace("à", "a")
                .replace("ä", "a").replace("å", "a").replace("â", "a")
                .replace("ã", "a").replace("ą", "a").replace("4", "a")
                .replace("@", "a").replace("Ⲇ", "a").replace("₳", "a")
                .replace("\uD83C\uDDE6", "a").replace("\uD835\uDCEA", "a")
                .replace("\uD835\uDC68", "a").replace("\uD835\uDE56", "a")
                .replace("\uD835\uDE8A", "a");

        // B
        newMessage = newMessage.replace("ß", "b").replace("в", "b")
                .replace("฿", "b").replace("\uD83C\uDDE7", "b")
                .replace("\uD835\uDC69", "b").replace("\uD835\uDE8B", "b")
                .replace("\uD835\uDE57", "b").replace("\uD835\uDD53", "b")
                .replace("\uD835\uDC1B", "b");

        // C
        // D
        newMessage = newMessage.replace("ð", "d");

        // E
        newMessage = newMessage.replace("é", "e").replace("è", "e")
                .replace("ë", "e").replace("ê", "e").replace("ę", "e")
                .replace("3", "e").replace("£", "e").replace("∑", "e")
                .replace("Ξ", "e").replace("е", "e").replace("€", "e")
                .replace("ɇ", "e").replace("\uD835\uDC6C", "e")
                .replace("\uD835\uDE8E", "e");

        // F
        newMessage = newMessage.replace("\uD83C\uDDEB", "f").replace("\uD835\uDCD5", "f")
                .replace("\uD835\uDE5B", "f");

        // G
        // H
        newMessage = newMessage.replace("н", "h").replace("ⱨ", "h")
                .replace("\uD83C\uDDED", "h").replace("\uD835\uDC89", "h")
                .replace("\uD835\uDE5D", "h").replace("\uD835\uDE91", "h")
                .replace("\uD835\uDE43", "h").replace("ℍ", "h")
                .replace("\uD835\uDC07", "h");

        // I
        newMessage = newMessage.replace("í", "i").replace("ì", "i")
                .replace("ï", "i").replace("î", "i").replace("į", "i")
                .replace("\uD83C\uDDEE", "i");

        // J
        // K
        // L
        newMessage = newMessage.replace("ⱡ", "l").replace("\uD835\uDE61", "l")
                .replace("\uD835\uDC73", "l").replace("\uD835\uDE95", "l");

        // M
        newMessage = newMessage.replace("₥", "m").replace("\uD835\uDC74", "m")
                .replace("\uD835\uDE96", "m");

        // N
        newMessage = newMessage.replace("₦", "n").replace("\uD835\uDCF7", "n")
                .replace("\uD835\uDE63", "n");

        // O
        newMessage = newMessage.replace("ó", "o").replace("ò", "o")
                .replace("ö", "o").replace("ô", "o")
                .replace("õ", "o").replace("ő", "o")
                .replace("0", "o").replace("ø", "o")
                .replace("о", "o").replace("\uD835\uDE64", "o")
                .replace("\uD83C\uDDF4", "o").replace("\uD835\uDD60", "o")
                .replace("\uD835\uDC28", "o");

        // P
        // Q
        // R
        // S
        newMessage = newMessage.replace("5", "s").replace("\uD835\uDCFC", "s")
                .replace("\uD835\uDC7A", "s").replace("\uD835\uDE68", "s")
                .replace("\uD835\uDE9C", "s");

        // T
        newMessage = newMessage.replace("₮", "t").replace("\uD835\uDCFD", "t")
                .replace("\uD835\uDE69", "t").replace("т", "t");

        // U
        newMessage = newMessage.replace("ú", "u").replace("ù", "u")
                .replace("ü", "u").replace("û", "u").replace("ų", "u")
                .replace("ʉ", "u").replace("\uD835\uDE6A", "u")
                .replace("\uD835\uDC7C", "u").replace("\uD835\uDE9E", "u")
                .replace("\uD83C\uDDFA", "u").replace("\uD835\uDD66", "u")
                .replace("\uD835\uDC2E", "u");

        // V
        newMessage = newMessage.replace("√", "v");

        // W
        // X
        // Y
        newMessage = newMessage.replace("\uD83C\uDDFE", "y").replace("\uD835\uDD02", "y")
                .replace("\uD835\uDE6E", "y").replace("ү", "y");

        // Z
        newMessage = newMessage.replace("\uD835\uDE6F", "z").replace("\uD83C\uDDFF", "z")
                .replace("\uD835\uDD6B", "z").replace("\uD835\uDC33", "z");

        // Others.
        newMessage = newMessage.replace("æ", "ae").replaceAll("\\.+", ".");
        return newMessage;
    }

    public boolean autoReportCheck(RoomChatMessage roomChatMessage) {
        String message = normalise(roomChatMessage.getMessage().toLowerCase());

        for (WordFilterWord word : this.autoReportWords) {
            if (message.contains(word.key)) {
                Emulator.getGameEnvironment().getModToolManager().quickTicket(roomChatMessage.getHabbo(), "Automatic WordFilter", roomChatMessage.getMessage());

                if (Emulator.getConfig().getBoolean("notify.staff.chat.auto.report")) {
                    if (Emulator.getPluginManager().fireEvent(new UserTriggerWordFilterEvent(roomChatMessage.getHabbo(), word)).isCancelled())
                        continue;

                    Message chatMessage = new Message(roomChatMessage.getHabbo().getHabboInfo().getId(), -5, Emulator.getTexts().getValue("warning.auto.report").replace("%word%", word.key));
                    Emulator.getGameServer().getGameClientManager().sendBroadcastResponse((new FriendChatMessageComposer(chatMessage, -5, roomChatMessage.getHabbo().getHabboInfo().getId())).compose(), "acc_automatic_chat", roomChatMessage.getHabbo().getClient());

                    // Bubble Alert
                    String[] ranks = Emulator.getConfig().getValue("kb.moderation.minrank").split(",");

                    for(Habbo i : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
                        int minRank = i.getHabboInfo().getRank().getId();

                        if (Arrays.asList(ranks).contains(String.valueOf(minRank))) {
                            THashMap<String, String> keys = new THashMap<>();
                            keys.put("display", "BUBBLE");
                            keys.put("message", Emulator.getTexts().getValue("kb.moderation.message").replace("%username%", roomChatMessage.getHabbo().getHabboInfo().getUsername()).replace("%filter%", word.key));
                            keys.put("image", Emulator.getConfig().getValue("kb.moderation.image"));
                            keys.put("sound", "filter_alert");
                            keys.put("linkUrl", "event:navigator/goto/" + roomChatMessage.getHabbo().getRoomUnit().getRoom().getId());

                            i.getClient().sendResponse(new BubbleAlertComposer("filter", keys));
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

    public void autoReportConsole(Habbo habbo, String roomChatMessage) {
        String message = normalise(roomChatMessage.toLowerCase());

        for (WordFilterWord word : this.autoReportWords) {
            if (message.contains(word.key)) {
                Emulator.getGameEnvironment().getModToolManager().quickTicket(habbo, "Automatic WordFilter", message);

                if (Emulator.getConfig().getBoolean("notify.staff.chat.auto.report")) {
                    Message chatMessage = new Message(habbo.getHabboInfo().getId(), -5, Emulator.getTexts().getValue("warning.auto.report").replace("%word%", word.key));
                    Emulator.getGameServer().getGameClientManager().sendBroadcastResponse((new FriendChatMessageComposer(chatMessage, -5, habbo.getHabboInfo().getId())).compose(), "acc_automatic_chat", habbo.getClient());

                    // Bubble Alert.
                    String[] ranks = Emulator.getConfig().getValue("kb.moderation.minrank").split(",");

                    for(Habbo i : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
                        int minRank = i.getHabboInfo().getRank().getId();

                        if (Arrays.asList(ranks).contains(String.valueOf(minRank))) {
                            THashMap<String, String> keys = new THashMap<>();
                            keys.put("display", "BUBBLE");
                            keys.put("message", Emulator.getTexts().getValue("kb.moderation.message").replace("%username%", habbo.getHabboInfo().getUsername()).replace("%filter%", word.key));
                            keys.put("image", Emulator.getConfig().getValue("kb.moderation.image"));
                            keys.put("sound", "filter_alert");
                            keys.put("linkUrl", "event:navigator/goto/" + habbo.getRoomUnit().getRoom().getId());

                            i.getClient().sendResponse(new BubbleAlertComposer("filter", keys));
                        }
                    }
                }

                break;
            }
        }
    }

    public boolean hideMessageCheck(String message) {
        message = normalise(message).toLowerCase();

        for (WordFilterWord word : this.hideMessageWords) {
            if (message.contains(word.key)) {
                return true;
            }
        }

        return false;
    }

    public String[] filter(String[] messages) {
        for (int i = 0; i < messages.length; i++) {
            messages[i] = this.filter(messages[i], null);
        }

        return messages;
    }

    public String filter(String message, Habbo habbo) {
        String filteredMessage = message;
        if (Emulator.getConfig().getBoolean("hotel.wordfilter.normalise")) {
            filteredMessage = normalise(filteredMessage);
        }

        TObjectHashIterator<WordFilterWord> iterator = this.words.iterator();

        boolean foundShit = false;

        while (iterator.hasNext()) {
            WordFilterWord word = iterator.next();
            if (StringUtils.containsIgnoreCase(filteredMessage, word.key)) {
                if (habbo != null) {
                    if (Emulator.getPluginManager().fireEvent(new UserTriggerWordFilterEvent(habbo, word)).isCancelled())
                        continue;
                }

                filteredMessage = "I am noob!";
                foundShit = true;

                if (habbo != null && word.muteTime > 0) {
                    habbo.mute(word.muteTime, false);
                }
            }
        }

        if (!foundShit) {
            return message;
        }

        return filteredMessage;
    }

    public void filter(RoomChatMessage roomChatMessage, Habbo habbo) {
        String message = roomChatMessage.getMessage().toLowerCase();

        if (Emulator.getConfig().getBoolean("hotel.wordfilter.normalise")) {
            message = normalise(message);
        }

        for (WordFilterWord word : this.words) {
            if (StringUtils.containsIgnoreCase(message, word.key)) {
                if (habbo != null) {
                    if (Emulator.getPluginManager().fireEvent(new UserTriggerWordFilterEvent(habbo, word)).isCancelled())
                        continue;
                }

                message = message.replace(word.key, word.replacement);
                roomChatMessage.filtered = true;
            }
        }

        if (roomChatMessage.filtered) {
            roomChatMessage.setMessage(message);
        }
    }

    public THashSet<WordFilterWord> getWords() {
        return new THashSet<>(this.words);
    }

    public void addWord(WordFilterWord word) {
        this.words.add(word);
    }
}
