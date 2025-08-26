package com.eu.habbo.messages.incoming.inventory;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RequestInventoryBadgeDelete extends MessageHandler {

    public int getRatelimit() {
        return 500;
    }

    public void handle() {
        String badgeCode = this.packet.readString();
        Habbo habbo = this.client.getHabbo();

        if (habbo == null) {
            return;
        }

        HabboBadge badge = habbo.getInventory().getBadgesComponent().getBadge(badgeCode);

        if (badge == null) {
            return;
        }

        habbo.deleteBadge(badge);
    }
}