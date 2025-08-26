package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

public class GuideToolsComposer extends MessageComposer {
    private final boolean onDuty;
    private final boolean onTourRequest;
    private final boolean onHelperRequest;
    private final boolean onBullyReport;

    public GuideToolsComposer(boolean onDuty, boolean onTourRequest, boolean onHelperRequest, boolean onBullyReport) {
        this.onDuty = onDuty;
        this.onTourRequest = onTourRequest;
        this.onHelperRequest = onHelperRequest;
        this.onBullyReport = onBullyReport;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideToolsComposer);
        this.response.appendBoolean(this.onDuty);
        this.response.appendBoolean(this.onTourRequest);
        this.response.appendBoolean(this.onHelperRequest);
        this.response.appendBoolean(this.onBullyReport);
        this.response.appendInt(0);
        this.response.appendInt(Emulator.getGameEnvironment().getGuideManager().getGuidesCount());
        this.response.appendInt(Emulator.getGameEnvironment().getGuideManager().getGuardiansCount());

        return this.response;
    }

    public boolean isOnDuty() {
        return onDuty;
    }
}
