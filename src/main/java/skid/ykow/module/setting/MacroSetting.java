package skid.ykow.module.setting;

import java.util.ArrayList;
import java.util.List;

public class MacroSetting extends Setting {
   private List<String> commands;
   private final List<String> defaultCommands;

   public MacroSetting(CharSequence name, List<String> defaultCommands) {
      super(name);
      this.commands = new ArrayList<>(defaultCommands);
      this.defaultCommands = new ArrayList<>(defaultCommands);
   }

   public MacroSetting(CharSequence name) {
      this(name, new ArrayList<>());
   }

   public List<String> getCommands() {
      return this.commands;
   }

   public void setCommands(List<String> commands) {
      this.commands = new ArrayList<>(commands);
   }

   public void addCommand(String command) {
      this.commands.add(command);
   }

   public void removeCommand(int index) {
      if (index >= 0 && index < this.commands.size()) {
         this.commands.remove(index);
      }
   }

   public void clearCommands() {
      this.commands.clear();
   }

   public List<String> getDefaultCommands() {
      return new ArrayList<>(this.defaultCommands);
   }

   public void resetValue() {
      this.commands = new ArrayList<>(this.defaultCommands);
   }

   public MacroSetting setDescription(CharSequence description) {
      super.setDescription(description);
      return this;
   }
}
