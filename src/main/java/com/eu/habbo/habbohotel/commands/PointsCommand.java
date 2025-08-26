package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;

public class PointsCommand extends Command {
    public PointsCommand() {
        super("cmd_points", Emulator.getTexts().getValue("commands.keys.cmd_points").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length >= 3) {
            HabboInfo info = HabboManager.getOfflineHabboInfo(params[1]);
            if (info != null) {
                Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(params[1]);

                int amount = 0;
                int type = Emulator.getConfig().getInt("seasonal.primary.type");

                try {
                    amount = Integer.parseInt(params[2]);
                } catch (Exception e) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_amount"));
                    return true;
                }

                if (params.length == 4) {
                    try {
                        type = Integer.parseInt(params[3]);
                    } catch (Exception e) {
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_type").replace("%types%", Emulator.getConfig().getValue("seasonal.types").replace(";", ", ")));
                        return true;
                    }
                }

                if (habbo != null) {
                    if (amount != 0) {
                        habbo.givePoints(type, amount);

                        if (habbo.getHabboInfo().getCurrentRoom() != null) {
                            habbo.whisper(Emulator.getTexts().getValue("commands.generic.cmd_points.received").replace("%amount%", String.valueOf(amount)).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + type)), RoomChatMessageBubbles.SYSTEM);
                        } else {
                            habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_points.received").replace("%amount%", String.valueOf(amount)).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + type)));
                        }

                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_points.send").replace("%amount%", String.valueOf(amount)).replace("%user%", params[1]).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + type)), RoomChatMessageBubbles.SYSTEM);

                    } else {
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_amount"));
                    }
                } else {
                    Emulator.getGameEnvironment().getHabboManager().givePoints(info.getId(), type, amount);
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_points.send").replace("%amount%", String.valueOf(amount)).replace("%user%", params[1]).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + type)), RoomChatMessageBubbles.SYSTEM);
                }
            } else {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.notfound"));
            }
        } else {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_amount"));
        }

        return true;
    }
}
