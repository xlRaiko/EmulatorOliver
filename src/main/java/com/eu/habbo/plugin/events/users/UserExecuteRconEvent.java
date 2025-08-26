package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.plugin.Event;

public class UserExecuteRconEvent extends Event {
    public final HabboInfo habbo;
    public final String type;
    public final String[] params;

    public UserExecuteRconEvent(HabboInfo habbo, String type, String[] params) {
        this.habbo = habbo;
        this.type = type;
        this.params = params;
    }
}
