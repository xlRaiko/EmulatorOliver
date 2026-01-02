package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionTileClick;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerFurniStateToggled;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredTriggerTileClick
extends WiredTriggerFurniStateToggled {
    public WiredTriggerTileClick(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerTileClick(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        return super.execute(roomUnit, room, stuff);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        this.items.clear();
        int count = settings.getFurniIds().length;
        for (int i = 0; i < count; ++i) {
            HabboItem habboItem = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(settings.getFurniIds()[i]);
            if (!(habboItem instanceof InteractionTileClick)) continue;
            this.items.add(habboItem);
        }
        return true;
    }

    @Override
    public WiredTriggerType getType() {
        return WiredTriggerType.CLICK_TILE;
    }
}