package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

public class RestartCommand extends Command {
    public RestartCommand() {
        super("cmd_restart", Emulator.getTexts().getValue("commands.keys.cmd_restart").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "/var/www/Emulator/restart_script.sh");
            processBuilder.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
