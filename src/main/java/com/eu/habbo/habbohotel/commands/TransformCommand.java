package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.RoomUserPetComposer;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.pets.PetData;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRemoveComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUsersComposer;
import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;

public class TransformCommand extends Command {
    public static String CACHE_TRANSFORM = "habbo.transform";

    public TransformCommand() {
        super("cmd_transform", new String[]{"transform"});
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length == 1) {
            StringBuilder petNames = new StringBuilder();
            ArrayList<PetData> petData = new ArrayList<>(Emulator.getGameEnvironment().getPetManager().getPetData());

            for (PetData pet : petData) {
                petNames.append(pet.getType());
                petNames.append(" | ").append(pet.getName());
                petNames.append("\r");
            }

            THashMap<String, String> codes = new THashMap<>();
            codes.put("MESSAGE", petNames.toString());
            codes.put("EXTRA", "TransformCommand");

            ServerMessage msg = new BubbleAlertComposer("hotel.custom", codes).compose();
            gameClient.sendResponse(msg);

            return true;
        } else {
            String petName = params[1].toUpperCase();
            if(petName.contains("HUMAN")) {
                gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserRemoveComposer(gameClient.getHabbo().getRoomUnit()).compose());
                gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUsersComposer(gameClient.getHabbo()).compose());
                gameClient.getHabbo().getHabboStats().cache.remove(CACHE_TRANSFORM);

                return true;
            } else {
                PetData petData = Emulator.getGameEnvironment().getPetManager().getPetData(petName);

                int race = 0;

                if (params.length >= 3) {
                    try {
                        race = Integer.parseInt(params[2]);
                    } catch (Exception e) {
                        return true;
                    }
                }

                String color = "FFFFFF";
                if (params.length >= 4) {
                    color = params[3];
                }

                if (petData != null) {
                    RoomUnit roomUnit = gameClient.getHabbo().getRoomUnit();
                    roomUnit.setRoomUnitType(RoomUnitType.PET);
                    gameClient.getHabbo().getHabboStats().cache.put("pet_type", petData);
                    gameClient.getHabbo().getHabboStats().cache.put("pet_race", race);
                    gameClient.getHabbo().getHabboStats().cache.put("pet_color", color);
                    gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserRemoveComposer(gameClient.getHabbo().getRoomUnit()).compose());
                    gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserPetComposer(petData.getType(), race, color, gameClient.getHabbo()).compose());

                    String stringParams = params[0] + " " + petName + " " + race + " " + color;
                    gameClient.getHabbo().getHabboStats().cache.put(CACHE_TRANSFORM, stringParams);

                    return true;
                }
            }
        }

        return true;
    }
}