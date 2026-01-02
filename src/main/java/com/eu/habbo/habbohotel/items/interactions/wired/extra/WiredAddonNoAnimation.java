package com.eu.habbo.habbohotel.items.interactions.wired.extra;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredExtra;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemUpdateComposer;

import gnu.trove.set.hash.THashSet;

public class WiredAddonNoAnimation extends InteractionWiredExtra {

    public WiredAddonNoAnimation(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredAddonNoAnimation(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    /**
     * Ejecuta el addon: quita animaciones de movimiento de todos los muebles seleccionados.
     */
    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (room == null) return false;

        // Obtiene los muebles seleccionados por el wired (puedes cambiar esta lógica si quieres todos los muebles)
        THashSet<HabboItem> itemsToMove = new THashSet<>();
        room.getFloorItems().stream()
                .filter(item -> item != null)
                .forEach(itemsToMove::add);

        for (HabboItem item : itemsToMove) {
            // Mover el mueble a su posición actual sin animación
            RoomTile tile = room.getLayout().getTile(item.getX(), item.getY());
            if (tile != null && tile.state != RoomTileState.INVALID) {
                double z = room.getStackHeight(tile.x, tile.y, false, item);

                // Cambio directo de coordenadas
                item.setX(tile.x);
                item.setY(tile.y);
                item.setZ(z);

                // Actualiza el cliente inmediatamente
                room.sendComposer(new FloorItemUpdateComposer(item).compose());
                item.needsUpdate(true);
            }
        }

        return true;
    }

    @Override
    public String getWiredData() {
        return "";
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) {
    }

    @Override
    public void onPickUp() {
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) {
    }
}
