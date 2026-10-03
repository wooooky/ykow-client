package skid.ykow.manager;

import java.io.IOException;
import java.nio.FloatBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class ShaderManager {
   private static ShaderProgram roundedQuadShader;
   private static int vao;
   private static int vbo;
   private static boolean initialized = false;

   public static void init(ResourceManager resourceManager) {
      if (!initialized) {
         try {
            roundedQuadShader = new ShaderProgram(resourceManager, "ykow:rounded_quad", VertexFormats.POSITION_TEXTURE);
            vao = GL30.glGenVertexArrays();
            vbo = GL20.glGenBuffers();
            setupVertexData();
            initialized = true;
         } catch (IOException var2) {
            System.err.println("Failed to initialize shaders: " + var2.getMessage());
            var2.printStackTrace();
         }
      }
   }

   private static void setupVertexData() {
      float[] vertices = new float[]{0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F};
      GL30.glBindVertexArray(vao);
      GL20.glBindBuffer(34962, vbo);
      FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
      buffer.put(vertices).flip();
      GL20.glBufferData(34962, buffer, 35044);
      MemoryUtil.memFree(buffer);
      GL20.glVertexAttribPointer(0, 2, 5126, false, 16, 0L);
      GL20.glEnableVertexAttribArray(0);
      GL20.glVertexAttribPointer(1, 2, 5126, false, 16, 8L);
      GL20.glEnableVertexAttribArray(1);
      GL30.glBindVertexArray(0);
   }

   public static void renderRoundedQuad(
      float x, float y, float width, float height, float r, float g, float b, float a, float topLeft, float topRight, float bottomRight, float bottomLeft
   ) {
      if (initialized && roundedQuadShader != null) {
         try {
            roundedQuadShader.bind();
            int programId = GL20.glGetInteger(35725);
            int colorLocation = GL20.glGetUniformLocation(programId, "color");
            int sizeLocation = GL20.glGetUniformLocation(programId, "size");
            int radiusLocation = GL20.glGetUniformLocation(programId, "radius");
            int smoothnessLocation = GL20.glGetUniformLocation(programId, "smoothness");
            if (colorLocation != -1) {
               GL20.glUniform4f(colorLocation, r, g, b, a);
            }

            if (sizeLocation != -1) {
               GL20.glUniform2f(sizeLocation, width, height);
            }

            if (radiusLocation != -1) {
               GL20.glUniform4f(radiusLocation, topLeft, topRight, bottomRight, bottomLeft);
            }

            if (smoothnessLocation != -1) {
               GL20.glUniform1f(smoothnessLocation, 1.0F);
            }

            int projectionLocation = GL20.glGetUniformLocation(programId, "projection");
            int modelLocation = GL20.glGetUniformLocation(programId, "model");
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getWindow() != null) {
               float[] projection = createOrthographicMatrix(0.0F, client.getWindow().getScaledWidth(), 0.0F, client.getWindow().getScaledHeight(), -1.0F, 1.0F);
               if (projectionLocation != -1) {
                  GL20.glUniformMatrix4fv(projectionLocation, false, projection);
               }
            }

            float[] model = createModelMatrix(x, y, width, height);
            if (modelLocation != -1) {
               GL20.glUniformMatrix4fv(modelLocation, false, model);
            }

            GL30.glBindVertexArray(vao);
            GL20.glDrawArrays(6, 0, 4);
            GL30.glBindVertexArray(0);
            roundedQuadShader.unbind();
         } catch (Exception var21) {
            System.err.println("Error rendering rounded quad with shader: " + var21.getMessage());
         }
      }
   }

   private static float[] createOrthographicMatrix(float left, float right, float bottom, float top, float near, float far) {
      return new float[]{
         2.0F / (right - left),
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         2.0F / (top - bottom),
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         -2.0F / (far - near),
         0.0F,
         -(right + left) / (right - left),
         -(top + bottom) / (top - bottom),
         -(far + near) / (far - near),
         1.0F
      };
   }

   private static float[] createModelMatrix(float x, float y, float width, float height) {
      return new float[]{width, 0.0F, 0.0F, 0.0F, 0.0F, height, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, x, y, 0.0F, 1.0F};
   }

   public static void cleanup() {
      if (vao != 0) {
         GL30.glDeleteVertexArrays(vao);
         vao = 0;
      }

      if (vbo != 0) {
         GL20.glDeleteBuffers(vbo);
         vbo = 0;
      }

      initialized = false;
   }

   public static boolean isInitialized() {
      return initialized;
   }
}
