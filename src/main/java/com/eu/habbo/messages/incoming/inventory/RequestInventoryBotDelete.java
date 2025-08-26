package com.eu.habbo.messages.incoming.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.InventoryBotsComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;

public class RequestInventoryBotDelete extends MessageHandler {

    public int getRatelimit() {
        return 500;
    }

    public void handle() {
        int botId = Math.abs(this.packet.readInt());
        Habbo habbo = this.client.getHabbo();

        if (habbo == null) {
            return;
        }

        Bot bot = habbo.getInventory().getBotsComponent().getBot(botId);

        if (bot == null) {
            return;
        }

        habbo.getInventory().getBotsComponent().removeBot(bot);
        Emulator.getGameEnvironment().getBotManager().deleteBot(bot);

        habbo.getClient().sendResponse(new InventoryRefreshComposer());
        habbo.getClient().sendResponse(new InventoryBotsComposer(habbo));
    }
}