package skid.ykow.utils;

import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class EnhancedShaderRenderer {
   private static int shaderProgram;
   private static int VAO;
   private static int VBO;
   private static boolean initialized = false;
   private static int u_resolution;
   private static int u_position;
   private static int u_size;
   private static int u_color;
   private static int u_cornerRadii;
   private static int u_borderWidth;
   private static int u_borderColor;

   public static void init() {
      if (!initialized) {
         String vertexShaderSource = "#version 330 core\nlayout (location = 0) in vec2 aPos;\nout vec2 fragCoord;\nuniform vec2 u_resolution;\nvoid main() {\n    fragCoord = (aPos + 1.0) * 0.5 * u_resolution;\n    gl_Position = vec4(aPos, 0.0, 1.0);\n}";
         String fragmentShaderSource = "#version 330 core\nuniform vec2 u_resolution;\nuniform vec2 u_position;\nuniform vec2 u_size;\nuniform vec4 u_color;\nuniform vec4 u_cornerRadii;\nuniform float u_borderWidth;\nuniform vec4 u_borderColor;\nin vec2 fragCoord;\nout vec4 fragColor;\n\nfloat roundedBoxSDF(vec2 centerPos, vec2 size, vec4 radius) {\n    radius.xy = (centerPos.x > 0.0) ? radius.xy : radius.zw;\n    radius.x = (centerPos.y > 0.0) ? radius.x : radius.y;\n    vec2 q = abs(centerPos) - size + radius.x;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;\n}\n\nvoid main() {\n    vec2 halfSize = u_size * 0.5;\n    vec2 centerPos = fragCoord - u_position - halfSize;\n    float distance = roundedBoxSDF(centerPos, halfSize, u_cornerRadii);\n    float alpha = 1.0 - smoothstep(-1.0, 1.0, distance);\n    \n    if (u_borderWidth > 0.0) {\n        float borderDistance = abs(distance) - u_borderWidth * 0.5;\n        float borderAlpha = 1.0 - smoothstep(-1.0, 1.0, borderDistance);\n        vec4 finalColor = mix(u_color, u_borderColor, step(0.0, distance - u_borderWidth * 0.5));\n        fragColor = vec4(finalColor.rgb, finalColor.a * alpha * borderAlpha);\n    } else {\n        fragColor = vec4(u_color.rgb, u_color.a * alpha);\n    }\n}";

         try {
            shaderProgram = createShaderProgram(vertexShaderSource, fragmentShaderSource);
            u_resolution = GL20.glGetUniformLocation(shaderProgram, "u_resolution");
            u_position = GL20.glGetUniformLocation(shaderProgram, "u_position");
            u_size = GL20.glGetUniformLocation(shaderProgram, "u_size");
            u_color = GL20.glGetUniformLocation(shaderProgram, "u_color");
            u_cornerRadii = GL20.glGetUniformLocation(shaderProgram, "u_cornerRadii");
            u_borderWidth = GL20.glGetUniformLocation(shaderProgram, "u_borderWidth");
            u_borderColor = GL20.glGetUniformLocation(shaderProgram, "u_borderColor");
            setupQuad();
            initialized = true;
         } catch (Exception var3) {
            System.err.println("Failed to initialize EnhancedShaderRenderer: " + var3.getMessage());
            var3.printStackTrace();
         }
      }
   }

   private static void setupQuad() {
      float[] vertices = new float[]{-1.0F, -1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F};
      VAO = GL30.glGenVertexArrays();
      VBO = GL15.glGenBuffers();
      GL30.glBindVertexArray(VAO);
      GL15.glBindBuffer(34962, VBO);
      GL15.glBufferData(34962, vertices, 35044);
      GL20.glVertexAttribPointer(0, 2, 5126, false, 8, 0L);
      GL20.glEnableVertexAttribArray(0);
      GL15.glBindBuffer(34962, 0);
      GL30.glBindVertexArray(0);
   }

   public static void renderRoundedRect(MatrixStack matrices, Color color, float x, float y, float width, float height, float radius) {
      renderRoundedRect(matrices, color, x, y, width, height, radius, radius, radius, radius, 0.0F, null);
   }

   public static void renderRoundedRect(
      MatrixStack matrices,
      Color color,
      float x,
      float y,
      float width,
      float height,
      float topLeft,
      float topRight,
      float bottomLeft,
      float bottomRight,
      float borderWidth,
      Color borderColor
   ) {
      if (!initialized) {
         init();
      }

      if (shaderProgram != 0) {
         try {
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL20.glUseProgram(shaderProgram);
            float screenWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
            float screenHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();
            GL20.glUniform2f(u_resolution, screenWidth, screenHeight);
            GL20.glUniform2f(u_position, x, screenHeight - y - height);
            GL20.glUniform2f(u_size, width, height);
            GL20.glUniform4f(u_color, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            GL20.glUniform4f(u_cornerRadii, topLeft, topRight, bottomLeft, bottomRight);
            if (borderWidth > 0.0F && borderColor != null) {
               GL20.glUniform1f(u_borderWidth, borderWidth);
               GL20.glUniform4f(
                  u_borderColor,
                  borderColor.getRed() / 255.0F,
                  borderColor.getGreen() / 255.0F,
                  borderColor.getBlue() / 255.0F,
                  borderColor.getAlpha() / 255.0F
               );
            } else {
               GL20.glUniform1f(u_borderWidth, 0.0F);
               GL20.glUniform4f(u_borderColor, 0.0F, 0.0F, 0.0F, 0.0F);
            }

            GL11.glScissor((int)x, (int)(screenHeight - y - height), (int)width, (int)height);
            GL11.glEnable(3089);
            GL30.glBindVertexArray(VAO);
            GL11.glDrawArrays(4, 0, 6);
            GL30.glBindVertexArray(0);
            GL11.glDisable(3089);
            GL20.glUseProgram(0);
            GL11.glDisable(3042);
         } catch (Exception var14) {
            System.err.println("Error rendering rounded rect: " + var14.getMessage());
         }
      }
   }

   private static int createShaderProgram(String vertexSource, String fragmentSource) {
      int vertexShader = compileShader(35633, vertexSource);
      int fragmentShader = compileShader(35632, fragmentSource);
      int program = GL20.glCreateProgram();
      GL20.glAttachShader(program, vertexShader);
      GL20.glAttachShader(program, fragmentShader);
      GL20.glLinkProgram(program);
      if (GL20.glGetProgrami(program, 35714) == 0) {
         throw new RuntimeException("Failed to link shader program: " + GL20.glGetProgramInfoLog(program));
      } else {
         GL20.glDeleteShader(vertexShader);
         GL20.glDeleteShader(fragmentShader);
         return program;
      }
   }

   private static int compileShader(int type, String source) {
      int shader = GL20.glCreateShader(type);
      GL20.glShaderSource(shader, source);
      GL20.glCompileShader(shader);
      if (GL20.glGetShaderi(shader, 35713) == 0) {
         throw new RuntimeException("Failed to compile shader: " + GL20.glGetShaderInfoLog(shader));
      } else {
         return shader;
      }
   }

   public static void cleanup() {
      if (initialized) {
         GL20.glDeleteProgram(shaderProgram);
         GL30.glDeleteVertexArrays(VAO);
         GL15.glDeleteBuffers(VBO);
         initialized = false;
      }
   }
}
