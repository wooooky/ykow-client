package skid.ykow.event.events;

import skid.ykow.event.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HeldItemRendererEvent implements Event {
   private final Hand hand;
   private final ItemStack item;
   private final float equipProgress;
   private final MatrixStack matrices;

   public HeldItemRendererEvent(Hand hand, ItemStack item, float equipProgress, MatrixStack matrices) {
      this.hand = hand;
      this.item = item;
      this.equipProgress = equipProgress;
      this.matrices = matrices;
   }

   public Hand getHand() {
      return this.hand;
   }

   public ItemStack getItem() {
      return this.item;
   }

   public float getEquipProgress() {
      return this.equipProgress;
   }

   public MatrixStack getMatrices() {
      return this.matrices;
   }
}
