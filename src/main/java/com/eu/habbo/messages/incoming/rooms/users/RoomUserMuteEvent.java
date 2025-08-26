package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserIgnoredComposer;

public class RoomUserMuteEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        int userId = this.packet.readInt();
        int roomId = this.packet.readInt();
        int minutes = this.packet.readInt();

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(roomId);

        if (room != null) {
            if (room.hasRights(this.client.getHabbo()) || this.client.getHabbo().hasPermission("cmd_mute") || this.client.getHabbo().hasPermission(Permission.ACC_AMBASSADOR)) {
                Habbo habbo = room.getHabbo(userId);

                if (habbo != null) {
                    if (habbo.getHabboInfo().getRank().getId() >= this.client.getHabbo().getHabboInfo().getRank().getId()) {
                        this.client.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.rank"));
                    } else {
                        habbo.mute(minutes * 60, false);
                        if (habbo.getHabboInfo().getCurrentRoom() != null)
                            habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserIgnoredComposer(habbo, RoomUserIgnoredComposer.MUTED).compose());

                        this.client.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_mute.muted").replace("%username%", habbo.getHabboInfo().getUsername()).replace("%minutes%", String.valueOf(minutes)), RoomChatMessageBubbles.ALERT);

                        AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModMuteSeen"));
                    }
                }
            }
        }
    }
}
