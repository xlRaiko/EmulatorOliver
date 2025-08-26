package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

import static com.eu.habbo.core.CounterIpsScheduler.Counter;

public class AutoScheduler extends Scheduler {
    public static boolean IGNORE_HOTEL_VIEW;
    public static boolean IGNORE_IDLED;
    public static double HC_MODIFIER;
    public static double VIP_MODIFIER;

    public AutoScheduler() {
        super(Emulator.getConfig().getInt("hotel.auto.interval"));
        this.reloadConfig();
    }

    public void reloadConfig() {
        if (Emulator.getConfig().getBoolean("hotel.auto.enabled")) {
            IGNORE_HOTEL_VIEW = Emulator.getConfig().getBoolean("hotel.auto.ignore.hotelview");
            IGNORE_IDLED = Emulator.getConfig().getBoolean("hotel.auto.ignore.idled");
            HC_MODIFIER = Emulator.getConfig().getDouble("hotel.auto.hc_modifier", 1.0);
            VIP_MODIFIER = Emulator.getConfig().getDouble("hotel.auto.vip_modifier", 1.0);

            if (this.disposed) {
                this.disposed = false;
                this.run();
            }
        } else {
            this.disposed = true;
        }
    }

    @Override
    public void run() {
        super.run();

        for (Habbo habbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
            if (habbo.getHabboInfo().getCurrentRoom() == null && IGNORE_HOTEL_VIEW) {
                continue;
            }

            if (habbo.getRoomUnit().isIdle() && IGNORE_IDLED) {
                continue;
            }

            int realTimestamp = Emulator.getIntUnixTimestamp() - Integer.parseInt(habbo.getHabboStats().cache.get("online").toString());
            if (realTimestamp >= Emulator.getConfig().getInt("hotel.auto.required_time")) {
                habbo.getHabboStats().cache.put("online", Emulator.getIntUnixTimestamp());

                if (Counter(habbo.getHabboInfo().getIpLogin()) != 1) {
                    habbo.whisper(Emulator.getTexts().getValue("hotel.auto.ip_error"));
                    continue;
                }

                int diamonds = (int)(habbo.getHabboInfo().getRank().getDiamondsTimerAmount() * (habbo.getHabboStats().hasActiveVip() ? VIP_MODIFIER : (habbo.getHabboStats().hasActiveClub() ? HC_MODIFIER : 1.0)));
                int pixels = (int)(habbo.getHabboInfo().getRank().getPixelsTimerAmount() * (habbo.getHabboStats().hasActiveVip() ? VIP_MODIFIER : (habbo.getHabboStats().hasActiveClub() ? HC_MODIFIER : 1.0)));

                habbo.givePoints(diamonds);
                habbo.givePixels(pixels);

                habbo.whisper(Emulator.getTexts().getValue("hotel.auto.message").replace("%diamonds%", String.valueOf(diamonds)).replace("%pixels%", String.valueOf(pixels)), RoomChatMessageBubbles.SYSTEM);
            }
        }
    }
}
