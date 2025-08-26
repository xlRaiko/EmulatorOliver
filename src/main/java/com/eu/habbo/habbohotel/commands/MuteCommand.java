package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserIgnoredComposer;

public class MuteCommand extends Command {
    public MuteCommand() {
        super("cmd_mute", Emulator.getTexts().getValue("commands.keys.cmd_mute").split(";"));
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
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.not_specified"));
            return true;
        }

        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(params[1]);
        if(habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.not_found").replace("%username%", params[1]));
            return true;
        } else {
            if (habbo == gameClient.getHabbo()) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.self"));
                return true;
            }

            if (habbo.getHabboInfo().getRank().getId() >= gameClient.getHabbo().getHabboInfo().getRank().getId()) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.rank"));
                return true;
            }

            int minutes = 5*60; // Default time.
            String initial = "5";

            if(params.length >= 3) {
                if(IsInteger(params[2])) {
                    minutes = Integer.parseInt(params[2]) * 60;
                    initial = params[2];
                } else {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.time"));
                    return true;
                }
            }

            habbo.mute(minutes, false);
            if (habbo.getHabboInfo().getCurrentRoom() != null)
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserIgnoredComposer(habbo, RoomUserIgnoredComposer.MUTED).compose());

            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_mute.muted").replace("%username%", params[1]).replace("%minutes%", initial), RoomChatMessageBubbles.ALERT);
        }

        return true;
    }
}
