package com.eu.habbo.habbohotel.items.interactions.wired.extra;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionOperator;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.messages.ServerMessage;

import gnu.trove.map.hash.THashMap;

public class WiredAddonAnimationDelay extends InteractionWiredCondition {
    
    public static final WiredConditionType type = WiredConditionType.MOVE_ANIMATION_DELAY;
    
    private int delayMs = 500; // Valor por defecto: 500ms
    private static final int MIN_DELAY = 50;  // 50ms mínimo
    private static final int MAX_DELAY = 2000; // 2000ms máximo
    
    // Mapa para almacenar el delay por sala
    private static final ConcurrentHashMap<Integer, Integer> roomDelays = new ConcurrentHashMap<>();
    // Mapa para almacenar el delay por item en cada sala
    private static final ConcurrentHashMap<String, Integer> itemDelays = new ConcurrentHashMap<>();
    
    public WiredAddonAnimationDelay(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.loadDelayFromExtraData();
    }

    public WiredAddonAnimationDelay(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.loadDelayFromExtraData();
    }
    
    private void loadDelayFromExtraData() {
        if (this.getExtradata() != null && !this.getExtradata().isEmpty()) {
            try {
                int savedDelay = Integer.parseInt(this.getExtradata());
                this.delayMs = Math.max(MIN_DELAY, Math.min(MAX_DELAY, savedDelay));
            } catch (NumberFormatException e) {
                this.delayMs = 500; // Valor por defecto si hay error
            }
        }
    }
    
    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (room == null) {
            return false;
        }
        
        // Establecer el delay de animación para esta sala
        roomDelays.put(room.getId(), this.delayMs);
        
        // También podemos aplicar el delay a items específicos si hay alguno en los parámetros
        if (stuff != null && stuff.length > 0) {
            for (Object obj : stuff) {
                if (obj instanceof Item) {
                    Item item = (Item) obj;
                    String key = room.getId() + "_" + item.getId();
                    itemDelays.put(key, this.delayMs);
                }
            }
        }
        
        return true; // Como condición, siempre retorna true para permitir la ejecución
    }
    
    // Método estático para obtener el delay de animación de una sala
    public static int getAnimationDelayForRoom(Room room) {
        if (room == null) return 0;
        Integer delay = roomDelays.get(room.getId());
        return delay != null ? delay : 0;
    }
    
    // Método estático para obtener el delay de un item específico
    public static int getAnimationDelayForItem(Room room, int itemId) {
        if (room == null) return 0;
        String key = room.getId() + "_" + itemId;
        Integer delay = itemDelays.get(key);
        if (delay != null) return delay;
        
        // Si no hay delay específico para el item, usar el de la sala
        return getAnimationDelayForRoom(room);
    }
    
    // Método para limpiar delays cuando se descarga una sala
    public static void cleanupRoom(int roomId) {
        roomDelays.remove(roomId);
        // Limpiar todos los items de esta sala
        itemDelays.keySet().removeIf(key -> key.startsWith(roomId + "_"));
    }
    
    public int getDelayMs() {
        return this.delayMs;
    }
    
    public void setDelayMs(int delayMs) {
        this.delayMs = Math.max(MIN_DELAY, Math.min(MAX_DELAY, delayMs));
        this.setExtradata(String.valueOf(this.delayMs));
        this.needsUpdate(true);
    }

    @Override
    public String getWiredData() {
        return String.valueOf(this.delayMs);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        try {
            message.appendBoolean(false); // isTrigger
            message.appendInt(0); // Número de selecciones
            
            // Configuración: delay en ms
            THashMap<String, String> config = new THashMap<>();
            config.put("delay", String.valueOf(this.delayMs));
            
            message.appendInt(config.size()); // número de valores de configuración
            for (Map.Entry<String, String> entry : config.entrySet()) {
                message.appendString(entry.getKey()); // clave de configuración
                message.appendString(entry.getValue()); // valor de configuración
            }
            
            message.appendInt(0); // selecciones count
            message.appendInt(WiredConditionType.MOVE_ANIMATION_DELAY.code); // tipo de condición
            
        } catch (Exception e) {
            Emulator.getLogging().logErrorLine(e);
        }
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        try {
            String wiredData = set.getString("wired_data");
            if (wiredData != null && !wiredData.trim().isEmpty()) {
                int savedDelay = Integer.parseInt(wiredData.trim());
                this.setDelayMs(savedDelay);
            }
        } catch (Exception e) {
            Emulator.getLogging().logErrorLine("Error loading WiredConditionAnimationDelay data: " + e.getMessage());
        }
    }

    public boolean saveData(ServerMessage message) {
        try {
            // Assuming the message contains delay value directly or needs to be parsed differently
            String wiredData = message.toString();
            if (wiredData != null && !wiredData.trim().isEmpty()) {
                try {
                    int newDelay = Integer.parseInt(wiredData.trim());
                    this.setDelayMs(newDelay);
                } catch (NumberFormatException e) {
                    // Mantener valor actual si es inválido
                }
            }
            
            return true;
        } catch (Exception e) {
            Emulator.getLogging().logErrorLine("Error saving WiredConditionAnimationDelay: " + e.getMessage());
            return false;
        }
    }

    @Override
    public WiredConditionType getType() {
        return WiredConditionType.MOVE_ANIMATION_DELAY;
    }
    
    @Override
    public WiredConditionOperator operator() {
        return WiredConditionOperator.AND;
    }

    @Override
    public void onPickUp() {
        // Resetear al recoger el wired
        this.delayMs = 500;
        this.setExtradata("500");
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {
        // No se necesita implementación
    }
    
    public boolean requiresActingUser() {
        return false; // No requiere usuario específico
    }

    @Override
    public boolean saveData(WiredSettings settings) {
    try {
        // Usamos el primer parámetro entero como delay
        int newDelay = settings.getIntParams().length > 0
                ? settings.getIntParams()[0]
                : this.delayMs;

        // Limitar valores
        newDelay = Math.max(MIN_DELAY, Math.min(MAX_DELAY, newDelay));

        this.delayMs = newDelay;

        // Guardar en extradata (OBLIGATORIO)
        this.setExtradata(String.valueOf(this.delayMs));

        this.needsUpdate(true);
        return true;
    } catch (Exception e) {
        Emulator.getLogging().logErrorLine(e);
        return false;
    }
}

}