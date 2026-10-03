package skid.ykow.module.modules.donut;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.EntitySpawnEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity.RemovalReason;

public final class AntiTrap extends Module {
   public AntiTrap() {
      super(EncryptedString.of("Anti Trap"), EncryptedString.of("Module that helps you escape Polish traps"), -1, Category.DONUT);
      this.addsettings(new Setting[0]);
   }

   @Override
   public void onEnable() {
      this.removeTrapEntities();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onEntitySpawn(EntitySpawnEvent entitySpawnEvent) {
      if (this.isTrapEntity(entitySpawnEvent.packet.getEntityType())) {
         entitySpawnEvent.cancel();
      }
   }

   private void removeTrapEntities() {
      if (this.mc.world != null) {
         ArrayList<Entity> trapEntities = new ArrayList<>();
         this.mc.world.getEntities().forEach(entity -> {
            if (entity != null && this.isTrapEntity(entity.getType())) {
               trapEntities.add(entity);
            }
         });
         trapEntities.forEach(trapEntity -> {
            if (!trapEntity.isRemoved()) {
               trapEntity.remove(RemovalReason.DISCARDED);
            }
         });
      }
   }

   private boolean isTrapEntity(EntityType<?> entityType) {
      return entityType != null && (entityType.equals(EntityType.ARMOR_STAND) || entityType.equals(EntityType.CHEST_MINECART));
   }
}
