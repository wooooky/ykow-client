package skid.ykow.gui.components;

import skid.ykow.module.modules.client.Ykow;
import skid.ykow.module.setting.FriendsSetting;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class FriendsFilter extends Screen {
   private final FriendsSetting setting;
   private final List<String> friendsList;
   private String inputText;
   private int scrollOffset;
   private int selectedIndex;
   private boolean isTyping;
   final FriendsBox this$0;

   public FriendsFilter(FriendsBox this$0, FriendsSetting setting) {
      super(Text.empty());
      this.this$0 = this$0;
      this.inputText = "";
      this.scrollOffset = 0;
      this.selectedIndex = -1;
      this.isTyping = false;
      this.setting = setting;
      this.friendsList = new ArrayList<>(setting.getFriends());
   }

   public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
      RenderUtils.unscaledProjection();
      int scaledMouseX = mouseX * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
      int scaledMouseY = mouseY * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
      super.render(drawContext, scaledMouseX, scaledMouseY, delta);
      int screenWidth = this.this$0.mc.getWindow().getWidth();
      int screenHeight = this.this$0.mc.getWindow().getHeight();
      int bgAlpha = Ykow.renderBackground.getValue() ? 180 : 0;
      drawContext.fill(0, 0, screenWidth, screenHeight, new Color(0, 0, 0, bgAlpha).getRGB());
      int panelWidth = 500;
      int panelHeight = 450;
      int panelX = (screenWidth - 500) / 2;
      int panelY = (screenHeight - 450) / 2;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(30, 30, 35, 240), panelX, panelY, panelX + 500, panelY + 450, 8.0, 8.0, 8.0, 8.0, 20.0);
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(40, 40, 45, 255), panelX, panelY, panelX + 500, panelY + 40, 8.0, 8.0, 0.0, 0.0, 20.0);
      drawContext.fill(panelX, panelY + 40, panelX + 500, panelY + 41, Utils.getMainColor(255, 1).getRGB());
      TextRenderer.drawCenteredString("FRIENDS LIST", drawContext, panelX + 250, panelY + 12, new Color(245, 245, 245, 255).getRGB());
      int inputX = panelX + 20;
      int inputY = panelY + 60;
      int inputWidth = 380;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(20, 20, 25, 255), inputX, inputY, inputX + 380, inputY + 35, 5.0, 5.0, 5.0, 5.0, 20.0);
      RenderUtils.renderRoundedOutline(
         drawContext,
         this.isTyping ? Utils.getMainColor(150, 1) : new Color(60, 60, 65, 255),
         inputX,
         inputY,
         inputX + 380,
         inputY + 35,
         5.0,
         5.0,
         5.0,
         5.0,
         1.5,
         20.0
      );
      String displayText = this.inputText;
      if (this.isTyping && System.currentTimeMillis() % 1000L > 500L) {
         displayText = displayText + "|";
      }

      String placeholder = this.inputText.isEmpty() && !this.isTyping ? "Player Name" : displayText;
      Color textColor = this.inputText.isEmpty() && !this.isTyping ? new Color(120, 120, 120, 200) : new Color(200, 200, 200, 255);
      if (TextRenderer.getWidth(placeholder) > 360) {
         while (TextRenderer.getWidth(placeholder) > 360 && placeholder.length() > 0) {
            placeholder = placeholder.substring(0, placeholder.length() - 1);
         }
      }

      TextRenderer.drawString(placeholder, drawContext, inputX + 10, inputY + 12, textColor.getRGB());
      int addBtnX = inputX + 380 + 10;
      boolean addHovered = this.isInBounds(scaledMouseX, scaledMouseY, addBtnX, inputY, 70, 35);
      Color addBtnColor = addHovered ? Utils.getMainColor(255, 1) : Utils.getMainColor(200, 1);
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), addBtnColor, addBtnX, inputY, addBtnX + 70, inputY + 35, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("ADD ITEM", drawContext, addBtnX + 35, inputY + 12, new Color(245, 245, 245, 255).getRGB());
      int listX = panelX + 20;
      int listY = inputY + 50;
      int listWidth = 460;
      int listHeight = 230;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(25, 25, 30, 255), listX, listY, listX + 460, listY + 230, 5.0, 5.0, 5.0, 5.0, 20.0);
      int itemY = listY + 10;
      int itemHeight = 40;
      int visibleItems = 5;
      if (this.friendsList.isEmpty()) {
         TextRenderer.drawCenteredString("No friends added yet", drawContext, listX + 230, listY + 115 - 10, new Color(150, 150, 150, 200).getRGB());
      } else {
         int startIndex = Math.max(0, this.scrollOffset);
         int endIndex = Math.min(this.friendsList.size(), startIndex + 5);

         for (int i = startIndex; i < endIndex; i++) {
            String friendName = this.friendsList.get(i);
            int itemX = listX + 10;
            int currentItemY = itemY + (i - startIndex) * 40;
            boolean isHovered = this.isInBounds(scaledMouseX, scaledMouseY, itemX, currentItemY, 440, 35);
            boolean isSelected = i == this.selectedIndex;
            Color itemBg = isSelected ? Utils.getMainColor(80, 1) : (isHovered ? new Color(35, 35, 40, 200) : new Color(30, 30, 35, 150));
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), itemBg, itemX, currentItemY, itemX + 460 - 20, currentItemY + 40 - 5, 4.0, 4.0, 4.0, 4.0, 20.0
            );
            TextRenderer.drawString(friendName, drawContext, itemX + 10, currentItemY + 13, new Color(230, 230, 230, 255).getRGB());
            int removeBtnX = itemX + 460 - 55;
            int removeBtnY = currentItemY + 7;
            boolean removeHovered = this.isInBounds(scaledMouseX, scaledMouseY, removeBtnX, removeBtnY, 25, 25);
            Color removeBg = removeHovered ? new Color(239, 68, 68, 150) : new Color(60, 60, 70, 100);
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), removeBg, removeBtnX, removeBtnY, removeBtnX + 25, removeBtnY + 25, 4.0, 4.0, 4.0, 4.0, 20.0
            );
            TextRenderer.drawCenteredString("✕", drawContext, removeBtnX + 12, removeBtnY + 6, new Color(245, 245, 245, 255).getRGB());
         }

         if (this.friendsList.size() > 5) {
            int scrollbarX = listX + 460 - 8;
            int scrollbarY = listY + 5;
            int scrollbarHeight = 220;
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), new Color(20, 20, 25, 150), scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + 220, 3.0, 3.0, 3.0, 3.0, 20.0
            );
            float scrollPercentage = (float)this.scrollOffset / Math.max(1, this.friendsList.size() - 5);
            float thumbHeight = Math.max(40.0F, 220.0F * (5.0F / this.friendsList.size()));
            int thumbY = scrollbarY + (int)((220.0F - thumbHeight) * scrollPercentage);
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), Utils.getMainColor(255, 1), scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 3.0, 3.0, 3.0, 3.0, 20.0
            );
         }
      }

      int btnY = panelY + 450 - 50;
      int saveX = panelX + 500 - 90 - 20;
      int cancelX = saveX - 90 - 10;
      boolean saveHovered = this.isInBounds(scaledMouseX, scaledMouseY, saveX, btnY, 90, 35);
      Color saveBg = saveHovered ? Utils.getMainColor(255, 1) : Utils.getMainColor(200, 1);
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), saveBg, saveX, btnY, saveX + 90, btnY + 35, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("SAVE", drawContext, saveX + 45, btnY + 12, new Color(245, 245, 245, 255).getRGB());
      boolean cancelHovered = this.isInBounds(scaledMouseX, scaledMouseY, cancelX, btnY, 90, 35);
      Color cancelBg = cancelHovered ? new Color(70, 70, 75, 255) : new Color(60, 60, 65, 255);
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), cancelBg, cancelX, btnY, cancelX + 90, btnY + 35, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("CANCEL", drawContext, cancelX + 45, btnY + 12, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.scaledProjection();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      double scaledX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
      double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
      int screenWidth = this.this$0.mc.getWindow().getWidth();
      int screenHeight = this.this$0.mc.getWindow().getHeight();
      int panelWidth = 500;
      int panelHeight = 450;
      int panelX = (screenWidth - 500) / 2;
      int panelY = (screenHeight - 450) / 2;
      int inputX = panelX + 20;
      int inputY = panelY + 60;
      int inputWidth = 380;
      if (this.isInBounds(scaledX, scaledY, inputX, inputY, 380, 35)) {
         this.isTyping = true;
         return true;
      } else {
         this.isTyping = false;
         int addBtnX = inputX + 380 + 10;
         if (this.isInBounds(scaledX, scaledY, addBtnX, inputY, 70, 35)) {
            if (!this.inputText.trim().isEmpty()) {
               this.friendsList.add(this.inputText.trim());
               this.inputText = "";
            }

            return true;
         } else {
            int listX = panelX + 20;
            int listY = inputY + 50;
            int listWidth = 460;
            int listHeight = 230;
            int itemHeight = 40;
            int visibleItems = 5;
            int itemY = listY + 10;
            int startIndex = Math.max(0, this.scrollOffset);
            int endIndex = Math.min(this.friendsList.size(), startIndex + 5);

            for (int i = startIndex; i < endIndex; i++) {
               int itemX = listX + 10;
               int currentItemY = itemY + (i - startIndex) * 40;
               int removeBtnX = itemX + 460 - 55;
               int removeBtnY = currentItemY + 7;
               if (this.isInBounds(scaledX, scaledY, removeBtnX, removeBtnY, 25, 25)) {
                  this.friendsList.remove(i);
                  if (this.selectedIndex == i) {
                     this.selectedIndex = -1;
                  } else if (this.selectedIndex > i) {
                     this.selectedIndex--;
                  }

                  return true;
               }

               if (this.isInBounds(scaledX, scaledY, itemX, currentItemY, 440, 35)) {
                  this.selectedIndex = i;
                  return true;
               }
            }

            int btnY = panelY + 450 - 50;
            int saveX = panelX + 500 - 90 - 20;
            if (this.isInBounds(scaledX, scaledY, saveX, btnY, 90, 35)) {
               this.setting.setFriends(this.friendsList);
               this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
               return true;
            } else {
               int cancelX = saveX - 90 - 10;
               if (this.isInBounds(scaledX, scaledY, cancelX, btnY, 90, 35)) {
                  this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
                  return true;
               } else {
                  return super.mouseClicked(scaledX, scaledY, button);
               }
            }
         }
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      double scaledX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
      double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
      int screenWidth = this.this$0.mc.getWindow().getWidth();
      int screenHeight = this.this$0.mc.getWindow().getHeight();
      int panelWidth = 500;
      int panelHeight = 450;
      int panelX = (screenWidth - 500) / 2;
      int panelY = (screenHeight - 450) / 2;
      int listX = panelX + 20;
      int listY = panelY + 110;
      int listWidth = 460;
      int listHeight = 230;
      if (this.isInBounds(scaledX, scaledY, listX, listY, 460, 230)) {
         int visibleItems = 5;
         int maxScroll = Math.max(0, this.friendsList.size() - 5);
         if (verticalAmount > 0.0) {
            this.scrollOffset = Math.max(0, this.scrollOffset - 1);
         } else if (verticalAmount < 0.0) {
            this.scrollOffset = Math.min(maxScroll, this.scrollOffset + 1);
         }

         return true;
      } else {
         return super.mouseScrolled(scaledX, scaledY, horizontalAmount, verticalAmount);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.setting.setFriends(this.friendsList);
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else {
         if (this.isTyping) {
            if (keyCode == 259) {
               if (!this.inputText.isEmpty()) {
                  this.inputText = this.inputText.substring(0, this.inputText.length() - 1);
               }

               return true;
            }

            if (keyCode == 257) {
               if (!this.inputText.trim().isEmpty()) {
                  this.friendsList.add(this.inputText.trim());
                  this.inputText = "";
               }

               return true;
            }
         }

         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.isTyping && !Character.isISOControl(chr)) {
         this.inputText = this.inputText + chr;
         return true;
      } else {
         return super.charTyped(chr, modifiers);
      }
   }

   private boolean isInBounds(double x, double y, int boundX, int boundY, int width, int height) {
      return x >= boundX && x <= boundX + width && y >= boundY && y <= boundY + height;
   }

   public void renderBackground(DrawContext drawContext, int n, int n2, float n3) {
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}
