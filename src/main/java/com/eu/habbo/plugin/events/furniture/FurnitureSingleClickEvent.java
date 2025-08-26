package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

public class FurnitureSingleClickEvent extends FurnitureUserEvent {
    public int state;

    public FurnitureSingleClickEvent(HabboItem furniture, Habbo habbo, int state) {
        super(furniture, habbo);
        this.state = state;
    }
}
