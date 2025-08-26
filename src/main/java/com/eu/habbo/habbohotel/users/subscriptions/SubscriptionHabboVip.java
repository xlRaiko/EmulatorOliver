package com.eu.habbo.habbohotel.users.subscriptions;

import com.eu.habbo.Emulator;
import com.eu.habbo.database.Database;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboStats;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.messages.outgoing.catalog.ClubCenterDataComposer;
import com.eu.habbo.messages.outgoing.generic.PickMonthlyClubGiftNotificationComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.UpdateUserLookComposer;
import com.eu.habbo.messages.outgoing.users.UserVipComposer;
import com.eu.habbo.messages.outgoing.users.UserPermissionsComposer;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class SubscriptionHabboVip extends Subscription {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionHabboVip.class);

    public static String ACHIEVEMENT_NAME = "";
    public static boolean DISCOUNT_ENABLED = false;
    public static int DISCOUNT_DAYS_BEFORE_END = 7;

    public static boolean isExecuting = false;

    public SubscriptionHabboVip(Integer id, Integer userId, String subscriptionType, Integer timestampStart, Integer duration, Boolean active) {
        super(id, userId, subscriptionType, timestampStart, duration, active);
    }

    @Override
    public void onCreated() {
        super.onCreated();

        HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(this.getUserId());
        HabboStats stats = habboInfo.getHabboStats();

        stats.maxFriends = Messenger.MAXIMUM_FRIENDS_HC;
        stats.maxRooms = RoomManager.MAXIMUM_ROOMS_HC;
        Emulator.getThreading().run(stats);

        progressAchievement(habboInfo);

        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.getUserId());
        if (habbo != null && habbo.getClient() != null) {

            if (habbo.getHabboStats().getRemainingClubGifts() > 0) {
                habbo.getClient().sendResponse(new PickMonthlyClubGiftNotificationComposer(habbo.getHabboStats().getRemainingClubGifts()));
            }

            if ((Emulator.getIntUnixTimestamp() - habbo.getHabboStats().hcMessageLastModified) < 60) {
                Emulator.getThreading().run(() -> {
                    habbo.getClient().sendResponse(new UserVipComposer(habbo));
                    habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
                }, (Emulator.getIntUnixTimestamp() - habbo.getHabboStats().hcMessageLastModified));
            } else {
                habbo.getClient().sendResponse(new UserVipComposer(habbo, SubscriptionHabboVip.HABBO_VIP, UserVipComposer.RESPONSE_TYPE_NORMAL));
                habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
            }
        }
    }


    @Override
    public void addDuration(int amount) {
        super.addDuration(amount);

        if (amount < 0) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.getUserId());
            if (habbo != null && habbo.getClient() != null) {
                habbo.getClient().sendResponse(new UserVipComposer(habbo, SubscriptionHabboVip.HABBO_VIP, UserVipComposer.RESPONSE_TYPE_NORMAL));
                habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
            }
        }
    }

    @Override
    public void onExtended(int duration) {
        super.onExtended(duration);

        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.getUserId());

        if (habbo != null && habbo.getClient() != null) {
            habbo.getClient().sendResponse(new UserVipComposer(habbo, SubscriptionHabboVip.HABBO_VIP, UserVipComposer.RESPONSE_TYPE_NORMAL));
            habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
        }
    }

    @Override
    public void onExpired() {
        super.onExpired();

        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.getUserId());
        HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(this.getUserId());
        HabboStats stats = habboInfo.getHabboStats();

        stats.maxFriends = Messenger.MAXIMUM_FRIENDS;
        stats.maxRooms = RoomManager.MAXIMUM_ROOMS_USER;
        Emulator.getThreading().run(stats);

        if (habbo != null && ClothingValidationManager.VALIDATE_ON_HC_EXPIRE) {
            habboInfo.setLook(ClothingValidationManager.validateLook(habbo, habboInfo.getLook(), habboInfo.getGender().name()));
            Emulator.getThreading().run(habbo.getHabboInfo());

            if (habbo.getClient() != null) {
                habbo.getClient().sendResponse(new UpdateUserLookComposer(habbo));
            }

            if (habbo.getHabboInfo().getCurrentRoom() != null) {
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
            }
        }

        if (habbo != null && habbo.getClient() != null) {
            habbo.getClient().sendResponse(new UserVipComposer(habbo, SubscriptionHabboVip.HABBO_VIP, UserVipComposer.RESPONSE_TYPE_NORMAL));
            habbo.getClient().sendResponse(new UserPermissionsComposer(habbo));
        }
    }

    public static void processClubBadge(Habbo habbo) {
        progressAchievement(habbo.getHabboInfo());
    }

    private static void progressAchievement(HabboInfo habboInfo) {
        HabboStats stats = habboInfo.getHabboStats();
        Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement(ACHIEVEMENT_NAME);
        if(achievement != null) {
            int currentProgress = stats.getAchievementProgress(achievement);
            if(currentProgress == -1) {
                currentProgress = 0;
            }

            int progressToSet = (int)Math.ceil(stats.getPastTimeAsVip() / 2678400.0);
            int toIncrease = Math.max(progressToSet - currentProgress, 0);

            if(toIncrease > 0) {
                AchievementManager.progressAchievement(habboInfo.getId(), achievement, toIncrease);
            }
        }
    }

}
