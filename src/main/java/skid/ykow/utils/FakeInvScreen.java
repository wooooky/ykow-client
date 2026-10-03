package skid.ykow.utils;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class FakeInvScreen extends InventoryScreen {
   public FakeInvScreen(PlayerEntity playerEntity) {
      super(playerEntity);
   }

   protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return false;
   }
}
