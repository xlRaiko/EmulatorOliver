package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class WiredTriggerStateSelection extends InteractionWiredTrigger {
    private static final WiredTriggerType type = WiredTriggerType.STATE_SELECTION;
    public THashSet<HabboItem> items = new THashSet();
    private boolean onlyCurrentState = true; // Opción: Activar apenas no estado atual
    private int targetState = -1; // Estado objetivo para comparar

    public WiredTriggerStateSelection(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerStateSelection(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (stuff == null || stuff.length == 0) {
            return false;
        }
        
        // Verificar si se trata de un cambio de estado
        if (stuff[0] instanceof HabboItem) {
            HabboItem triggeredItem = (HabboItem) stuff[0];
            
            // Verificar si el item está en la lista de items seleccionados
            if (!this.items.contains(triggeredItem)) {
                return false;
            }
            
            // Obtener el estado actual del item
            int currentState;
            try {
                currentState = Integer.parseInt(triggeredItem.getExtradata());
            } catch (NumberFormatException e) {
                return false;
            }
            
            // Si la opción "Activar en todos los estados" está seleccionada
            if (!onlyCurrentState) {
                return true;
            }
            
            // Si la opción "Activar apenas no estado atual" está seleccionada
            if (targetState == -1) {
                // Primera ejecución, guardar el estado actual como objetivo
                targetState = currentState;
                return true;
            } else {
                // Comparar con el estado objetivo
                return currentState == targetState;
            }
        }
        
        return false;
    }

    @Override
    public String getWiredData() {
        JsonData data = new JsonData(
            this.items.stream().map(HabboItem::getId).collect(Collectors.toList()),
            this.onlyCurrentState,
            this.targetState
        );
        return WiredHandler.getGsonBuilder().create().toJson(data);
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        this.items = new THashSet();
        
        try {
            if (wiredData.startsWith("{")) {
                // Formato JSON (nuevo)
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.onlyCurrentState = data.onlyCurrentState;
                this.targetState = data.targetState;
                
                for (Integer id : data.itemIds) {
                    HabboItem item = room.getHabboItem(id);
                    if (item == null) continue;
                    this.items.add(item);
                }
            } else if (wiredData.contains(":")) {
                // Formato antiguo (compatible)
                String[] parts = wiredData.split(":");
                if (parts.length > 0) {
                    try {
                        super.setDelay(Integer.parseInt(parts[0]));
                    } catch (NumberFormatException ignored) {}
                }
                
                // Cargar opciones por defecto
                this.onlyCurrentState = true;
                this.targetState = -1;
                
                if (parts.length >= 3 && !parts[2].equals("\t")) {
                    for (String s : parts[2].split(";")) {
                        try {
                            HabboItem item = room.getHabboItem(Integer.parseInt(s));
                            if (item == null) continue;
                            this.items.add(item);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            // En caso de error, usar valores por defecto
            this.onlyCurrentState = true;
            this.targetState = -1;
        }
    }

    @Override
    public void onPickUp() {
        this.items.clear();
        this.onlyCurrentState = true;
        this.targetState = -1;
    }

    @Override
    public WiredTriggerType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        // Limpiar items que ya no existen en la sala
        THashSet<HabboItem> itemsToRemove = new THashSet<HabboItem>();
        for (HabboItem item : this.items) {
            if (item.getRoomId() != this.getRoomId() || room.getHabboItem(item.getId()) == null) {
                itemsToRemove.add(item);
            }
        }
        for (HabboItem item : itemsToRemove) {
            this.items.remove(item);
        }
        
        // Serializar datos del wired
        message.appendBoolean(false); // ¿Tiene delay?
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION); // Máximo de furnis seleccionables (20)
        message.appendInt(this.items.size()); // Cantidad de furnis seleccionados
        
        // IDs de los furnis seleccionados
        for (HabboItem item : this.items) {
            message.appendInt(item.getId());
        }
        
        message.appendInt(this.getBaseItem().getSpriteId()); // Sprite ID del wired
        message.appendInt(this.getId()); // ID del wired
        message.appendString(""); // Texto adicional (no usado)
        message.appendInt(0); // Delay (no usado)
        message.appendInt(0); // Unknown
        message.appendInt(this.getType().code); // Código del tipo de trigger (debe ser STATE_SELECTION)
        
        // IMPORTANTE: En este emulador, las opciones se envían en un formato diferente
        // No usamos appendInt(2) y luego las opciones individuales como antes
        // En su lugar, configuramos la cantidad de opciones y las opciones en sí
        
        // Para 2 opciones (casillas de verificación):
        message.appendInt(2); // Número de opciones
        
        // Opción 1: "Ativar apenas no estado atual" (ID 0)
        message.appendInt(0);
        message.appendInt(this.onlyCurrentState ? 1 : 0);
        
        // Opción 2: "Ativar en todos los estados" (ID 1)
        message.appendInt(1);
        message.appendInt(!this.onlyCurrentState ? 1 : 0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        this.items.clear();
        
        // Guardar los furnis seleccionados
        int[] furniIds = settings.getFurniIds();
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
        if (room != null) {
            for (int furniId : furniIds) {
                HabboItem item = room.getHabboItem(furniId);
                if (item != null) {
                    this.items.add(item);
                }
            }
        }
        
        // LEER LAS OPCIONES DE LAS CASILLAS DE VERIFICACIÓN
        // En este emulador, las opciones vienen en intParams
        // intParams[0] = valor de la primera opción (0 o 1)
        // intParams[1] = valor de la segunda opción (0 o 1)
        int[] intParams = settings.getIntParams();
        
        if (intParams != null && intParams.length >= 2) {
            // Tenemos dos opciones excluyentes:
            // - Si intParams[0] = 1 -> "Ativar apenas no estado atual"
            // - Si intParams[1] = 1 -> "Ativar en todos los estados"
            
            if (intParams[1] == 1) {
                this.onlyCurrentState = false; // "Activar en todos los estados"
            } else {
                this.onlyCurrentState = true; // "Ativar apenas no estado atual" (por defecto)
            }
        } else {
            // Si no hay parámetros, usar valor por defecto
            this.onlyCurrentState = true;
        }
        
        // Reiniciar el estado objetivo cuando se guarda nueva configuración
        this.targetState = -1;
        
        return true;
    }

    @Override
    public boolean isTriggeredByRoomUnit() {
        return false; // Este trigger se activa por cambio de estado del furni, no por acción del usuario
    }

    public boolean requiresTriggeringUser() {
        return false;
    }

    // Clase interna para almacenar datos en formato JSON
    static class JsonData {
        List<Integer> itemIds;
        boolean onlyCurrentState;
        int targetState;

        public JsonData(List<Integer> itemIds, boolean onlyCurrentState, int targetState) {
            this.itemIds = itemIds;
            this.onlyCurrentState = onlyCurrentState;
            this.targetState = targetState;
        }
        
        // Constructor vacío necesario para Gson
        public JsonData() {
        }
    }
}