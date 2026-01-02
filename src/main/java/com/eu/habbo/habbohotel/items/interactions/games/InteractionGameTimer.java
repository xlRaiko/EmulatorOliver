package com.eu.habbo.habbohotel.items.interactions.games;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.wired.WiredGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.threading.runnables.games.GameTimer;

public class InteractionGameTimer extends HabboItem implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(InteractionGameTimer.class);
   private int[] TIMER_INTERVAL_STEPS = new int[]{30, 60, 120, 180, 300, 600};
   private int baseTime = 0;
   private int timeNow = 0;
   private boolean isRunning = false;
   private boolean isPaused = false;
   private boolean threadActive = false;

   public InteractionGameTimer(ResultSet set, Item baseItem) throws SQLException {
      super(set, baseItem);
      this.parseCustomParams(baseItem);

      try {
         String[] data = set.getString("extra_data").split("\t");
         if (data.length >= 2) {
            this.baseTime = Integer.parseInt(data[1]);
            this.timeNow = this.baseTime;
         }

         if (data.length >= 1) {
            this.setExtradata(data[0] + "\t0");
         }
      } catch (Exception var4) {
         this.baseTime = this.TIMER_INTERVAL_STEPS[0];
         this.timeNow = this.baseTime;
      }

   }

   public InteractionGameTimer(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
      super(id, userId, item, extradata, limitedStack, limitedSells);
      this.parseCustomParams(item);
   }

   private void parseCustomParams(Item baseItem) {
      try {
         this.TIMER_INTERVAL_STEPS = Arrays.stream(baseItem.getCustomParams().split(",")).mapToInt((s) -> {
            try {
               return Integer.parseInt(s);
            } catch (NumberFormatException var2) {
               return 0;
            }
         }).toArray();
      } catch (Exception var3) {
         LOGGER.error("Caught exception", var3);
      }

   }

   public void endGame(Room room) {
      this.endGame(room, false);
   }

   public void endGame(Room room, boolean isStart) {
      this.isRunning = false;
      this.isPaused = false;
      Iterator var3 = room.getGames().iterator();

      while(true) {
         Game game;
         do {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               game = (Game)var3.next();
            } while(game.getState().equals(GameState.IDLE));
         } while(isStart && game instanceof WiredGame);

         game.onEnd();
         game.stop();
      }
   }

   private void createNewGame(Room room) {
      Iterator var2 = Emulator.getGameEnvironment().getRoomManager().getGameTypes().iterator();

      while(var2.hasNext()) {
         Class<? extends Game> gameClass = (Class)var2.next();
         Game existingGame = room.getGame(gameClass);
         if (existingGame != null) {
            existingGame.initialise();
         } else {
            try {
               Game game = (Game)gameClass.getDeclaredConstructor(Room.class).newInstance(room);
               room.addGame(game);
               game.initialise();
            } catch (Exception var6) {
               LOGGER.error("Caught exception", var6);
            }
         }
      }

   }

   private void pause(Room room) {
      Iterator var2 = room.getGames().iterator();

      while(var2.hasNext()) {
         Game game = (Game)var2.next();
         game.pause();
      }

   }

   private void unpause(Room room) {
      Iterator var2 = room.getGames().iterator();

      while(var2.hasNext()) {
         Game game = (Game)var2.next();
         game.unpause();
      }

   }

   public void run() {
      if (this.needsUpdate() || this.needsDelete()) {
         super.run();
      }

   }

   public void onPickUp(Room room) {
      this.endGame(room);
      this.setExtradata(this.baseTime + "\t" + this.baseTime);
      this.needsUpdate(true);
   }

   public void onPlace(Room room) {
      if (this.baseTime < this.TIMER_INTERVAL_STEPS[0]) {
         this.baseTime = this.TIMER_INTERVAL_STEPS[0];
      }

      this.timeNow = this.baseTime;
      this.setExtradata(this.timeNow + "\t" + this.baseTime);
      room.updateItem(this);
      this.needsUpdate(true);
      super.onPlace(room);
   }

   public void serializeExtradata(ServerMessage serverMessage) {
      serverMessage.appendInt(this.isLimited() ? 256 : 0);
      serverMessage.appendString("" + this.timeNow);
      super.serializeExtradata(serverMessage);
   }

   public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
      return false;
   }

   public boolean isWalkable() {
      return false;
   }

   public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
      if (this.getExtradata().isEmpty()) {
         this.setExtradata("0\t" + this.TIMER_INTERVAL_STEPS[0]);
      }

      if (objects.length >= 2 && objects[1] instanceof WiredEffectType) {
         if (this.isRunning && !this.isPaused) {
            return;
         }

         boolean wasPaused = this.isPaused;
         this.endGame(room, true);
         if (wasPaused) {
            WiredHandler.handle(WiredTriggerType.GAME_ENDS, (RoomUnit)null, room, new Object[0]);
         }

         this.createNewGame(room);
         this.timeNow = this.baseTime;
         this.isRunning = true;
         this.isPaused = false;
         room.updateItem(this);
         WiredHandler.handle(WiredTriggerType.GAME_STARTS, (RoomUnit)null, room, new Object[0]);
         if (!this.threadActive) {
            this.threadActive = true;
            Emulator.getThreading().run(new GameTimer(this), 1000L);
         }
      } else if (client != null) {
         if (!room.hasRights(client.getHabbo()) && !client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
            return;
         }

         InteractionGameTimerAction state = com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer.InteractionGameTimerAction.START_STOP;
         if (objects.length >= 1 && objects[0] instanceof Integer) {
            state = com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer.InteractionGameTimerAction.getByAction((Integer)objects[0]);
         }

         switch (state) {
            case START_STOP:
               if (this.isRunning) {
                  this.isPaused = !this.isPaused;
                  if (this.isPaused) {
                     this.pause(room);
                  } else {
                     this.unpause(room);
                     if (!this.threadActive) {
                        this.threadActive = true;
                        Emulator.getThreading().run(new GameTimer(this));
                     }
                  }
               } else {
                  this.isPaused = false;
                  this.isRunning = true;
                  this.timeNow = this.baseTime;
                  room.updateItem(this);
                  this.createNewGame(room);
                  WiredHandler.handle(WiredTriggerType.GAME_STARTS, (RoomUnit)null, room, new Object[]{this});
                  if (!this.threadActive) {
                     this.threadActive = true;
                     Emulator.getThreading().run(new GameTimer(this), 1000L);
                  }
               }
               break;
            case INCREASE_TIME:
               if (!this.isRunning) {
                  this.increaseTimer(room);
               } else if (this.isPaused) {
                  this.endGame(room);
                  this.increaseTimer(room);
                  WiredHandler.handle(WiredTriggerType.GAME_ENDS, (RoomUnit)null, room, new Object[0]);
               }
         }
      }

      super.onClick(client, room, objects);
   }

   public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
   }

   private void increaseTimer(Room room) {
      if (!this.isRunning) {
         int baseTime = -1;
         if (this.timeNow != this.baseTime) {
            baseTime = this.baseTime;
         } else {
            int[] var3 = this.TIMER_INTERVAL_STEPS;
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               int step = var3[var5];
               if (this.timeNow < step) {
                  baseTime = step;
                  break;
               }
            }

            if (baseTime == -1) {
               baseTime = this.TIMER_INTERVAL_STEPS[0];
            }
         }

         this.baseTime = baseTime;
         this.setExtradata(this.timeNow + "\t" + this.baseTime);
         this.timeNow = this.baseTime;
         room.updateItem(this);
         this.needsUpdate(true);
      }
   }

   public String getDatabaseExtraData() {
      return this.getExtradata();
   }

   public boolean allowWiredResetState() {
      return true;
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public void setRunning(boolean running) {
      this.isRunning = running;
   }

   public void setThreadActive(boolean threadActive) {
      this.threadActive = threadActive;
   }

   public boolean isPaused() {
      return this.isPaused;
   }

   public void reduceTime() {
      --this.timeNow;
   }

   public int getTimeNow() {
      return this.timeNow;
   }

   public void setTimeNow(int timeNow) {
      this.timeNow = timeNow;
   }

   public enum InteractionGameTimerAction {
   START_STOP(1),
   INCREASE_TIME(2);

   private int action;

   private InteractionGameTimerAction(int action) {
      this.action = action;
   }

   public int getAction() {
      return this.action;
   }

   public static InteractionGameTimerAction getByAction(int action) {
      if (action == 1) {
         return START_STOP;
      } else {
         return action == 2 ? INCREASE_TIME : START_STOP;
      }
   }
}
}