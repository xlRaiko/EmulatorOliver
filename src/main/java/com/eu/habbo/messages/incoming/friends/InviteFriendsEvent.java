package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Message;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.RoomInviteComposer;

import java.util.Objects;

public class InviteFriendsEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboStats().allowTalk()) {
            int[] userIds = new int[this.packet.readInt()];

            for (int i = 0; i < userIds.length; i++) {
                userIds[i] = this.packet.readInt();
            }

            String message = this.packet.readString();

            if (this.client.getHabbo().hasPermission(Permission.ACC_NOMUTE)) {
                for (int i : userIds) {
                    if (i == 0)
                        continue;

                    Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);

                    if (habbo != null) {
                        if (!habbo.getHabboStats().blockRoomInvites) {
                            habbo.getClient().sendResponse(new RoomInviteComposer(this.client.getHabbo().getHabboInfo().getId(), message));
                            Message chatMessage = new Message(habbo.getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getId(), message);
                            Emulator.getThreading().run(chatMessage);
                        }
                    }
                }
            } else {
                String filtered = Emulator.getGameEnvironment().getWordFilter().filter(message, this.client.getHabbo());

                if (Objects.equals(filtered, "I am noob!")) {
                    Emulator.getGameEnvironment().getWordFilter().autoReportConsole(this.client.getHabbo(), message);
                } else {
                    for (int i : userIds) {
                        if (i == 0)
                            continue;

                        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(i);

                        if (habbo != null) {
                            if (!habbo.getHabboStats().blockRoomInvites) {
                                habbo.getClient().sendResponse(new RoomInviteComposer(this.client.getHabbo().getHabboInfo().getId(), filtered));
                                Message chatMessage = new Message(habbo.getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getId(), filtered);
                                Emulator.getThreading().run(chatMessage);
                            }
                        }
                    }
                }
            }
        }
    }
}
