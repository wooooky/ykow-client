package skid.ykow.utils;

import skid.ykow.module.modules.client.Ykow;
import skid.ykow.module.modules.client.SelfDestruct;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public final class Utils {
   public static Color getMainColor(int alpha, int offset) {
      int red = Ykow.redColor.getIntValue();
      int green = Ykow.greenColor.getIntValue();
      int blue = Ykow.blueColor.getIntValue();
      if (Ykow.enableRainbowEffect.getValue()) {
         return ColorUtil.getRainbowColor(offset, alpha);
      } else {
         return Ykow.enableBreathingEffect.getValue()
            ? ColorUtil.getBreathingColor(new Color(red, green, blue, alpha), offset, 20)
            : new Color(red, green, blue, alpha);
      }
   }

   public static File getCurrentJarPath() throws URISyntaxException {
      return new File(SelfDestruct.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
   }

   public static void downloadAndOverwriteFile(String url, File targetFile) {
      try {
         HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
         connection.setRequestMethod("GET");
         InputStream inputStream = connection.getInputStream();
         FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
         byte[] buffer = new byte[1024];

         while (true) {
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == -1) {
               fileOutputStream.close();
               inputStream.close();
               connection.disconnect();
               break;
            }

            fileOutputStream.write(buffer, 0, bytesRead);
         }
      } catch (Throwable var7) {
         var7.printStackTrace(System.err);
      }
   }

   public static void copyVector(Vector3d destination, Vec3d source) {
      destination.x = source.x;
      destination.y = source.y;
      destination.z = source.z;
   }

   public static float getTick() {
      return skid.ykow.Ykow.mc.getRenderTickCounter().getTickDelta(false);
   }
}
