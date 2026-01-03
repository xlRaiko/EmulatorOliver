package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;

import gnu.trove.set.hash.THashSet;

public class WiredEffectAltitude extends InteractionWiredEffect {

    public static final WiredEffectType type = WiredEffectType.ALTITUDE;

    private final THashSet<HabboItem> items = new THashSet<>();
    private int mode = 0;
    private double height = 0.0;

    public WiredEffectAltitude(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectAltitude(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (room == null || this.items.isEmpty()) {
            return false;
        }

        for (HabboItem item : this.items) {
            double currentZ = item.getZ();
            double targetZ;

            switch (this.mode) {
                case 0: // Aumentar
                    targetZ = currentZ + this.height;
                    break;

                case 1: // Disminuir
                    targetZ = currentZ - this.height;
                    break;

                case 2: // Establecer
                    targetZ = this.height;
                    break;

                default:
                    continue;
            }

            targetZ = Math.max(0.0, Math.min(60.0, targetZ));

            room.sendComposer(
                new FloorItemOnRollerComposer(
                    item,
                    null,
                    room.getLayout().getTile(item.getX(), item.getY()),
                    targetZ - currentZ,
                    room
                ).compose()
            );
        }

        return true;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.items.size());

        for (HabboItem item : this.items) {
            message.appendInt(item.getId());
        }

        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");

        message.appendInt(2);
        message.appendInt(this.mode);
        message.appendInt((int) (this.height * 100));

        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    public boolean saveData(WiredSettings settings) {

        Room room = Emulator.getGameEnvironment()
                .getRoomManager()
                .getRoom(this.getRoomId());

        this.items.clear();

        if (room != null) {
            for (int id : settings.getFurniIds()) {
                HabboItem item = room.getHabboItem(id);
                if (item != null) {
                    this.items.add(item);
                }
            }
        }

        int[] params = settings.getIntParams();

        if (params.length > 0) {
            this.mode = Math.max(0, Math.min(2, params[0]));
        } else {
            this.mode = 0;
        }

        if (params.length > 1) {
            this.height = params[1] / 100.0;
        } else {
            this.height = 0.0;
        }

        this.height = Math.max(0.0, Math.min(60.0, this.height));

        this.setDelay(settings.getDelay());
        return true;
    }

    @Override
    public void onPickUp() {
        this.items.clear();
        this.mode = 0;
        this.height = 0.0;
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    @Override
    public boolean requiresTriggeringUser() {
        return false;
    }

    @Override
public String getWiredData() {
    THashSet<Integer> itemIds = new THashSet<>();
    for (HabboItem item : this.items) {
        itemIds.add(item.getId());
    }
    
    JsonData data = new JsonData(
        itemIds,
        this.mode,
        this.height,
        this.getDelay()
    );
    
    return WiredHandler.getGsonBuilder().create().toJson(data);
}

@Override
public void loadWiredData(ResultSet set, Room room) throws SQLException {
    this.items.clear();
    
    String wiredData = set.getString("wired_data");
    if (wiredData != null && wiredData.startsWith("{")) {
        try {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.mode = data.mode;
            this.height = data.height;
            this.setDelay(data.delay);
            
            for (Integer itemId : data.itemIds) {
                HabboItem item = room.getHabboItem(itemId);
                if (item != null) {
                    this.items.add(item);
                }
            }
        } catch (Exception e) {
            Emulator.getLogging().logErrorLine("Error loading WiredEffectAltitude data: " + e.getMessage());
        }
    }
}

@Override
public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
    return this.saveData(settings); // Reutiliza el método existente
}

// Clase interna para JSON
static class JsonData {
    THashSet<Integer> itemIds;
    int mode;
    double height;
    int delay;
    
    public JsonData(THashSet<Integer> itemIds, int mode, double height, int delay) {
        this.itemIds = itemIds;
        this.mode = mode;
        this.height = height;
        this.delay = delay;
    }
}

}