package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionDice;
import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.Event;
import com.eu.habbo.plugin.events.furniture.FurnitureSingleClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleClickItemEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleClickItemEvent.class);

    @Override
    public void handle() throws Exception {
        try {
            Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();

            if (room == null) {
                return;
            }

            int itemId = this.packet.readInt();
            int state = this.packet.readInt();

            HabboItem item = room.getHabboItem(itemId);

            if (item == null || item instanceof InteractionDice) {
                return;
            }

            Event furnitureSingleClickEvent = new FurnitureSingleClickEvent(item, this.client.getHabbo(), state);
            Emulator.getPluginManager().fireEvent(furnitureSingleClickEvent);

            if (furnitureSingleClickEvent.isCancelled()) {
                return;
            }

            if (item instanceof InteractionWired) {
                this.client.getHabbo().getRoomUnit().setGoalLocation(this.client.getHabbo().getRoomUnit().getCurrentLocation());
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }
}
