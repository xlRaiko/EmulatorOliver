package com.eu.habbo.habbohotel.items.interactions.wired.extra;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredMatchFurniSetting;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WiredAddonOneCondition
extends InteractionWiredCondition {
    public int type = 1;
    public String key = "";
    public THashSet<WiredMatchFurniSetting> items = new THashSet();

    public WiredAddonOneCondition(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredAddonOneCondition(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public WiredConditionType getType() {
        return WiredConditionType.ONE_CONDITION;
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        this.type = settings.getIntParams()[0];
        this.key = settings.getStringParam();
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
        if (room == null) {
            return true;
        }
        int count = settings.getFurniIds().length;
        if (count > Emulator.getConfig().getInt("hotel.wired.furni.selection.count")) {
            return false;
        }
        this.items.clear();
        for (int i = 0; i < count; ++i) {
            int itemId = settings.getFurniIds()[i];
            HabboItem item = room.getHabboItem(itemId);
            if (!(item instanceof InteractionWiredCondition)) continue;
            this.items.add(new WiredMatchFurniSetting(item.getId(), item.getExtradata(), item.getRotation(), item.getX(), item.getY()));
        }
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.type, this.key, new ArrayList<WiredMatchFurniSetting>(this.items)));
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.items.size());
        for (WiredMatchFurniSetting item : this.items) {
            message.appendInt(item.item_id);
        }
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString(this.key);
        message.appendInt(1);
        message.appendInt(this.type);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            this.items.clear();
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.type = data.type;
            this.key = data.key;
            this.items.addAll(data.items);
        } else {
            this.type = 1;
            this.key = "0";
            this.items = new THashSet();
        }
    }

    @Override
    public void onPickUp() {
        this.items.clear();
        this.type = 1;
        this.key = "";
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {
    }

    static class JsonData {
        int type;
        String key;
        List<WiredMatchFurniSetting> items;

        public JsonData(int type, String key, List<WiredMatchFurniSetting> items) {
            this.type = type;
            this.key = key;
            this.items = items;
        }
    }
}