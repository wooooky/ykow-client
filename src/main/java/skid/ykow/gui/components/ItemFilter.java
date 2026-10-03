package skid.ykow.gui.components;

import skid.ykow.module.modules.client.Ykow;
import skid.ykow.module.setting.ItemSetting;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public class ItemFilter extends Screen {
   private final ItemSetting setting;
   private String searchQuery;
   private final List<Item> allItems;
   private List<Item> filteredItems;
   private int scrollOffset;
   private final int ITEMS_PER_ROW = 11;
   private final int MAX_ROWS_VISIBLE = 6;
   private int selectedIndex;
   private final int ITEM_SIZE = 40;
   private final int ITEM_SPACING = 8;
   final ItemBox this$0;

   public ItemFilter(ItemBox this$0, ItemSetting setting) {
      super(Text.empty());
      this.this$0 = this$0;
      this.searchQuery = "";
      this.scrollOffset = 0;
      this.selectedIndex = -1;
      this.setting = setting;
      this.allItems = new ArrayList<>();
      Registries.ITEM.forEach(item -> {
         if (item != Items.AIR) {
            this.allItems.add(item);
         }
      });
      this.filteredItems = new ArrayList<>(this.allItems);
      if (setting.getItem() != null && setting.getItem() != Items.AIR) {
         for (int i = 0; i < this.filteredItems.size(); i++) {
            if (this.filteredItems.get(i) == setting.getItem()) {
               this.selectedIndex = i;
               break;
            }
         }
      }
   }

   public void render(DrawContext drawContext, int n, int n2, float n3) {
      RenderUtils.unscaledProjection();
      int n4 = n * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
      int n5 = n2 * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
      super.render(drawContext, n4, n5, n3);
      int width = this.this$0.mc.getWindow().getWidth();
      int height = this.this$0.mc.getWindow().getHeight();
      int a;
      if (Ykow.renderBackground.getValue()) {
         a = 180;
      } else {
         a = 0;
      }

      drawContext.fill(0, 0, width, height, new Color(0, 0, 0, a).getRGB());
      int n6 = (this.this$0.mc.getWindow().getWidth() - 600) / 2;
      int n7 = (this.this$0.mc.getWindow().getHeight() - 500) / 2;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(30, 30, 35, 240), n6, n7, n6 + 600, n7 + 500, 8.0, 8.0, 8.0, 8.0, 20.0);
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(40, 40, 45, 255), n6, n7, n6 + 600, n7 + 30, 8.0, 8.0, 0.0, 0.0, 20.0);
      drawContext.fill(n6, n7 + 30, n6 + 600, n7 + 31, Utils.getMainColor(255, 1).getRGB());
      TextRenderer.drawCenteredString("Select Item: " + this.setting.getName(), drawContext, n6 + 300, n7 + 8, new Color(245, 245, 245, 255).getRGB());
      int n8 = n6 + 20;
      int n9 = n7 + 50;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(20, 20, 25, 255), n8, n9, n8 + 560, n9 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      RenderUtils.renderRoundedOutline(drawContext, new Color(60, 60, 65, 255), n8, n9, n8 + 560, n9 + 30, 5.0, 5.0, 5.0, 5.0, 1.0, 20.0);
      String searchQuery = this.searchQuery;
      String s;
      if (System.currentTimeMillis() % 1000L > 500L) {
         s = "|";
      } else {
         s = "";
      }

      TextRenderer.drawString("Search: " + searchQuery + s, drawContext, n8 + 10, n9 + 9, new Color(200, 200, 200, 255).getRGB());
      int n10 = n6 + 20;
      int n11 = n9 + 30 + 15;
      int n12 = 500 - (n11 - n7) - 60;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(25, 25, 30, 255), n10, n11, n10 + 560, n11 + n12, 5.0, 5.0, 5.0, 5.0, 20.0);
      double ceil = Math.ceil(this.filteredItems.size() / 11.0);
      int max = Math.max(0, (int)ceil - 6);
      this.scrollOffset = Math.min(this.scrollOffset, max);
      if ((int)ceil > 6) {
         int n13 = n10 + 560 - 6 - 5;
         int n14 = n11 + 5;
         int n15 = n12 - 10;
         RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(20, 20, 25, 150), n13, n14, n13 + 6, n14 + n15, 3.0, 3.0, 3.0, 3.0, 20.0);
         float n16 = (float)this.scrollOffset / max;
         float max2 = Math.max(40.0F, n15 * (6.0F / (int)ceil));
         int n17 = n14 + (int)((n15 - max2) * n16);
         RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Utils.getMainColor(255, 1), n13, n17, n13 + 6, n17 + max2, 3.0, 3.0, 3.0, 3.0, 20.0);
      }

      int i;
      for (int n18 = i = this.scrollOffset * 11; i < Math.min(n18 + Math.min(this.filteredItems.size(), 66), this.filteredItems.size()); i++) {
         int n19 = n10 + 5 + (i - n18) % 11 * 48;
         int n20 = n11 + 5 + (i - n18) / 11 * 48;
         Color mainColor;
         if (i == this.selectedIndex) {
            mainColor = Utils.getMainColor(100, 1);
         } else {
            mainColor = new Color(35, 35, 40, 255);
         }

         RenderUtils.renderRoundedQuad(drawContext.getMatrices(), mainColor, n19, n20, n19 + 40, n20 + 40, 4.0, 4.0, 4.0, 4.0, 20.0);
         RenderUtils.drawItem(drawContext, new ItemStack((ItemConvertible)this.filteredItems.get(i)), n19, n20, 40.0F, 0);
         if (n4 >= n19 && n4 <= n19 + 40 && n5 >= n20 && n5 <= n20 + 40) {
            RenderUtils.renderRoundedOutline(drawContext, Utils.getMainColor(200, 1), n19, n20, n19 + 40, n20 + 40, 4.0, 4.0, 4.0, 4.0, 1.0, 20.0);
         }
      }

      if (this.filteredItems.isEmpty()) {
         TextRenderer.drawCenteredString("No items found", drawContext, n10 + 280, n11 + n12 / 2 - 10, new Color(150, 150, 150, 200).getRGB());
      }

      int n21 = n7 + 500 - 45;
      int n22 = n6 + 600 - 80 - 20;
      int n23 = n22 - 80 - 10;
      int n24 = n23 - 80 - 10;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Utils.getMainColor(255, 1), n22, n21, n22 + 80, n21 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Save", drawContext, n22 + 40, n21 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(60, 60, 65, 255), n23, n21, n23 + 80, n21 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Cancel", drawContext, n23 + 40, n21 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(70, 40, 40, 255), n24, n21, n24 + 80, n21 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Reset", drawContext, n24 + 40, n21 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.scaledProjection();
   }

   public boolean mouseClicked(double n, double n2, int n3) {
      double n4 = n * MinecraftClient.getInstance().getWindow().getScaleFactor();
      double n5 = n2 * MinecraftClient.getInstance().getWindow().getScaleFactor();
      int n6 = (this.this$0.mc.getWindow().getWidth() - 600) / 2;
      int n7 = (this.this$0.mc.getWindow().getHeight() - 500) / 2;
      int n8 = n7 + 500 - 45;
      int n9 = n6 + 600 - 80 - 20;
      int n10 = n9 - 80 - 10;
      if (this.isInBounds(n4, n5, n9, n8, 80, 30)) {
         if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredItems.size()) {
            this.setting.setItem(this.filteredItems.get(this.selectedIndex));
         }

         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (this.isInBounds(n4, n5, n10, n8, 80, 30)) {
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (!this.isInBounds(n4, n5, n10 - 80 - 10, n8, 80, 30)) {
         int n11 = n6 + 20;
         int n12 = n7 + 50 + 30 + 15;
         if (this.isInBounds(n4, n5, n11, n12, 560, 500 - (n12 - n7) - 60)) {
            int n13 = this.scrollOffset * 11;
            int n14 = (int)(n4 - n11 - 5.0) / 48;
            if (n14 >= 0 && n14 < 11) {
               int selectedIndex = n13 + (int)(n5 - n12 - 5.0) / 48 * 11 + n14;
               if (selectedIndex >= 0 && selectedIndex < this.filteredItems.size()) {
                  this.selectedIndex = selectedIndex;
                  return true;
               }
            }
         }

         return super.mouseClicked(n4, n5, n3);
      } else {
         this.setting.setItem(this.setting.getDefaultValue());
         this.selectedIndex = -1;

         for (int i = 0; i < this.filteredItems.size(); i++) {
            if (this.filteredItems.get(i) == this.setting.getDefaultValue()) {
               this.selectedIndex = i;
               break;
            }
         }

         return true;
      }
   }

   public boolean mouseScrolled(double n, double n2, double n3, double n4) {
      double n5 = n * MinecraftClient.getInstance().getWindow().getScaleFactor();
      double n6 = n2 * MinecraftClient.getInstance().getWindow().getScaleFactor();
      int width = this.this$0.mc.getWindow().getWidth();
      int n7 = (this.this$0.mc.getWindow().getHeight() - 500) / 2;
      int n8 = n7 + 50 + 30 + 15;
      if (this.isInBounds(n5, n6, (width - 600) / 2 + 20, n8, 560, 500 - (n8 - n7) - 60)) {
         int max = Math.max(0, (int)Math.ceil(this.filteredItems.size() / 11.0) - 6);
         if (n4 > 0.0) {
            this.scrollOffset = Math.max(0, this.scrollOffset - 1);
         } else if (n4 < 0.0) {
            this.scrollOffset = Math.min(max, this.scrollOffset + 1);
         }

         return true;
      } else {
         return super.mouseScrolled(n5, n6, n3, n4);
      }
   }

   public boolean keyPressed(int n, int n2, int n3) {
      if (n == 256) {
         if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredItems.size()) {
            this.setting.setItem(this.filteredItems.get(this.selectedIndex));
         }

         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (n == 259) {
         if (!this.searchQuery.isEmpty()) {
            this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
            this.updateFilteredItems();
         }

         return true;
      } else if (n == 265) {
         if (this.selectedIndex >= 11) {
            this.selectedIndex -= 11;
            this.ensureSelectedItemVisible();
         }

         return true;
      } else if (n == 264) {
         if (this.selectedIndex + 11 < this.filteredItems.size()) {
            this.selectedIndex += 11;
            this.ensureSelectedItemVisible();
         }

         return true;
      } else if (n == 263) {
         if (this.selectedIndex > 0) {
            this.selectedIndex--;
            this.ensureSelectedItemVisible();
         }

         return true;
      } else if (n == 262) {
         if (this.selectedIndex < this.filteredItems.size() - 1) {
            this.selectedIndex++;
            this.ensureSelectedItemVisible();
         }

         return true;
      } else if (n == 257) {
         if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredItems.size()) {
            this.setting.setItem(this.filteredItems.get(this.selectedIndex));
            this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         }

         return true;
      } else {
         return super.keyPressed(n, n2, n3);
      }
   }

   public boolean charTyped(char c, int n) {
      this.searchQuery = this.searchQuery + c;
      this.updateFilteredItems();
      return true;
   }

   private void updateFilteredItems() {
      if (this.searchQuery.isEmpty()) {
         this.filteredItems = new ArrayList<>(this.allItems);
      } else {
         this.filteredItems = this.allItems
            .stream()
            .filter(item -> item.getName().getString().toLowerCase().contains(this.searchQuery.toLowerCase()))
            .collect(Collectors.toList());
      }

      this.scrollOffset = 0;
      this.selectedIndex = -1;
      Item a = this.setting.getItem();
      if (a != null) {
         for (int i = 0; i < this.filteredItems.size(); i++) {
            if (this.filteredItems.get(i) == a) {
               this.selectedIndex = i;
               break;
            }
         }
      }
   }

   private void ensureSelectedItemVisible() {
      if (this.selectedIndex >= 0) {
         int scrollOffset = this.selectedIndex / 11;
         if (scrollOffset < this.scrollOffset) {
            this.scrollOffset = scrollOffset;
         } else if (scrollOffset >= this.scrollOffset + 6) {
            this.scrollOffset = scrollOffset - 6 + 1;
         }
      }
   }

   private boolean isInBounds(double n, double n2, int n3, int n4, int n5, int n6) {
      return n >= n3 && n <= n3 + n5 && n2 >= n4 && n2 <= n4 + n6;
   }

   public void renderBackground(DrawContext drawContext, int n, int n2, float n3) {
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}
