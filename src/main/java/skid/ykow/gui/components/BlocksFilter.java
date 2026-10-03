package skid.ykow.gui.components;

import skid.ykow.module.modules.client.Ykow;
import skid.ykow.module.setting.BlocksSetting;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public class BlocksFilter extends Screen {
   private final BlocksSetting setting;
   private String searchQuery;
   private final List<Block> allBlocks;
   private List<Block> filteredBlocks;
   private final Set<Block> selectedBlocks;
   private int scrollOffset;
   private final int BLOCKS_PER_ROW = 11;
   private final int MAX_ROWS_VISIBLE = 6;
   private final int BLOCK_SIZE = 40;
   private final int BLOCK_SPACING = 8;
   private boolean showingSelectedOnly = false;
   final BlocksBox this$0;

   public BlocksFilter(BlocksBox this$0, BlocksSetting setting) {
      super(Text.empty());
      this.this$0 = this$0;
      this.searchQuery = "";
      this.scrollOffset = 0;
      this.setting = setting;
      this.selectedBlocks = new HashSet<>(setting.getBlocks());
      this.allBlocks = new ArrayList<>();
      Registries.BLOCK.forEach(block -> {
         if (block != Blocks.AIR && block.asItem() instanceof BlockItem) {
            this.allBlocks.add(block);
         }
      });
      this.filteredBlocks = new ArrayList<>(this.allBlocks);
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
      int n6 = (width - 600) / 2;
      int n7 = (height - 580) / 2;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(30, 30, 35, 240), n6, n7, n6 + 600, n7 + 580, 8.0, 8.0, 8.0, 8.0, 20.0);
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(40, 40, 45, 255), n6, n7, n6 + 600, n7 + 30, 8.0, 8.0, 0.0, 0.0, 20.0);
      drawContext.fill(n6, n7 + 30, n6 + 600, n7 + 31, Utils.getMainColor(255, 1).getRGB());
      TextRenderer.drawCenteredString("Select Blocks: " + this.setting.getName(), drawContext, n6 + 300, n7 + 8, new Color(245, 245, 245, 255).getRGB());
      int n8 = n6 + 20;
      int n9 = n7 + 50;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(20, 20, 25, 255), n8, n9, n8 + 360, n9 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      RenderUtils.renderRoundedOutline(drawContext, new Color(60, 60, 65, 255), n8, n9, n8 + 360, n9 + 30, 5.0, 5.0, 5.0, 5.0, 1.0, 20.0);
      String searchQuery = this.searchQuery;
      String s;
      if (System.currentTimeMillis() % 1000L > 500L) {
         s = "|";
      } else {
         s = "";
      }

      TextRenderer.drawString("Search: " + searchQuery + s, drawContext, n8 + 10, n9 + 9, new Color(200, 200, 200, 255).getRGB());
      int selectedX = n8 + 360 + 20;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(40, 40, 50, 255), selectedX, n9, selectedX + 180, n9 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      RenderUtils.renderRoundedOutline(drawContext, Utils.getMainColor(150, 1), selectedX, n9, selectedX + 180, n9 + 30, 5.0, 5.0, 5.0, 5.0, 1.0, 20.0);
      TextRenderer.drawCenteredString("Selected: " + this.selectedBlocks.size(), drawContext, selectedX + 90, n9 + 9, Utils.getMainColor(255, 1).getRGB());
      int n10 = n6 + 20;
      int n11 = n9 + 30 + 15;
      int n12 = 580 - (n11 - n7) - 50;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(25, 25, 30, 255), n10, n11, n10 + 560, n11 + n12, 5.0, 5.0, 5.0, 5.0, 20.0);
      List<Block> displayBlocks = (List<Block>)(this.showingSelectedOnly ? new ArrayList<>(this.selectedBlocks) : this.filteredBlocks);
      double ceil = Math.ceil(displayBlocks.size() / 11.0);
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

      List<Block> displayBlocks2 = (List<Block>)(this.showingSelectedOnly ? new ArrayList<>(this.selectedBlocks) : this.filteredBlocks);

      int i;
      for (int n18 = i = this.scrollOffset * 11; i < Math.min(n18 + Math.min(displayBlocks2.size(), 66), displayBlocks2.size()); i++) {
         int n19 = n10 + 5 + (i - n18) % 11 * 48;
         int n20 = n11 + 5 + (i - n18) / 11 * 48;
         Block block = displayBlocks2.get(i);
         boolean isSelected = this.selectedBlocks.contains(block);
         Color bgColor;
         if (isSelected) {
            bgColor = Utils.getMainColor(120, 1);
         } else {
            bgColor = new Color(35, 35, 40, 255);
         }

         RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor, n19, n20, n19 + 40, n20 + 40, 4.0, 4.0, 4.0, 4.0, 20.0);
         RenderUtils.drawItem(drawContext, new ItemStack(block.asItem()), n19, n20, 40.0F, 0);
         if (isSelected) {
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), Utils.getMainColor(200, 1), n19 + 28, n20 + 2, n19 + 38, n20 + 12, 2.0, 2.0, 2.0, 2.0, 20.0
            );
            TextRenderer.drawCenteredString("✓", drawContext, n19 + 33, n20 + 3, new Color(255, 255, 255, 255).getRGB());
         }

         if (n4 >= n19 && n4 <= n19 + 40 && n5 >= n20 && n5 <= n20 + 40) {
            RenderUtils.renderRoundedOutline(drawContext, Utils.getMainColor(200, 1), n19, n20, n19 + 40, n20 + 40, 4.0, 4.0, 4.0, 4.0, 1.5, 20.0);
         }
      }

      if (displayBlocks2.isEmpty()) {
         String emptyText = this.showingSelectedOnly ? "No blocks selected" : "No blocks found";
         TextRenderer.drawCenteredString(emptyText, drawContext, n10 + 280, n11 + n12 / 2 - 10, new Color(150, 150, 150, 200).getRGB());
      }

      int n21 = n7 + 580 - 45;
      int n22 = n6 + 600 - 80 - 20;
      int n23 = n22 - 90 - 10;
      int n24 = n23 - 100 - 10;
      int n25 = n24 - 80 - 10;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Utils.getMainColor(255, 1), n22, n21, n22 + 80, n21 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Save", drawContext, n22 + 40, n21 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(60, 60, 65, 255), n23, n21, n23 + 90, n21 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Cancel", drawContext, n23 + 45, n21 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(70, 40, 40, 255), n24, n21, n24 + 100, n21 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Clear All", drawContext, n24 + 50, n21 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.renderRoundedQuad(
         drawContext.getMatrices(),
         this.showingSelectedOnly ? Utils.getMainColor(200, 1) : new Color(50, 50, 60, 255),
         n25,
         n21,
         n25 + 80,
         n21 + 30,
         5.0,
         5.0,
         5.0,
         5.0,
         20.0
      );
      TextRenderer.drawCenteredString(this.showingSelectedOnly ? "Back" : "Selected", drawContext, n25 + 40, n21 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.scaledProjection();
   }

   public boolean mouseClicked(double n, double n2, int n3) {
      double n4 = n * MinecraftClient.getInstance().getWindow().getScaleFactor();
      double n5 = n2 * MinecraftClient.getInstance().getWindow().getScaleFactor();
      int n6 = (this.this$0.mc.getWindow().getWidth() - 600) / 2;
      int n7 = (this.this$0.mc.getWindow().getHeight() - 580) / 2;
      int n8 = n7 + 580 - 45;
      int n9 = n6 + 600 - 80 - 20;
      int n10 = n9 - 90 - 10;
      int n11 = n10 - 100 - 10;
      int n25 = n11 - 80 - 10;
      int selectedX = n6 + 20 + 360 + 20;
      int selectedY = n7 + 50;
      if (this.isInBounds(n4, n5, selectedX, selectedY, 180, 30)) {
         this.showingSelectedOnly = !this.showingSelectedOnly;
         this.scrollOffset = 0;
         return true;
      } else if (this.isInBounds(n4, n5, n9, n8, 80, 30)) {
         this.setting.setBlocks(this.selectedBlocks);
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (this.isInBounds(n4, n5, n10, n8, 90, 30)) {
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (this.isInBounds(n4, n5, n11, n8, 100, 30)) {
         this.selectedBlocks.clear();
         return true;
      } else if (this.isInBounds(n4, n5, n25, n8, 80, 30)) {
         this.showingSelectedOnly = !this.showingSelectedOnly;
         this.scrollOffset = 0;
         return true;
      } else {
         int n12 = n6 + 20;
         int n13 = n7 + 50 + 30 + 15;
         int gridHeight = 580 - (n13 - n7) - 50;
         if (this.isInBounds(n4, n5, n12, n13, 560, gridHeight)) {
            List<Block> displayBlocks = (List<Block>)(this.showingSelectedOnly ? new ArrayList<>(this.selectedBlocks) : this.filteredBlocks);
            int n14 = this.scrollOffset * 11;
            int n15 = (int)(n4 - n12 - 5.0) / 48;
            if (n15 >= 0 && n15 < 11) {
               int blockIndex = n14 + (int)(n5 - n13 - 5.0) / 48 * 11 + n15;
               if (blockIndex >= 0 && blockIndex < displayBlocks.size()) {
                  Block clickedBlock = displayBlocks.get(blockIndex);
                  if (this.selectedBlocks.contains(clickedBlock)) {
                     this.selectedBlocks.remove(clickedBlock);
                  } else {
                     this.selectedBlocks.add(clickedBlock);
                  }

                  return true;
               }
            }
         }

         return super.mouseClicked(n4, n5, n3);
      }
   }

   public boolean mouseScrolled(double n, double n2, double n3, double n4) {
      double n5 = n * MinecraftClient.getInstance().getWindow().getScaleFactor();
      double n6 = n2 * MinecraftClient.getInstance().getWindow().getScaleFactor();
      int width = this.this$0.mc.getWindow().getWidth();
      int n7 = (this.this$0.mc.getWindow().getHeight() - 580) / 2;
      int n8 = n7 + 50 + 30 + 15;
      int gridHeight = 580 - (n8 - n7) - 50;
      if (this.isInBounds(n5, n6, (width - 600) / 2 + 20, n8, 560, gridHeight)) {
         List<Block> displayBlocks = (List<Block>)(this.showingSelectedOnly ? new ArrayList<>(this.selectedBlocks) : this.filteredBlocks);
         int max = Math.max(0, (int)Math.ceil(displayBlocks.size() / 11.0) - 6);
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
         this.setting.setBlocks(this.selectedBlocks);
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (n == 259) {
         if (!this.searchQuery.isEmpty()) {
            this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
            this.updateFilteredBlocks();
         }

         return true;
      } else if (n == 257) {
         this.setting.setBlocks(this.selectedBlocks);
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else {
         return super.keyPressed(n, n2, n3);
      }
   }

   public boolean charTyped(char c, int n) {
      this.searchQuery = this.searchQuery + c;
      this.updateFilteredBlocks();
      return true;
   }

   private void updateFilteredBlocks() {
      if (this.searchQuery.isEmpty()) {
         this.filteredBlocks = new ArrayList<>(this.allBlocks);
      } else {
         this.filteredBlocks = this.allBlocks
            .stream()
            .filter(block -> block.getName().getString().toLowerCase().contains(this.searchQuery.toLowerCase()))
            .collect(Collectors.toList());
      }

      this.scrollOffset = 0;
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
