package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;
import com.eu.habbo.plugin.Event;
import com.eu.habbo.plugin.events.users.UserClickEvent;

public class RequestWearingBadgesEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        int userId = this.packet.readInt();
        int targetId = this.packet.readInt();

        Habbo target = Emulator.getGameServer().getGameClientManager().getHabbo(targetId);

        if (target == null || target.getHabboInfo() == null || target.getInventory() == null || target.getInventory().getBadgesComponent() == null) {
            this.client.sendResponse(new UserBadgesComposer(BadgesComponent.getBadgesOfflineHabbo(targetId), targetId));
        } else {
            Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(userId);

            Event userClickEvent = new UserClickEvent(habbo, target);
            Emulator.getPluginManager().fireEvent(userClickEvent);

            if (userClickEvent.isCancelled()) {
                return;
            }

            this.client.sendResponse(new UserBadgesComposer(target.getInventory().getBadgesComponent().getWearingBadges(), target.getHabboInfo().getId()));
        }
    }
}
