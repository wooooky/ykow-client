package skid.ykow.module;

import skid.ykow.utils.EncryptedString;

public enum Category {
   CRYSTAL(EncryptedString.of("Crystal")),
   COMBAT(EncryptedString.of("Combat")),
   MISC(EncryptedString.of("Misc")),
   DONUT(EncryptedString.of("Donut")),
   RENDER(EncryptedString.of("Render")),
   CLIENT(EncryptedString.of("Client")),
   MOVEMENT(EncryptedString.of("Movement"));

   public final CharSequence name;

   private Category(final CharSequence name) {
      this.name = name;
   }
}
