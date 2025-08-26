package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.modtool.ModToolBan;
import com.eu.habbo.habbohotel.modtool.ModToolBanType;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;

import java.util.Objects;

public class BanCommand extends Command {
    public final static int TEN_YEARS = 315569260;

    public BanCommand() {
        super("cmd_ban", Emulator.getTexts().getValue("commands.keys.cmd_ban").split(";"));
    }

    public static boolean IsInteger(String s) {
        try{
            Integer.parseInt(s);
        }catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.not_specified"));
            return true;
        }

        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(params[1]);
        HabboInfo target;

        if (habbo != null)
            target = habbo.getHabboInfo();
        else
            target = HabboManager.getOfflineHabboInfo(params[1]);

        if (target == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.not_found").replace("%username%", params[1]));
            return true;
        } else {
            if (habbo == gameClient.getHabbo()) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.self"));
                return true;
            }

            if (target.getRank().getId() >= gameClient.getHabbo().getHabboInfo().getRank().getId()) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.rank").replace("%username%", params[1]));
                return true;
            }

            int minutes = TEN_YEARS; // Default time.
            String initial = "∞";

            if (params.length >= 3) {
                if (!Objects.equals(params[2].toUpperCase(), "PUB")) {
                    if (IsInteger(params[2])) {
                        minutes = Integer.parseInt(params[2]) * 60;
                        initial = params[2];
                    } else {
                        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_ban.forgot_time"));
                        return true;
                    }
                }
            }

            StringBuilder reason = new StringBuilder();

            if (params.length >= 4) {
                for (int i = 3; i < params.length; i++) {
                    reason.append(params[i]).append(" ");
                }
            } else {
                reason.append("Sin razón especificada. ");
            }

            Emulator.getGameEnvironment().getModToolManager().ban(target.getId(), gameClient.getHabbo(), reason.substring(0, reason.length()-1), minutes, ModToolBanType.ACCOUNT, -1);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.success.cmd_ban").replace("%username%", target.getUsername()).replace("%reason%", reason).replace("%minutes%", initial), RoomChatMessageBubbles.ALERT);
        }

        return true;
    }
}