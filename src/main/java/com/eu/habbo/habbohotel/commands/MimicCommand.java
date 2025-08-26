package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.eu.habbo.util.figure.FigureUtil;

public class MimicCommand extends Command {
    public MimicCommand() {
        super("cmd_mimic", Emulator.getTexts().getValue("commands.keys.cmd_mimic").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length == 2) {
            HabboInfo info = HabboManager.getOfflineHabboInfo(params[1]);

            if (info != null) {
                if (info.getId() == gameClient.getHabbo().getHabboInfo().getId()) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.not_self"));
                    return true;
                } else {
                    if (info.getRank().hasPermission(Permission.ACC_NOT_MIMICED, false) && !gameClient.getHabbo().hasPermission(Permission.ACC_NOT_MIMICED)) {
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.blocked").replace("%username%", params[1]));
                        return true;
                    }

                    if (!gameClient.getHabbo().hasPermission("acc_mimic_unredeemed") && FigureUtil.hasBlacklistedClothing(info.getLook(), gameClient.getHabbo().getForbiddenClothing())) {
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.forbidden_clothing"));
                        return true;
                    }

                    gameClient.getHabbo().getHabboInfo().setLook(info.getLook());
                    gameClient.getHabbo().getHabboInfo().setGender(info.getGender());
                    gameClient.sendResponse(new UserDataComposer(gameClient.getHabbo()));
                    gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(gameClient.getHabbo()).compose());
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_mimic.copied").replace("%username%", params[1]), RoomChatMessageBubbles.SYSTEM);
                }
            } else {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.not_found").replace("%username%", params[1]));
            }

        } else {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.usage"));
        }

        return true;
    }
}
