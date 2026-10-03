package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.MacroSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import java.util.List;

public final class QuickMacro extends Module {
   private final MacroSetting quickCommands = new MacroSetting(EncryptedString.of("Quick Commands"))
      .setDescription(EncryptedString.of("Commands to execute when keybind is pressed"));
   private final BooleanSetting showNotifications = new BooleanSetting(EncryptedString.of("Show Notifications"), true)
      .setDescription(EncryptedString.of("Show chat notifications when commands are executed"));
   private final BooleanSetting executeAll = new BooleanSetting(EncryptedString.of("Execute All"), false)
      .setDescription(EncryptedString.of("Execute all commands or just the first one"));

   public QuickMacro() {
      super(EncryptedString.of("Quick Macro"), EncryptedString.of("\ud83d\udc31 Execute macro commands quickly with a keybind"), -1, Category.MISC);
      this.addsettings(new Setting[]{this.quickCommands, this.showNotifications, this.executeAll});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.executeQuickMacro();
      this.toggle();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onTick(TickEvent event) {
   }

   private void executeQuickMacro() {
      if (this.mc.player != null && this.mc.getNetworkHandler() != null) {
         List<String> commandList = this.quickCommands.getCommands();
         if (commandList.isEmpty()) {
            if (this.showNotifications.getValue()) {
               this.mc.player.sendMessage(EncryptedString.of("§c\ud83d\udc31 Quick Macro: No commands configured!").toText());
            }
         } else {
            if (this.executeAll.getValue()) {
               for (int i = 0; i < commandList.size(); i++) {
                  String command = commandList.get(i);
                  this.executeCommand(command);
                  if (this.showNotifications.getValue()) {
                     this.mc
                        .player
                        .sendMessage(EncryptedString.of("§e\ud83d\udc31 Quick Macro: Executed command " + (i + 1) + "/" + commandList.size()).toText());
                  }
               }

               if (this.showNotifications.getValue()) {
                  this.mc.player.sendMessage(EncryptedString.of("§a\ud83d\udc31 Quick Macro: All commands executed!").toText());
               }
            } else {
               String command = commandList.get(0);
               this.executeCommand(command);
               if (this.showNotifications.getValue()) {
                  this.mc.player.sendMessage(EncryptedString.of("§a\ud83d\udc31 Quick Macro: Executed command!").toText());
               }
            }
         }
      }
   }

   private void executeCommand(String command) {
      if (command.startsWith("/")) {
         command = command.substring(1);
         this.mc.getNetworkHandler().sendChatCommand(command);
      } else {
         this.mc.getNetworkHandler().sendChatMessage(command);
      }
   }
}
