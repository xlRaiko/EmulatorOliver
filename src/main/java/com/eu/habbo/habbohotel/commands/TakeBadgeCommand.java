package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.habbohotel.users.HabboInfo;

public class TakeBadgeCommand extends Command {
    public TakeBadgeCommand() {
        super("cmd_take_badge", Emulator.getTexts().getValue("commands.keys.cmd_take_badge").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length != 3) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue(params.length == 2 ? "commands.error.cmd_take_badge.forgot_badge" : "commands.error.cmd_take_badge.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }

        String username = params[1];
        String badge = params[2];

        HabboInfo habboInfo = HabboManager.getOfflineHabboInfo(username);
        if (habboInfo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_take_badge.not_found").replace("%username%", username), RoomChatMessageBubbles.ALERT);
            return true;
        }

        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(username);
        if (habbo != null) {
            HabboBadge badgeToRemove = habbo.getInventory().getBadgesComponent().getBadge(badge);
            if (badgeToRemove == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_take_badge.no_badge").replace("%username%", habbo.getHabboInfo().getUsername()).replace("%badge%", badge), RoomChatMessageBubbles.ALERT);
                return true;
            }

            habbo.deleteBadge(badgeToRemove);
        } else {
            int userId = habboInfo.getId();
            BadgesComponent.deleteBadge(userId, badge);
        }

        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_take_badge"), RoomChatMessageBubbles.ALERT);

        return true;
    }
}