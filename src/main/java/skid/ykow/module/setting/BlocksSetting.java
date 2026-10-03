package skid.ykow.module.setting;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlocksSetting extends Setting {
   private final Set<Block> defaultValue;
   private Set<Block> value;

   public BlocksSetting(CharSequence name, Set<Block> value) {
      super(name);
      this.value = new HashSet<>(value);
      this.defaultValue = new HashSet<>(value);
   }

   public BlocksSetting(CharSequence name, Block... blocks) {
      super(name);
      this.value = new HashSet<>();
      this.defaultValue = new HashSet<>();

      for (Block block : blocks) {
         this.value.add(block);
         this.defaultValue.add(block);
      }
   }

   public Set<Block> getBlocks() {
      return new HashSet<>(this.value);
   }

   public void setBlocks(Set<Block> blocks) {
      this.value = new HashSet<>(blocks);
   }

   public void addBlock(Block block) {
      if (block != null && block != Blocks.AIR) {
         this.value.add(block);
      }
   }

   public void removeBlock(Block block) {
      this.value.remove(block);
   }

   public boolean contains(Block block) {
      return this.value.contains(block);
   }

   public void toggleBlock(Block block) {
      if (this.contains(block)) {
         this.removeBlock(block);
      } else {
         this.addBlock(block);
      }
   }

   public void clearBlocks() {
      this.value.clear();
   }

   public int size() {
      return this.value.size();
   }

   public Set<Block> getDefaultValue() {
      return new HashSet<>(this.defaultValue);
   }

   public void resetValue() {
      this.value = new HashSet<>(this.defaultValue);
   }
}
