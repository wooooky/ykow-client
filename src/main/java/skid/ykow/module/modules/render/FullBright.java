package skid.ykow.module.modules.render;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public final class FullBright extends Module {
   private final ModeSetting<FullBright.FullBrightMode> mode = new ModeSetting<>(
         EncryptedString.of("Mode"), FullBright.FullBrightMode.GAMMA, FullBright.FullBrightMode.class
      )
      .setDescription(EncryptedString.of("The way that will be used to change the game's brightness"));
   private double originalGamma = 1.0;

   public FullBright() {
      super(EncryptedString.of("FullBright"), EncryptedString.of("Gives you the ability to clearly see even when in the dark"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.mode});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (this.mc.player != null) {
         if (this.mode.getValue() == FullBright.FullBrightMode.GAMMA) {
            try {
               Field gammaField = this.mc.options.getClass().getDeclaredField("gamma");
               gammaField.setAccessible(true);
               Object gammaOption = gammaField.get(this.mc.options);
               if (gammaOption != null) {
                  Method getValueMethod = gammaOption.getClass().getMethod("getValue");
                  this.originalGamma = (Double)getValueMethod.invoke(gammaOption);
               }
            } catch (Exception var4) {
               this.originalGamma = 1.0;
            }
         }

         if (this.mode.getValue() == FullBright.FullBrightMode.POTION && !this.mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            this.mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1));
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (this.mc.player != null) {
         if (this.mode.getValue() == FullBright.FullBrightMode.GAMMA) {
            try {
               Field gammaField = this.mc.options.getClass().getDeclaredField("gamma");
               gammaField.setAccessible(true);
               Object gammaOption = gammaField.get(this.mc.options);
               if (gammaOption != null) {
                  Method setValueMethod = gammaOption.getClass().getMethod("setValue", double.class);
                  setValueMethod.invoke(gammaOption, this.originalGamma);
               }
            } catch (Exception var4) {
            }
         }

         if (this.mode.getValue() == FullBright.FullBrightMode.POTION && this.mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            this.mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
         }
      }
   }

   @EventListener
   public void onTick(TickEvent event) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.IllegalStateException: Could not find destination nodes for stat id {Block}:42 from source 60_tail
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.setEdges(FlattenStatementsHelper.java:563)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.buildDirectGraph(FlattenStatementsHelper.java:50)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.<init>(VarDefinitionHelper.java:151)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.<init>(VarDefinitionHelper.java:52)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarDefinitions(VarProcessor.java:52)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:434)
      //
      // Bytecode:
      // 000: aload 0
      // 001: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 004: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 007: ifnonnull 00b
      // 00a: return
      // 00b: aload 0
      // 00c: getfield dev/krispyy/module/modules/render/FullBright.mode Ldev/krispyy/module/setting/ModeSetting;
      // 00f: invokevirtual dev/krispyy/module/setting/ModeSetting.getValue ()Ljava/lang/Enum;
      // 012: getstatic dev/krispyy/module/modules/render/FullBright$FullBrightMode.POTION Ldev/krispyy/module/modules/render/FullBright$FullBrightMode;
      // 015: if_acmpne 03f
      // 018: aload 0
      // 019: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 01c: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 01f: getstatic net/minecraft/entity/effect/StatusEffects.NIGHT_VISION Lnet/minecraft/registry/entry/RegistryEntry;
      // 022: invokevirtual net/minecraft/client/network/ClientPlayerEntity.hasStatusEffect (Lnet/minecraft/registry/entry/RegistryEntry;)Z
      // 025: ifne 03e
      // 028: aload 0
      // 029: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 02c: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 02f: new net/minecraft/entity/effect/StatusEffectInstance
      // 032: dup
      // 033: getstatic net/minecraft/entity/effect/StatusEffects.NIGHT_VISION Lnet/minecraft/registry/entry/RegistryEntry;
      // 036: bipush -1
      // 037: invokespecial net/minecraft/entity/effect/StatusEffectInstance.<init> (Lnet/minecraft/registry/entry/RegistryEntry;I)V
      // 03a: invokevirtual net/minecraft/client/network/ClientPlayerEntity.addStatusEffect (Lnet/minecraft/entity/effect/StatusEffectInstance;)Z
      // 03d: pop
      // 03e: return
      // 03f: aload 0
      // 040: getfield dev/krispyy/module/modules/render/FullBright.mode Ldev/krispyy/module/setting/ModeSetting;
      // 043: invokevirtual dev/krispyy/module/setting/ModeSetting.getValue ()Ljava/lang/Enum;
      // 046: getstatic dev/krispyy/module/modules/render/FullBright$FullBrightMode.GAMMA Ldev/krispyy/module/modules/render/FullBright$FullBrightMode;
      // 049: if_acmpne 260
      // 04c: aload 0
      // 04d: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 050: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 053: ifnonnull 057
      // 056: return
      // 057: aload 0
      // 058: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 05b: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 05e: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 061: ldc "setGamma"
      // 063: bipush 1
      // 064: anewarray 96
      // 067: dup
      // 068: bipush 0
      // 069: getstatic java/lang/Double.TYPE Ljava/lang/Class;
      // 06c: aastore
      // 06d: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 070: astore 2
      // 071: aload 2
      // 072: aload 0
      // 073: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 076: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 079: bipush 1
      // 07a: anewarray 88
      // 07d: dup
      // 07e: bipush 0
      // 07f: ldc2_w 100.0
      // 082: invokestatic java/lang/Double.valueOf (D)Ljava/lang/Double;
      // 085: aastore
      // 086: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 089: pop
      // 08a: return
      // 08b: astore 2
      // 08c: aload 0
      // 08d: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 090: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 093: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 096: ldc "getGamma"
      // 098: bipush 0
      // 099: anewarray 96
      // 09c: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 09f: astore 2
      // 0a0: aload 2
      // 0a1: aload 0
      // 0a2: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 0a5: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 0a8: bipush 0
      // 0a9: anewarray 88
      // 0ac: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 0af: astore 3
      // 0b0: aload 3
      // 0b1: ifnull 0de
      // 0b4: aload 3
      // 0b5: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 0b8: ldc "setValue"
      // 0ba: bipush 1
      // 0bb: anewarray 96
      // 0be: dup
      // 0bf: bipush 0
      // 0c0: getstatic java/lang/Double.TYPE Ljava/lang/Class;
      // 0c3: aastore
      // 0c4: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 0c7: astore 4
      // 0c9: aload 4
      // 0cb: aload 3
      // 0cc: bipush 1
      // 0cd: anewarray 88
      // 0d0: dup
      // 0d1: bipush 0
      // 0d2: ldc2_w 100.0
      // 0d5: invokestatic java/lang/Double.valueOf (D)Ljava/lang/Double;
      // 0d8: aastore
      // 0d9: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 0dc: pop
      // 0dd: return
      // 0de: goto 0e2
      // 0e1: astore 2
      // 0e2: aload 0
      // 0e3: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 0e6: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 0e9: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 0ec: ldc "gamma"
      // 0ee: invokevirtual java/lang/Class.getDeclaredField (Ljava/lang/String;)Ljava/lang/reflect/Field;
      // 0f1: astore 2
      // 0f2: aload 2
      // 0f3: bipush 1
      // 0f4: invokevirtual java/lang/reflect/Field.setAccessible (Z)V
      // 0f7: aload 2
      // 0f8: aload 0
      // 0f9: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 0fc: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 0ff: invokevirtual java/lang/reflect/Field.get (Ljava/lang/Object;)Ljava/lang/Object;
      // 102: astore 3
      // 103: aload 3
      // 104: ifnull 131
      // 107: aload 3
      // 108: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 10b: ldc "setValue"
      // 10d: bipush 1
      // 10e: anewarray 96
      // 111: dup
      // 112: bipush 0
      // 113: getstatic java/lang/Double.TYPE Ljava/lang/Class;
      // 116: aastore
      // 117: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 11a: astore 4
      // 11c: aload 4
      // 11e: aload 3
      // 11f: bipush 1
      // 120: anewarray 88
      // 123: dup
      // 124: bipush 0
      // 125: ldc2_w 100.0
      // 128: invokestatic java/lang/Double.valueOf (D)Ljava/lang/Double;
      // 12b: aastore
      // 12c: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 12f: pop
      // 130: return
      // 131: goto 135
      // 134: astore 2
      // 135: goto 260
      // 138: astore 2
      // 139: aload 0
      // 13a: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 13d: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 140: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 143: ldc "brightness"
      // 145: invokevirtual java/lang/Class.getDeclaredField (Ljava/lang/String;)Ljava/lang/reflect/Field;
      // 148: astore 3
      // 149: aload 3
      // 14a: bipush 1
      // 14b: invokevirtual java/lang/reflect/Field.setAccessible (Z)V
      // 14e: aload 3
      // 14f: aload 0
      // 150: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 153: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 156: invokevirtual java/lang/reflect/Field.get (Ljava/lang/Object;)Ljava/lang/Object;
      // 159: astore 4
      // 15b: aload 4
      // 15d: ifnull 189
      // 160: aload 4
      // 162: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 165: ldc "setValue"
      // 167: bipush 1
      // 168: anewarray 96
      // 16b: dup
      // 16c: bipush 0
      // 16d: getstatic java/lang/Double.TYPE Ljava/lang/Class;
      // 170: aastore
      // 171: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 174: astore 5
      // 176: aload 5
      // 178: aload 4
      // 17a: bipush 1
      // 17b: anewarray 88
      // 17e: dup
      // 17f: bipush 0
      // 180: dconst_1
      // 181: invokestatic java/lang/Double.valueOf (D)Ljava/lang/Double;
      // 184: aastore
      // 185: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 188: pop
      // 189: goto 260
      // 18c: astore 3
      // 18d: aload 0
      // 18e: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 191: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 194: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 197: invokevirtual java/lang/Class.getDeclaredFields ()[Ljava/lang/reflect/Field;
      // 19a: astore 4
      // 19c: aload 4
      // 19e: astore 5
      // 1a0: aload 5
      // 1a2: arraylength
      // 1a3: istore 6
      // 1a5: bipush 0
      // 1a6: istore 7
      // 1a8: iload 7
      // 1aa: iload 6
      // 1ac: if_icmpge 235
      // 1af: aload 5
      // 1b1: iload 7
      // 1b3: aaload
      // 1b4: astore 8
      // 1b6: aload 8
      // 1b8: invokevirtual java/lang/reflect/Field.getName ()Ljava/lang/String;
      // 1bb: invokevirtual java/lang/String.toLowerCase ()Ljava/lang/String;
      // 1be: ldc "bright"
      // 1c0: invokevirtual java/lang/String.contains (Ljava/lang/CharSequence;)Z
      // 1c3: ifne 1e6
      // 1c6: aload 8
      // 1c8: invokevirtual java/lang/reflect/Field.getName ()Ljava/lang/String;
      // 1cb: invokevirtual java/lang/String.toLowerCase ()Ljava/lang/String;
      // 1ce: ldc "gamma"
      // 1d0: invokevirtual java/lang/String.contains (Ljava/lang/CharSequence;)Z
      // 1d3: ifne 1e6
      // 1d6: aload 8
      // 1d8: invokevirtual java/lang/reflect/Field.getName ()Ljava/lang/String;
      // 1db: invokevirtual java/lang/String.toLowerCase ()Ljava/lang/String;
      // 1de: ldc "light"
      // 1e0: invokevirtual java/lang/String.contains (Ljava/lang/CharSequence;)Z
      // 1e3: ifeq 22f
      // 1e6: aload 8
      // 1e8: bipush 1
      // 1e9: invokevirtual java/lang/reflect/Field.setAccessible (Z)V
      // 1ec: aload 8
      // 1ee: aload 0
      // 1ef: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 1f2: getfield net/minecraft/client/MinecraftClient.options Lnet/minecraft/client/option/GameOptions;
      // 1f5: invokevirtual java/lang/reflect/Field.get (Ljava/lang/Object;)Ljava/lang/Object;
      // 1f8: astore 9
      // 1fa: aload 9
      // 1fc: ifnull 22f
      // 1ff: aload 9
      // 201: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 204: ldc "setValue"
      // 206: bipush 1
      // 207: anewarray 96
      // 20a: dup
      // 20b: bipush 0
      // 20c: getstatic java/lang/Double.TYPE Ljava/lang/Class;
      // 20f: aastore
      // 210: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 213: astore 10
      // 215: aload 10
      // 217: aload 9
      // 219: bipush 1
      // 21a: anewarray 88
      // 21d: dup
      // 21e: bipush 0
      // 21f: ldc2_w 100.0
      // 222: invokestatic java/lang/Double.valueOf (D)Ljava/lang/Double;
      // 225: aastore
      // 226: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 229: pop
      // 22a: goto 235
      // 22d: astore 10
      // 22f: iinc 7 1
      // 232: goto 1a8
      // 235: goto 260
      // 238: astore 4
      // 23a: aload 0
      // 23b: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 23e: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 241: getstatic net/minecraft/entity/effect/StatusEffects.NIGHT_VISION Lnet/minecraft/registry/entry/RegistryEntry;
      // 244: invokevirtual net/minecraft/client/network/ClientPlayerEntity.hasStatusEffect (Lnet/minecraft/registry/entry/RegistryEntry;)Z
      // 247: ifne 260
      // 24a: aload 0
      // 24b: getfield dev/krispyy/module/modules/render/FullBright.mc Lnet/minecraft/client/MinecraftClient;
      // 24e: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 251: new net/minecraft/entity/effect/StatusEffectInstance
      // 254: dup
      // 255: getstatic net/minecraft/entity/effect/StatusEffects.NIGHT_VISION Lnet/minecraft/registry/entry/RegistryEntry;
      // 258: bipush -1
      // 259: invokespecial net/minecraft/entity/effect/StatusEffectInstance.<init> (Lnet/minecraft/registry/entry/RegistryEntry;I)V
      // 25c: invokevirtual net/minecraft/client/network/ClientPlayerEntity.addStatusEffect (Lnet/minecraft/entity/effect/StatusEffectInstance;)Z
      // 25f: pop
      // 260: return
   }

   public static enum FullBrightMode {
      GAMMA,
      POTION;
   }
}
