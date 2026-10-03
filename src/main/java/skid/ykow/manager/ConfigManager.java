package skid.ykow.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import skid.ykow.Ykow;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.ItemSetting;
import skid.ykow.module.setting.MacroSetting;
import skid.ykow.module.setting.MinMaxSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class ConfigManager {
   private JsonObject jsonObject;
   private final File configFile;
   private final Gson gson;

   public ConfigManager() {
      String userHome = System.getProperty("user.home");
      String configDir = userHome + File.separator + ".minecraft" + File.separator + "config";
      File configFolder = new File(configDir);
      if (!configFolder.exists()) {
         configFolder.mkdirs();
      }

      this.configFile = new File(configFolder, "ykow_config.json");
      this.gson = new GsonBuilder().setPrettyPrinting().create();
      this.jsonObject = new JsonObject();
      this.loadConfigFromFile();
   }

   private void loadConfigFromFile() {
      try {
         if (this.configFile.exists()) {
            try (FileReader reader = new FileReader(this.configFile)) {
               this.jsonObject = (JsonObject)this.gson.fromJson(reader, JsonObject.class);
               if (this.jsonObject == null) {
                  this.jsonObject = new JsonObject();
               }
            }
         } else {
            this.jsonObject = new JsonObject();
         }
      } catch (Exception var6) {
         System.err.println("[ConfigManager] Error loading config file: " + var6.getMessage());
         var6.printStackTrace();
         this.jsonObject = new JsonObject();
      }
   }

   public void loadProfile() {
      try {
         if (this.jsonObject == null) {
            this.jsonObject = new JsonObject();
            return;
         }

         int modulesLoaded = 0;

         for (Module next : Ykow.INSTANCE.getModuleManager().c()) {
            try {
               String moduleName = this.getModuleName(next);
               JsonElement value = this.jsonObject.get(moduleName);
               if (value != null && value.isJsonObject()) {
                  JsonObject asJsonObject = value.getAsJsonObject();
                  JsonElement value2 = asJsonObject.get("enabled");
                  if (value2 != null && value2.isJsonPrimitive() && value2.getAsBoolean()) {
                     next.toggle(true);
                  }

                  for (Object next2 : next.getSettings()) {
                     try {
                        String settingName = this.getSettingName((Setting)next2);
                        JsonElement value3 = asJsonObject.get(settingName);
                        if (value3 != null) {
                           this.setValueFromJson((Setting)next2, value3, next);
                        }
                     } catch (Exception var12) {
                        System.err.println("[ConfigManager] Error loading setting for module " + moduleName + ": " + var12.getMessage());
                     }
                  }

                  modulesLoaded++;
               }
            } catch (Exception var13) {
               System.err.println("[ConfigManager] Error loading module: " + var13.getMessage());
            }
         }
      } catch (Exception var14) {
         System.err.println("[ConfigManager] Error loading profile: " + var14.getMessage());
         var14.printStackTrace();
      }
   }

   private String getModuleName(Module module) {
      try {
         CharSequence name = module.getName();
         return name instanceof EncryptedString ? ((EncryptedString)name).toString() : name.toString();
      } catch (Exception var3) {
         return "Module_" + module.hashCode();
      }
   }

   private String getSettingName(Setting setting) {
      try {
         CharSequence name = setting.getName();
         return name instanceof EncryptedString ? ((EncryptedString)name).toString() : name.toString();
      } catch (Exception var3) {
         return "Setting_" + setting.hashCode();
      }
   }

   private void setValueFromJson(Setting setting, JsonElement jsonElement, Module module) {
      try {
         if (setting instanceof BooleanSetting booleanSetting) {
            if (jsonElement.isJsonPrimitive()) {
               booleanSetting.setValue(jsonElement.getAsBoolean());
            }
         } else if (setting instanceof ModeSetting enumSetting) {
            if (jsonElement.isJsonPrimitive()) {
               int asInt = jsonElement.getAsInt();
               if (asInt != -1) {
                  enumSetting.setModeIndex(asInt);
               } else {
                  enumSetting.setModeIndex(enumSetting.getOriginalValue());
               }
            }
         } else if (setting instanceof NumberSetting numberSetting) {
            if (jsonElement.isJsonPrimitive()) {
               numberSetting.getValue(jsonElement.getAsDouble());
            }
         } else if (setting instanceof BindSetting bindSetting) {
            if (jsonElement.isJsonPrimitive()) {
               int asInt2 = jsonElement.getAsInt();
               bindSetting.setValue(asInt2);
               if (bindSetting.isModuleKey()) {
                  module.setKeybind(asInt2);
               }
            }
         } else if (setting instanceof StringSetting stringSetting) {
            if (jsonElement.isJsonPrimitive()) {
               stringSetting.setValue(jsonElement.getAsString());
            }
         } else if (setting instanceof MinMaxSetting minMaxSetting) {
            if (jsonElement.isJsonObject()) {
               JsonObject asJsonObject = jsonElement.getAsJsonObject();
               if (asJsonObject.has("min") && asJsonObject.has("max")) {
                  double asDouble = asJsonObject.get("min").getAsDouble();
                  double asDouble2 = asJsonObject.get("max").getAsDouble();
                  minMaxSetting.setCurrentMin(asDouble);
                  minMaxSetting.setCurrentMax(asDouble2);
               }
            }
         } else if (setting instanceof ItemSetting && jsonElement.isJsonPrimitive()) {
            ((ItemSetting)setting).setItem((Item)Registries.ITEM.get(Identifier.of(jsonElement.getAsString())));
         } else if (setting instanceof MacroSetting && jsonElement.isJsonArray()) {
            MacroSetting macroSetting = (MacroSetting)setting;
            macroSetting.clearCommands();

            for (JsonElement element : jsonElement.getAsJsonArray()) {
               if (element.isJsonPrimitive()) {
                  macroSetting.addCommand(element.getAsString());
               }
            }
         }
      } catch (Exception var15) {
         System.err.println("[ConfigManager] Error setting value from JSON: " + var15.getMessage());
      }
   }

   private void saveConfigToFile() {
      try {
         File parentDir = this.configFile.getParentFile();
         if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
         }

         try (FileWriter writer = new FileWriter(this.configFile)) {
            this.gson.toJson(this.jsonObject, writer);
         }
      } catch (IOException var7) {
         System.err.println("[ConfigManager] Error saving config file: " + var7.getMessage());
         var7.printStackTrace();
      }
   }

   public void saveConfig() {
      this.shutdown();
   }

   public void manualSave() {
      this.shutdown();
   }

   public void reloadConfig() {
      this.loadConfigFromFile();
      this.loadProfile();
   }

   public File getConfigFile() {
      return this.configFile;
   }

   public void shutdown() {
      try {
         this.jsonObject = new JsonObject();
         int modulesSaved = 0;

         for (Module module : Ykow.INSTANCE.getModuleManager().c()) {
            try {
               String moduleName = this.getModuleName(module);
               JsonObject jsonObject = new JsonObject();
               jsonObject.addProperty("enabled", module.isEnabled());

               for (Setting setting : module.getSettings()) {
                  try {
                     this.save(setting, jsonObject, module);
                  } catch (Exception var9) {
                     System.err.println("[ConfigManager] Error saving setting for module " + moduleName + ": " + var9.getMessage());
                  }
               }

               this.jsonObject.add(moduleName, jsonObject);
               modulesSaved++;
            } catch (Exception var10) {
               System.err.println("[ConfigManager] Error saving module: " + var10.getMessage());
            }
         }

         this.saveConfigToFile();
      } catch (Exception var11) {
         System.err.println("[ConfigManager] Error during shutdown: " + var11.getMessage());
         var11.printStackTrace(System.err);
      }
   }

   private void save(Setting setting, JsonObject jsonObject, Module module) {
      try {
         String settingName = this.getSettingName(setting);
         if (setting instanceof BooleanSetting booleanSetting) {
            jsonObject.addProperty(settingName, booleanSetting.getValue());
         } else if (setting instanceof ModeSetting<?> enumSetting) {
            jsonObject.addProperty(settingName, enumSetting.getModeIndex());
         } else if (setting instanceof NumberSetting numberSetting) {
            jsonObject.addProperty(settingName, numberSetting.getValue());
         } else if (setting instanceof BindSetting bindSetting) {
            jsonObject.addProperty(settingName, bindSetting.getValue());
         } else if (setting instanceof StringSetting stringSetting) {
            jsonObject.addProperty(settingName, stringSetting.getValue());
         } else if (setting instanceof MinMaxSetting) {
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("min", ((MinMaxSetting)setting).getCurrentMin());
            jsonObject2.addProperty("max", ((MinMaxSetting)setting).getCurrentMax());
            jsonObject.add(settingName, jsonObject2);
         } else if (setting instanceof ItemSetting itemSetting) {
            jsonObject.addProperty(settingName, Registries.ITEM.getId(itemSetting.getItem()).toString());
         } else if (setting instanceof MacroSetting macroSetting) {
            JsonArray commandsArray = new JsonArray();

            for (String command : macroSetting.getCommands()) {
               commandsArray.add(command);
            }

            jsonObject.add(settingName, commandsArray);
         }
      } catch (Exception var16) {
         String settingNamex = "Unknown";

         try {
            settingNamex = this.getSettingName(setting);
         } catch (Exception var15) {
            settingNamex = "Setting_" + setting.hashCode();
         }

         System.err.println("[ConfigManager] Error saving setting " + settingNamex + ": " + var16.getMessage());
      }
   }
}
