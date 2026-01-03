package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredTriggerReset;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WiredTriggerRepeaterShort extends InteractionWiredTrigger implements ICycleable, WiredTriggerReset {
    public static final int DEFAULT_DELAY = 50; // 50ms por defecto
    public static final int MIN_DELAY = 50;      // Mínimo 50ms
    public static final int MAX_DELAY = 500;     // Máximo 500ms
    public static final int CYCLE_INTERVAL = 50; // Ciclo cada 50ms
    
    private static final WiredTriggerType type = WiredTriggerType.PERIODICALLY_SHORT;
    private int repeatTime = DEFAULT_DELAY;
    private int counter = 0;

    public WiredTriggerRepeaterShort(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerRepeaterShort(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
            this.repeatTime
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");

        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.repeatTime = data.repeatTime;
        } else {
            if (!wiredData.isEmpty()) {
                this.repeatTime = (Integer.parseInt(wiredData));
            }
        }

        // Asegurar que está dentro del rango permitido
        if (this.repeatTime < MIN_DELAY) {
            this.repeatTime = MIN_DELAY;
        } else if (this.repeatTime > MAX_DELAY) {
            this.repeatTime = MAX_DELAY;
        }
    }

    @Override
    public void onPickUp() {
        this.repeatTime = DEFAULT_DELAY;
        this.counter = 0;
    }

    @Override
    public WiredTriggerType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(5);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(1);
        // Mostrar en unidades de 50ms para la interfaz
        message.appendInt(this.repeatTime / 50);
        message.appendInt(0);
        message.appendInt(this.getType().code);

        if (!this.isTriggeredByRoomUnit()) {
            List<Integer> invalidTriggers = new ArrayList<>();
            room.getRoomSpecialTypes().getEffects(this.getX(), this.getY()).forEach(object -> {
                if (object.requiresTriggeringUser()) {
                    invalidTriggers.add(object.getBaseItem().getSpriteId());
                }
                return true;
            });
            message.appendInt(invalidTriggers.size());
            for (Integer i : invalidTriggers) {
                message.appendInt(i);
            }
        } else {
            message.appendInt(0);
        }
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        if(settings.getIntParams().length < 1) return false;
        
        // Convertir de unidades de interfaz (múltiplos de 50ms) a milisegundos
        this.repeatTime = settings.getIntParams()[0] * 50;
        this.counter = 0;

        // Asegurar que está dentro del rango permitido
        if (this.repeatTime < MIN_DELAY) {
            this.repeatTime = MIN_DELAY;
        } else if (this.repeatTime > MAX_DELAY) {
            this.repeatTime = MAX_DELAY;
        }

        return true;
    }

    @Override
    public void cycle(Room room) {
        // Se asume que este método se llama cada CYCLE_INTERVAL ms (50ms)
        this.counter += CYCLE_INTERVAL;
        
        long currentMillis = System.currentTimeMillis();
        String key = Double.toString(this.getX()) + Double.toString(this.getY());

        room.repeatersLastTick.putIfAbsent(key, currentMillis);
        
        if (this.counter >= this.repeatTime && room.repeatersLastTick.get(key) < currentMillis - (this.repeatTime - CYCLE_INTERVAL)) {
            this.counter = 0;
            
            if (this.getRoomId() != 0) {
                if (room.isLoaded()) {
                    room.repeatersLastTick.put(key, currentMillis);
                    WiredHandler.handle(this, null, room, new Object[]{this});
                }
            }
        }
    }

    @Override
    public void resetTimer() {
        this.counter = 0;
        if (this.getRoomId() != 0) {
            Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
            if (room != null && room.isLoaded()) {
                WiredHandler.handle(this, null, room, new Object[]{this});
            }
        }
    }

    static class JsonData {
        int repeatTime;

        public JsonData(int repeatTime) {
            this.repeatTime = repeatTime;
        }
    }
}