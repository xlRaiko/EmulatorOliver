package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

public class WiredEffectUserMoveRotate extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.USER_MOVE_ROTATE;
    
    // Opciones de movimiento (8 direcciones posibles)
    private boolean moveNorth = false;      // Arriba
    private boolean moveSouth = false;      // Abajo
    private boolean moveEast = false;       // Derecha
    private boolean moveWest = false;       // Izquierda
    private boolean moveNorthEast = false;  // Arriba-Derecha
    private boolean moveNorthWest = false;  // Arriba-Izquierda
    private boolean moveSouthEast = false;  // Abajo-Derecha
    private boolean moveSouthWest = false;  // Abajo-Izquierda
    
    // Opciones de rotación (8 direcciones posibles)
    private boolean rotateNorth = false;      // 0
    private boolean rotateSouth = false;      // 4
    private boolean rotateEast = false;       // 2
    private boolean rotateWest = false;       // 6
    private boolean rotateNorthEast = false;  // 1
    private boolean rotateNorthWest = false;  // 7
    private boolean rotateSouthEast = false;  // 3
    private boolean rotateSouthWest = false;  // 5
    
    // Opciones especiales de rotación
    private boolean rotateClockwise = false;        // Rotar en sentido horario
    private boolean rotateCounterClockwise = false; // Rotar en sentido antihorario

    public WiredEffectUserMoveRotate(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectUserMoveRotate(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (roomUnit == null || room == null) {
            return false;
        }

        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null) {
            return false;
        }

        // PASO 1: Mover al usuario según las opciones seleccionadas
        int newX = roomUnit.getX();
        int newY = roomUnit.getY();
        
        if (moveNorth) {
            newY--;
        }
        if (moveSouth) {
            newY++;
        }
        if (moveEast) {
            newX++;
        }
        if (moveWest) {
            newX--;
        }
        if (moveNorthEast) {
            newX++;
            newY--;
        }
        if (moveNorthWest) {
            newX--;
            newY--;
        }
        if (moveSouthEast) {
            newX++;
            newY++;
        }
        if (moveSouthWest) {
            newX--;
            newY++;
        }

        // Verificar si la nueva posición es válida
        if (newX != roomUnit.getX() || newY != roomUnit.getY()) {
            RoomTile newTile = room.getLayout().getTile((short) newX, (short) newY);
            
            if (newTile != null && newTile.isWalkable() && !newTile.hasUnits()) {
                // Mover al usuario instantáneamente
                roomUnit.setLocation(newTile);
                roomUnit.setZ(newTile.getStackHeight());
                roomUnit.setPreviousLocation(room.getLayout().getTile(roomUnit.getX(), roomUnit.getY()));
                
                // Actualizar posición en el cliente
                room.sendComposer(new RoomUserStatusComposer(roomUnit).compose());
            }
        }

        // PASO 2: Rotar al usuario según las opciones seleccionadas
        int newRotation = roomUnit.getBodyRotation().getValue();
        
        if (rotateNorth) {
            newRotation = 0;
        } else if (rotateNorthEast) {
            newRotation = 1;
        } else if (rotateEast) {
            newRotation = 2;
        } else if (rotateSouthEast) {
            newRotation = 3;
        } else if (rotateSouth) {
            newRotation = 4;
        } else if (rotateSouthWest) {
            newRotation = 5;
        } else if (rotateWest) {
            newRotation = 6;
        } else if (rotateNorthWest) {
            newRotation = 7;
        } else if (rotateClockwise) {
            // Rotar 45 grados en sentido horario
            newRotation = (roomUnit.getBodyRotation().getValue() + 1) % 8;
        } else if (rotateCounterClockwise) {
            // Rotar 45 grados en sentido antihorario
            newRotation = (roomUnit.getBodyRotation().getValue() - 1 + 8) % 8;
        }

        // Aplicar la rotación
        roomUnit.setRotation(com.eu.habbo.habbohotel.rooms.RoomUserRotation.values()[newRotation]);
        roomUnit.setHeadRotation(com.eu.habbo.habbohotel.rooms.RoomUserRotation.values()[newRotation]);
        
        // Actualizar rotación en el cliente
        room.sendComposer(new RoomUserStatusComposer(roomUnit).compose());

        return true;
    }

    @Override
    public String getWiredData() {
        StringBuilder data = new StringBuilder();
        
        // Guardar opciones de movimiento
        data.append(moveNorth ? "1" : "0").append(",");
        data.append(moveSouth ? "1" : "0").append(",");
        data.append(moveEast ? "1" : "0").append(",");
        data.append(moveWest ? "1" : "0").append(",");
        data.append(moveNorthEast ? "1" : "0").append(",");
        data.append(moveNorthWest ? "1" : "0").append(",");
        data.append(moveSouthEast ? "1" : "0").append(",");
        data.append(moveSouthWest ? "1" : "0").append(";");
        
        // Guardar opciones de rotación
        data.append(rotateNorth ? "1" : "0").append(",");
        data.append(rotateSouth ? "1" : "0").append(",");
        data.append(rotateEast ? "1" : "0").append(",");
        data.append(rotateWest ? "1" : "0").append(",");
        data.append(rotateNorthEast ? "1" : "0").append(",");
        data.append(rotateNorthWest ? "1" : "0").append(",");
        data.append(rotateSouthEast ? "1" : "0").append(",");
        data.append(rotateSouthWest ? "1" : "0").append(",");
        data.append(rotateClockwise ? "1" : "0").append(",");
        data.append(rotateCounterClockwise ? "1" : "0");
        
        return data.toString();
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        
        if (wiredData != null && !wiredData.isEmpty()) {
            try {
                String[] parts = wiredData.split(";");
                
                if (parts.length >= 2) {
                    // Cargar opciones de movimiento
                    String[] movementOptions = parts[0].split(",");
                    if (movementOptions.length >= 8) {
                        moveNorth = movementOptions[0].equals("1");
                        moveSouth = movementOptions[1].equals("1");
                        moveEast = movementOptions[2].equals("1");
                        moveWest = movementOptions[3].equals("1");
                        moveNorthEast = movementOptions[4].equals("1");
                        moveNorthWest = movementOptions[5].equals("1");
                        moveSouthEast = movementOptions[6].equals("1");
                        moveSouthWest = movementOptions[7].equals("1");
                    }
                    
                    // Cargar opciones de rotación
                    String[] rotationOptions = parts[1].split(",");
                    if (rotationOptions.length >= 10) {
                        rotateNorth = rotationOptions[0].equals("1");
                        rotateSouth = rotationOptions[1].equals("1");
                        rotateEast = rotationOptions[2].equals("1");
                        rotateWest = rotationOptions[3].equals("1");
                        rotateNorthEast = rotationOptions[4].equals("1");
                        rotateNorthWest = rotationOptions[5].equals("1");
                        rotateSouthEast = rotationOptions[6].equals("1");
                        rotateSouthWest = rotationOptions[7].equals("1");
                        rotateClockwise = rotationOptions[8].equals("1");
                        rotateCounterClockwise = rotationOptions[9].equals("1");
                    }
                }
            } catch (Exception e) {
                Emulator.getLogging().logErrorLine("Error loading WiredEffectUserMoveRotate data: " + e.getMessage());
            }
        }
    }

    @Override
    public void onPickUp() {
        // Resetear todas las opciones
        moveNorth = moveSouth = moveEast = moveWest = false;
        moveNorthEast = moveNorthWest = moveSouthEast = moveSouthWest = false;
        rotateNorth = rotateSouth = rotateEast = rotateWest = false;
        rotateNorthEast = rotateNorthWest = rotateSouthEast = rotateSouthWest = false;
        rotateClockwise = rotateCounterClockwise = false;
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
        
        // Enviar opciones al cliente
        message.appendInt(18); // Número total de opciones
        
        // Opciones de movimiento (8)
        message.appendInt(moveNorth ? 1 : 0);
        message.appendInt(moveSouth ? 1 : 0);
        message.appendInt(moveEast ? 1 : 0);
        message.appendInt(moveWest ? 1 : 0);
        message.appendInt(moveNorthEast ? 1 : 0);
        message.appendInt(moveNorthWest ? 1 : 0);
        message.appendInt(moveSouthEast ? 1 : 0);
        message.appendInt(moveSouthWest ? 1 : 0);
        
        // Opciones de rotación (10)
        message.appendInt(rotateNorth ? 1 : 0);
        message.appendInt(rotateSouth ? 1 : 0);
        message.appendInt(rotateEast ? 1 : 0);
        message.appendInt(rotateWest ? 1 : 0);
        message.appendInt(rotateNorthEast ? 1 : 0);
        message.appendInt(rotateNorthWest ? 1 : 0);
        message.appendInt(rotateSouthEast ? 1 : 0);
        message.appendInt(rotateSouthWest ? 1 : 0);
        message.appendInt(rotateClockwise ? 1 : 0);
        message.appendInt(rotateCounterClockwise ? 1 : 0);
    }

    public boolean saveData(WiredSettings settings, ClientMessage packet) throws WiredSaveException {
        packet.readInt(); // Delay
        
        // Leer opciones de movimiento (8 opciones)
        moveNorth = packet.readInt() == 1;
        moveSouth = packet.readInt() == 1;
        moveEast = packet.readInt() == 1;
        moveWest = packet.readInt() == 1;
        moveNorthEast = packet.readInt() == 1;
        moveNorthWest = packet.readInt() == 1;
        moveSouthEast = packet.readInt() == 1;
        moveSouthWest = packet.readInt() == 1;
        
        // Leer opciones de rotación (10 opciones)
        rotateNorth = packet.readInt() == 1;
        rotateSouth = packet.readInt() == 1;
        rotateEast = packet.readInt() == 1;
        rotateWest = packet.readInt() == 1;
        rotateNorthEast = packet.readInt() == 1;
        rotateNorthWest = packet.readInt() == 1;
        rotateSouthEast = packet.readInt() == 1;
        rotateSouthWest = packet.readInt() == 1;
        rotateClockwise = packet.readInt() == 1;
        rotateCounterClockwise = packet.readInt() == 1;
        
        return true;
    }

    @Override
    protected long requiredCooldown() {
        return 500L; // 500ms de cooldown
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        throw new UnsupportedOperationException("Fallo en el wired WiredEffectUserMoveRotate");
    }
}