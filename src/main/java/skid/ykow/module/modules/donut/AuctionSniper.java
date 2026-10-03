package skid.ykow.module.modules.donut;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.ItemSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public final class AuctionSniper extends Module {
   private final ItemSetting snipingItem = new ItemSetting(EncryptedString.of("Sniping Item"), Items.AIR);
   private final StringSetting price = new StringSetting(EncryptedString.of("Price"), "1k");
   private final ModeSetting<AuctionSniper.Mode> mode = new ModeSetting<>(EncryptedString.of("Mode"), AuctionSniper.Mode.MANUAL, AuctionSniper.Mode.class)
      .setDescription(EncryptedString.of("Manual is faster but api doesnt require auction gui opened all the time"));
   private final StringSetting apiKey = new StringSetting(EncryptedString.of("Api Key"), "")
      .setDescription(EncryptedString.of("You can get it by typing /api in chat"));
   private final NumberSetting refreshDelay = new NumberSetting(EncryptedString.of("Refresh Delay"), 0.0, 100.0, 2.0, 1.0);
   private final NumberSetting buyDelay = new NumberSetting(EncryptedString.of("Buy Delay"), 0.0, 100.0, 2.0, 1.0);
   private final NumberSetting apiRefreshRate = new NumberSetting(EncryptedString.of("API Refresh Rate"), 10.0, 5000.0, 250.0, 10.0)
      .getValue(EncryptedString.of("How often to query the API (in milliseconds)"));
   private final BooleanSetting showApiNotifications = new BooleanSetting(EncryptedString.of("Show API Notifications"), true)
      .setDescription(EncryptedString.of("Show chat notifications for API actions"));
   private int delayCounter;
   private boolean isProcessing;
   private final HttpClient httpClient;
   private final Gson gson;
   private long lastApiCallTimestamp = 0L;
   private final Map<String, Double> snipingItems = new HashMap<>();
   private boolean isApiQueryInProgress = false;
   private boolean isAuctionSniping = false;
   private int auctionPageCounter = -1;
   private String currentSellerName = "";

   public AuctionSniper() {
      super(EncryptedString.of("Auction Sniper"), EncryptedString.of("Snipes items on auction house for cheap"), -1, Category.DONUT);
      this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
      this.gson = new Gson();
      Setting[] settingArray = new Setting[]{
         this.snipingItem, this.price, this.mode, this.apiKey, this.refreshDelay, this.buyDelay, this.apiRefreshRate, this.showApiNotifications
      };
      this.addsettings(settingArray);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      double d = this.parsePrice(this.price.getValue());
      if (d == -1.0) {
         if (this.mc.player != null) {
            ClientPlayerEntity clientPlayerEntity = this.mc.player;
            clientPlayerEntity.sendMessage(Text.of("Invalid Price"), true);
         }

         this.toggle();
      } else {
         if (this.snipingItem.getItem() != Items.AIR) {
            Map<String, Double> map = this.snipingItems;
            map.put(this.snipingItem.getItem().toString(), d);
         }

         this.lastApiCallTimestamp = 0L;
         this.isApiQueryInProgress = false;
         this.isAuctionSniping = false;
         this.currentSellerName = "";
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.isAuctionSniping = false;
   }

   @EventListener
   public void onTick(TickEvent tickEvent) {
      if (this.mc.player != null) {
         if (this.delayCounter > 0) {
            this.delayCounter--;
         } else if (this.mode.isMode(AuctionSniper.Mode.API)) {
            this.handleApiMode();
         } else {
            if (this.mode.isMode(AuctionSniper.Mode.MANUAL)) {
               ScreenHandler screenHandler = this.mc.player.currentScreenHandler;
               if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                  String[] stringArray = this.snipingItem.getItem().getTranslationKey().split("\\.");
                  String string2 = stringArray[stringArray.length - 1];
                  String string3 = Arrays.stream(string2.replace("_", " ").split(" "))
                     .map(string -> string.substring(0, 1).toUpperCase() + string.substring(1))
                     .collect(Collectors.joining(" "));
                  this.mc.getNetworkHandler().sendChatCommand("ah " + string3);
                  this.delayCounter = 20;
                  return;
               }

               if (((GenericContainerScreenHandler)screenHandler).getRows() == 6) {
                  this.processSixRowAuction((GenericContainerScreenHandler)screenHandler);
               } else if (((GenericContainerScreenHandler)screenHandler).getRows() == 3) {
                  this.processThreeRowAuction((GenericContainerScreenHandler)screenHandler);
               }
            }
         }
      }
   }

   private void handleApiMode() {
      if (!this.isAuctionSniping) {
         if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler && this.mc.currentScreen.getTitle().getString().contains("Page")) {
            this.mc.player.closeHandledScreen();
            this.delayCounter = 20;
         } else if (!this.isApiQueryInProgress) {
            long l = System.currentTimeMillis();
            long l2 = l - this.lastApiCallTimestamp;
            if (l2 > this.apiRefreshRate.getIntValue()) {
               this.lastApiCallTimestamp = l;
               if (this.apiKey.getValue().isEmpty()) {
                  if (this.showApiNotifications.getValue()) {
                     ClientPlayerEntity clientPlayerEntity = this.mc.player;
                     clientPlayerEntity.sendMessage(Text.of("§cAPI key is not set. Set it using /api in-game."), false);
                  }

                  return;
               }

               this.isApiQueryInProgress = true;
               this.queryApi().thenAccept(this::processApiResponse);
            }
         }
      } else {
         ScreenHandler screenHandler = this.mc.player.currentScreenHandler;
         if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
            if (this.auctionPageCounter != -1) {
               if (this.auctionPageCounter <= 40) {
                  this.auctionPageCounter++;
               } else {
                  this.isAuctionSniping = false;
                  this.currentSellerName = "";
               }
            } else {
               this.mc.getNetworkHandler().sendChatCommand("ah " + this.currentSellerName);
               this.auctionPageCounter = 0;
            }
         } else {
            this.auctionPageCounter = -1;
            if (((GenericContainerScreenHandler)screenHandler).getRows() == 6) {
               this.processSixRowAuction((GenericContainerScreenHandler)screenHandler);
            } else if (((GenericContainerScreenHandler)screenHandler).getRows() == 3) {
               this.processThreeRowAuction((GenericContainerScreenHandler)screenHandler);
            }
         }
      }
   }

   private CompletableFuture<List<?>> queryApi() {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               String string = "https://api.donutsmp.net/v1/auction/list/1";
               HttpResponse<String> httpResponse = this.httpClient
                  .send(
                     HttpRequest.newBuilder()
                        .uri(URI.create(string))
                        .header("Authorization", "Bearer " + this.apiKey.getValue())
                        .header("Content-Type", "application/json")
                        .POST(BodyPublishers.ofString("{\"sort\": \"recently_listed\"}"))
                        .build(),
                     BodyHandlers.ofString()
                  );
               if (httpResponse.statusCode() != 200) {
                  if (this.showApiNotifications.getValue() && this.mc.player != null) {
                     ClientPlayerEntity clientPlayerEntity = this.mc.player;
                     clientPlayerEntity.sendMessage(Text.of("§cAPI Error: " + httpResponse.statusCode()), false);
                  }

                  ArrayList<?> arrayList = new ArrayList();
                  this.isApiQueryInProgress = false;
                  return arrayList;
               } else {
                  Gson gson = this.gson;
                  JsonArray jsonArray = ((JsonObject)gson.fromJson(httpResponse.body(), JsonObject.class)).getAsJsonArray("result");
                  ArrayList<JsonObject> arrayList = new ArrayList<>();

                  for (JsonElement jsonElement : jsonArray) {
                     arrayList.add(jsonElement.getAsJsonObject());
                  }

                  this.isApiQueryInProgress = false;
                  return arrayList;
               }
            } catch (Throwable var8) {
               var8.printStackTrace(System.err);
               return List.of();
            }
         }
      );
   }

   private void processApiResponse(List list) {
      for (Object auctionData : list) {
         try {
            String itemId = ((JsonObject)auctionData).getAsJsonObject("item").get("id").getAsString();
            long price = ((JsonObject)auctionData).get("price").getAsLong();
            String sellerName = ((JsonObject)auctionData).getAsJsonObject("seller").get("name").getAsString();

            for (Entry<String, Double> entry : this.snipingItems.entrySet()) {
               String itemName = entry.getKey();
               double maxPrice = entry.getValue();
               if (itemId.contains(itemName) && price <= maxPrice) {
                  if (this.showApiNotifications.getValue() && this.mc.player != null) {
                     ClientPlayerEntity clientPlayerEntity = this.mc.player;
                     clientPlayerEntity.sendMessage(
                        Text.of(
                           "§aFound "
                              + itemId
                              + " for "
                              + this.formatPrice(price)
                              + " §r(threshold: "
                              + this.formatPrice(maxPrice)
                              + ") §afrom seller: "
                              + sellerName
                        ),
                        false
                     );
                  }

                  this.isAuctionSniping = true;
                  this.currentSellerName = sellerName;
                  return;
               }
            }
         } catch (Exception var13) {
            if (this.showApiNotifications.getValue() && this.mc.player != null) {
               ClientPlayerEntity clientPlayerEntity = this.mc.player;
               clientPlayerEntity.sendMessage(Text.of("§cError processing auction: " + var13.getMessage()), false);
            }
         }
      }
   }

   private void processSixRowAuction(GenericContainerScreenHandler genericContainerScreenHandler) {
      ItemStack itemStack = genericContainerScreenHandler.getSlot(47).getStack();
      if (itemStack.isOf(Items.AIR)) {
         this.delayCounter = 2;
      } else {
         for (Object e : itemStack.getTooltip(TooltipContext.DEFAULT, this.mc.player, TooltipType.BASIC)) {
            String string = e.toString();
            if (string.contains("Recently Listed") && (((Text)e).getStyle().toString().contains("white") || string.contains("white"))) {
               this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 47, 1, SlotActionType.QUICK_MOVE, this.mc.player);
               this.delayCounter = 5;
               return;
            }
         }

         for (int i = 0; i < 44; i++) {
            ItemStack itemStack2 = genericContainerScreenHandler.getSlot(i).getStack();
            if (itemStack2.isOf(this.snipingItem.getItem()) && this.isValidAuctionItem(itemStack2)) {
               if (this.isProcessing) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                  this.isProcessing = false;
                  return;
               }

               this.isProcessing = true;
               this.delayCounter = this.buyDelay.getIntValue();
               return;
            }
         }

         if (this.isAuctionSniping) {
            this.isAuctionSniping = false;
            this.currentSellerName = "";
            this.mc.player.closeHandledScreen();
         } else {
            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 49, 1, SlotActionType.QUICK_MOVE, this.mc.player);
            this.delayCounter = this.refreshDelay.getIntValue();
         }
      }
   }

   private void processThreeRowAuction(GenericContainerScreenHandler genericContainerScreenHandler) {
      if (this.isValidAuctionItem(genericContainerScreenHandler.getSlot(13).getStack())) {
         this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 15, 1, SlotActionType.QUICK_MOVE, this.mc.player);
         this.delayCounter = 20;
      }

      if (this.isAuctionSniping) {
         this.isAuctionSniping = false;
         this.currentSellerName = "";
      }
   }

   private double parseTooltipPrice(List list) {
      if (list != null && !list.isEmpty()) {
         Iterator iterator = list.iterator();

         while (iterator.hasNext()) {
            String string3 = ((Text)iterator.next()).getString();
            if (string3.matches("(?i).*price\\s*:\\s*\\$.*")) {
               String string4 = string3.replaceAll("[,$]", "");
               Matcher matcher = Pattern.compile("([\\d]+(?:\\.[\\d]+)?)\\s*([KMB])?", 2).matcher(string4);
               if (matcher.find()) {
                  String string2 = matcher.group(1);
                  String string = matcher.group(2) != null ? matcher.group(2).toUpperCase() : "";
                  return this.parsePrice(string2 + string);
               }
            }
         }

         return -1.0;
      } else {
         return -1.0;
      }
   }

   private boolean isValidAuctionItem(ItemStack itemStack) {
      List list = itemStack.getTooltip(TooltipContext.DEFAULT, this.mc.player, TooltipType.BASIC);
      double d = this.parseTooltipPrice(list) / itemStack.getCount();
      double d2 = this.parsePrice(this.price.getValue());
      if (d2 == -1.0) {
         if (this.mc.player != null) {
            ClientPlayerEntity clientPlayerEntity = this.mc.player;
            clientPlayerEntity.sendMessage(Text.of("Invalid Price"), true);
         }

         this.toggle();
         return false;
      } else if (d != -1.0) {
         return d <= d2;
      } else {
         if (this.mc.player != null
            && (
               this.mc.player.currentScreenHandler == null
                  || this.mc
                     .player
                     .currentScreenHandler
                     .slots
                     .stream()
                     .noneMatch(slot -> slot.hasStack() && slot.getStack().getItem() == Items.LIME_STAINED_GLASS_PANE)
            )) {
            ClientPlayerEntity clientPlayerEntity = this.mc.player;

            for (int i = 0; i < list.size() - 1; i++) {
               PrintStream printStream = System.out;
               printStream.println(i + ". " + ((Text)list.get(i)).getString());
            }
         } else if (this.mc.player != null && this.mc.player.currentScreenHandler != null) {
            this.mc
               .player
               .currentScreenHandler
               .slots
               .stream()
               .filter(slot -> slot.hasStack() && slot.getStack().getItem() == Items.LIME_STAINED_GLASS_PANE)
               .findFirst()
               .ifPresent(
                  slot -> new Thread(
                        () -> {
                           try {
                              Thread.sleep(this.buyDelay.getIntValue() * 50);
                           } catch (InterruptedException var3x) {
                              var3x.printStackTrace();
                           }

                           this.mc
                              .execute(
                                 () -> this.mc
                                    .interactionManager
                                    .clickSlot(this.mc.player.currentScreenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, this.mc.player)
                              );
                        }
                     )
                     .start()
               );
         }

         if (this.mc.player != null
            && (
               this.mc.player.currentScreenHandler == null
                  || this.mc
                     .player
                     .currentScreenHandler
                     .slots
                     .stream()
                     .noneMatch(slot -> slot.hasStack() && slot.getStack().getItem() == Items.LIME_STAINED_GLASS_PANE)
            )) {
            return false;
         } else {
            this.toggle();
            return true;
         }
      }
   }

   private double parsePrice(String string) {
      if (string == null) {
         return -1.0;
      } else if (string.isEmpty()) {
         return -1.0;
      } else {
         String string2 = string.trim().toUpperCase();
         double d = 1.0;
         if (string2.endsWith("B")) {
            d = 1.0E9;
            string2 = string2.substring(0, string2.length() - 1);
         } else if (string2.endsWith("M")) {
            d = 1000000.0;
            string2 = string2.substring(0, string2.length() - 1);
         } else if (string2.endsWith("K")) {
            d = 1000.0;
            string2 = string2.substring(0, string2.length() - 1);
         }

         try {
            return Double.parseDouble(string2) * d;
         } catch (NumberFormatException var6) {
            return -1.0;
         }
      }
   }

   private String formatPrice(double d) {
      if (d >= 1.0E9) {
         Object[] objectArray = new Object[]{d / 1.0E9};
         return String.format("%.2fB", objectArray);
      } else if (d >= 1000000.0) {
         Object[] objectArray = new Object[]{d / 1000000.0};
         return String.format("%.2fM", objectArray);
      } else if (d >= 1000.0) {
         Object[] objectArray = new Object[]{d / 1000.0};
         return String.format("%.2fK", objectArray);
      } else {
         Object[] objectArray = new Object[]{d};
         return String.format("%.2f", objectArray);
      }
   }

   public static enum Mode {
      API("API", 0),
      MANUAL("MANUAL", 1);

      private Mode(final String name, final int ordinal) {
      }
   }
}
