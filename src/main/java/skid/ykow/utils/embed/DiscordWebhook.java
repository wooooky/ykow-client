package skid.ykow.utils.embed;

import java.awt.Color;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;

public class DiscordWebhook {
   private final String webhookUrl;
   private String content;
   private String username;
   private String avatarUrl;
   private boolean tts;
   private final List<DiscordWebhook.EmbedObject> embeds = new ArrayList<>();

   public DiscordWebhook(String webhookUrl) {
      this.webhookUrl = webhookUrl;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public void setAvatarUrl(String avatarUrl) {
      this.avatarUrl = avatarUrl;
   }

   public void setTts(boolean tts) {
      this.tts = tts;
   }

   public void addEmbed(DiscordWebhook.EmbedObject embed) {
      this.embeds.add(embed);
   }

   public void execute() throws Throwable {
      if (this.content == null && this.embeds.isEmpty()) {
         throw new IllegalArgumentException("Set content or add at least one EmbedObject");
      } else {
         DiscordWebhook.JSONObject jsonSerializer = new DiscordWebhook.JSONObject();
         jsonSerializer.put("content", this.content);
         jsonSerializer.put("username", this.username);
         jsonSerializer.put("avatar_url", this.avatarUrl);
         jsonSerializer.put("tts", this.tts);
         if (!this.embeds.isEmpty()) {
            ArrayList<DiscordWebhook.JSONObject> embedList = new ArrayList<>();

            for (DiscordWebhook.EmbedObject embed : this.embeds) {
               DiscordWebhook.JSONObject jsonEmbed = new DiscordWebhook.JSONObject();
               jsonEmbed.put("title", embed.title);
               jsonEmbed.put("description", embed.description);
               jsonEmbed.put("url", embed.url);
               if (embed.color != null) {
                  Color color = embed.color;
                  jsonEmbed.put("color", ((color.getRed() << 8) + color.getGreen() << 8) + color.getBlue());
               }

               DiscordWebhook.Footer footer = embed.footer;
               DiscordWebhook.Image image = embed.image;
               DiscordWebhook.Thumbnail thumbnail = embed.thumbnail;
               DiscordWebhook.Author author = embed.author;
               List<DiscordWebhook.Field> fields = embed.fields;
               if (footer != null) {
                  DiscordWebhook.JSONObject jsonFooter = new DiscordWebhook.JSONObject();
                  jsonFooter.put("text", footer.text);
                  jsonFooter.put("icon_url", footer.iconUrl);
                  jsonEmbed.put("footer", jsonFooter);
               }

               if (image != null) {
                  DiscordWebhook.JSONObject jsonImage = new DiscordWebhook.JSONObject();
                  jsonImage.put("url", image.url);
                  jsonEmbed.put("image", jsonImage);
               }

               if (thumbnail != null) {
                  DiscordWebhook.JSONObject jsonThumbnail = new DiscordWebhook.JSONObject();
                  jsonThumbnail.put("url", thumbnail.url);
                  jsonEmbed.put("thumbnail", jsonThumbnail);
               }

               if (author != null) {
                  DiscordWebhook.JSONObject jsonAuthor = new DiscordWebhook.JSONObject();
                  jsonAuthor.put("name", author.name);
                  jsonAuthor.put("url", author.url);
                  jsonAuthor.put("icon_url", author.iconUrl);
                  jsonEmbed.put("author", jsonAuthor);
               }

               ArrayList<DiscordWebhook.JSONObject> jsonFields = new ArrayList<>();

               for (DiscordWebhook.Field field : fields) {
                  DiscordWebhook.JSONObject jsonField = new DiscordWebhook.JSONObject();
                  jsonField.put("name", field.name());
                  jsonField.put("value", field.value());
                  jsonField.put("inline", field.inline());
                  jsonFields.add(jsonField);
               }

               jsonEmbed.put("fields", jsonFields.toArray());
               embedList.add(jsonEmbed);
            }

            jsonSerializer.put("embeds", embedList.toArray());
         }

         URLConnection connection = new URL(this.webhookUrl).openConnection();
         connection.addRequestProperty("Content-Type", "application/json");
         connection.addRequestProperty("User-Agent", "YourLocalLinuxUser");
         connection.setDoOutput(true);
         ((HttpsURLConnection)connection).setRequestMethod("POST");
         OutputStream outputStream = connection.getOutputStream();
         outputStream.write(jsonSerializer.toString().getBytes(StandardCharsets.UTF_8));
         outputStream.flush();
         outputStream.close();
         connection.getInputStream().close();
         ((HttpsURLConnection)connection).disconnect();
      }
   }

   record Author(String name, String url, String iconUrl) {
   }

   public static class EmbedObject {
      public String title;
      public String description;
      public String url;
      public Color color;
      public DiscordWebhook.Footer footer;
      public DiscordWebhook.Thumbnail thumbnail;
      public DiscordWebhook.Image image;
      public DiscordWebhook.Author author;
      public final List<DiscordWebhook.Field> fields = new ArrayList<>();

      public DiscordWebhook.EmbedObject setDescription(String description) {
         this.description = description;
         return this;
      }

      public DiscordWebhook.EmbedObject setColor(Color color) {
         this.color = color;
         return this;
      }

      public DiscordWebhook.EmbedObject setTitle(String title) {
         this.title = title;
         return this;
      }

      public DiscordWebhook.EmbedObject setUrl(String url) {
         this.url = url;
         return this;
      }

      public DiscordWebhook.EmbedObject setFooter(String text, String iconUrl) {
         this.footer = new DiscordWebhook.Footer(text, iconUrl);
         return this;
      }

      public DiscordWebhook.EmbedObject setImage(DiscordWebhook.Image image) {
         this.image = image;
         return this;
      }

      public DiscordWebhook.EmbedObject setThumbnail(String url) {
         this.thumbnail = new DiscordWebhook.Thumbnail(url);
         return this;
      }

      public DiscordWebhook.EmbedObject setAuthor(DiscordWebhook.Author author) {
         this.author = author;
         return this;
      }

      public DiscordWebhook.EmbedObject addField(String name, String value, boolean inline) {
         this.fields.add(new DiscordWebhook.Field(name, value, inline));
         return this;
      }
   }

   record Field(String name, String value, boolean inline) {
   }

   record Footer(String text, String iconUrl) {
   }

   record Image(String url) {
   }

   static class JSONObject {
      private final HashMap<String, Object> data = new HashMap<>();

      void put(String key, Object value) {
         if (value != null) {
            this.data.put(key, value);
         }
      }

      @Override
      public String toString() {
         StringBuilder stringBuilder = new StringBuilder();
         Set<Entry<String, Object>> entrySet = this.data.entrySet();
         stringBuilder.append("{");
         int count = 0;

         for (Entry<String, Object> entry : entrySet) {
            Object value = entry.getValue();
            stringBuilder.append(this.escapeString(entry.getKey())).append(":");
            if (value instanceof String) {
               stringBuilder.append(this.escapeString(String.valueOf(value)));
            } else if (value instanceof Integer) {
               stringBuilder.append(Integer.valueOf(String.valueOf(value)));
            } else if (value instanceof Boolean) {
               stringBuilder.append(value);
            } else if (value instanceof DiscordWebhook.JSONObject) {
               stringBuilder.append(value);
            } else if (value.getClass().isArray()) {
               stringBuilder.append("[");
               int length = Array.getLength(value);

               for (int i = 0; i < length; i++) {
                  StringBuilder append = stringBuilder.append(Array.get(value, i).toString());
                  String separator;
                  if (i != length - 1) {
                     separator = ",";
                  } else {
                     separator = "";
                  }

                  append.append(separator);
               }

               stringBuilder.append("]");
            }

            count++;
            stringBuilder.append(count == entrySet.size() ? "}" : ",");
         }

         return stringBuilder.toString();
      }

      private String escapeString(String str) {
         return "\"" + str;
      }
   }

   record Thumbnail(String url) {
   }
}
