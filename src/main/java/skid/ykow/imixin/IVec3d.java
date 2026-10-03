package skid.ykow.imixin;

import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IVec3d {
   void set(double var1, double var3, double var5);

   default void keyCodec(Vec3i vec3i) {
      this.set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
   }

   default void keyCodec(Vector3d vector3d) {
      this.set(vector3d.x, vector3d.y, vector3d.z);
   }

   void setXZ(double var1, double var3);

   void setY(double var1);
}
