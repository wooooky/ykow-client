package skid.ykow.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import javax.imageio.ImageIO;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.BufferUtils;

public final class GlyphPage {
   private int imageSize;
   private int maxHeight = -1;
   private final Font font;
   private final boolean antiAlias;
   private final boolean fractionalMetrics;
   private final HashMap<Character, GlyphPage.Glyph> glyphs = new HashMap<>();
   private BufferedImage img;
   private AbstractTexture texture;

   public GlyphPage(Font font, boolean antiAlias, boolean fractionalMetrics) {
      this.font = font;
      this.antiAlias = antiAlias;
      this.fractionalMetrics = fractionalMetrics;
   }

   public void generate(char[] chars) {
      double width = -1.0;
      double height = -1.0;
      FontRenderContext frc = new FontRenderContext(new AffineTransform(), this.antiAlias, this.fractionalMetrics);

      for (char item : chars) {
         Rectangle2D bounds = this.font.getStringBounds(Character.toString(item), frc);
         if (width < bounds.getWidth()) {
            width = bounds.getWidth();
         }

         if (height < bounds.getHeight()) {
            height = bounds.getHeight();
         }
      }

      double maxWidth = width + 2.0;
      double maxHeight = height + 2.0;
      this.imageSize = (int)Math.ceil(
            Math.max(
                  Math.ceil(Math.sqrt(maxWidth * maxWidth * chars.length) / maxWidth), Math.ceil(Math.sqrt(maxHeight * maxHeight * chars.length) / maxHeight)
               )
               * Math.max(maxWidth, maxHeight)
         )
         + 1;
      this.img = new BufferedImage(this.imageSize, this.imageSize, 2);
      Graphics2D graphics = this.img.createGraphics();
      graphics.setFont(this.font);
      graphics.setColor(new Color(255, 255, 255, 0));
      graphics.fillRect(0, 0, this.imageSize, this.imageSize);
      graphics.setColor(Color.white);
      graphics.setRenderingHint(
         RenderingHints.KEY_FRACTIONALMETRICS, this.fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF
      );
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, this.antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
      graphics.setRenderingHint(
         RenderingHints.KEY_TEXT_ANTIALIASING, this.antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
      );
      FontMetrics metrics = graphics.getFontMetrics();
      int currentHeight = 0;
      int posX = 0;
      int posY = 1;

      for (char c : chars) {
         GlyphPage.Glyph glyph = new GlyphPage.Glyph();
         Rectangle2D boundsx = metrics.getStringBounds(Character.toString(c), graphics);
         glyph.width = boundsx.getBounds().width + 8;
         glyph.height = boundsx.getBounds().height;
         if (posX + glyph.width >= this.imageSize) {
            posX = 0;
            posY += currentHeight;
            currentHeight = 0;
         }

         glyph.x = posX;
         glyph.y = posY;
         if (glyph.height > this.maxHeight) {
            this.maxHeight = glyph.height;
         }

         if (glyph.height > currentHeight) {
            currentHeight = glyph.height;
         }

         graphics.drawString(Character.toString(c), posX + 2, posY + metrics.getAscent());
         posX += glyph.width;
         this.glyphs.put(c, glyph);
      }
   }

   public void setup() {
      try {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         ImageIO.write(this.img, "png", output);
         byte[] byteArray = output.toByteArray();
         ByteBuffer data = BufferUtils.createByteBuffer(byteArray.length).put(byteArray);
         data.flip();
         this.texture = new NativeImageBackedTexture(NativeImage.read(data));
         RenderSystem.setShaderTexture(0, this.texture.getGlId());
         GlStateManager._texParameter(3553, 10240, 9729);
         GlStateManager._texParameter(3553, 10241, 9729);
         RenderSystem.setShaderTexture(0, 0);
      } catch (Throwable var4) {
         var4.printStackTrace(System.err);
      }
   }

   public void bind() {
      if (this.texture != null) {
         RenderSystem.setShaderTexture(0, this.texture.getGlId());
      }
   }

   public void unbind() {
      RenderSystem.setShaderTexture(0, 0);
   }

   public float drawChar(MatrixStack stack, char ch, float x, float y, float r, float b, float g, float alpha) {
      GlyphPage.Glyph glyph = this.glyphs.get(ch);
      if (glyph == null) {
         return 0.0F;
      } else if (this.texture == null) {
         return 0.0F;
      } else {
         float pageX = (float)glyph.x / this.imageSize;
         float pageY = (float)glyph.y / this.imageSize;
         float pageWidth = (float)glyph.width / this.imageSize;
         float pageHeight = (float)glyph.height / this.imageSize;
         float width = glyph.width;
         float height = glyph.height;
         RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
         this.bind();
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         builder.vertex(stack.peek().getPositionMatrix(), x, y + height, 0.0F).color(r, g, b, alpha).texture(pageX, pageY + pageHeight);
         builder.vertex(stack.peek().getPositionMatrix(), x + width, y + height, 0.0F).color(r, g, b, alpha).texture(pageX + pageWidth, pageY + pageHeight);
         builder.vertex(stack.peek().getPositionMatrix(), x + width, y, 0.0F).color(r, g, b, alpha).texture(pageX + pageWidth, pageY);
         builder.vertex(stack.peek().getPositionMatrix(), x, y, 0.0F).color(r, g, b, alpha).texture(pageX, pageY);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         this.unbind();
         return width - 8.0F;
      }
   }

   public float getWidth(char c) {
      GlyphPage.Glyph glyph = this.glyphs.get(c);
      return glyph != null ? glyph.width : 0.0F;
   }

   public boolean isAntiAlias() {
      return this.antiAlias;
   }

   public boolean isFractionalMetrics() {
      return this.fractionalMetrics;
   }

   public int getMaxHeight() {
      return this.maxHeight;
   }

   public static final class Glyph {
      private int x;
      private int y;
      private int width;
      private int height;

      Glyph(int x, int y, int width, int height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
      }

      Glyph() {
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }
   }
}
