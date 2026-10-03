package skid.ykow.module;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.KeyEvent;
import skid.ykow.gui.ClickGUI;
import skid.ykow.module.modules.client.ConfigDebug;
import skid.ykow.module.modules.client.Ykow;
import skid.ykow.module.modules.client.Friends;
import skid.ykow.module.modules.client.SelfDestruct;
import skid.ykow.module.modules.combat.ElytraSwap;
import skid.ykow.module.modules.combat.Hitbox;
import skid.ykow.module.modules.combat.MaceSwap;
import skid.ykow.module.modules.combat.ShieldBreaker;
import skid.ykow.module.modules.combat.StaticHitboxes;
import skid.ykow.module.modules.combat.TriggerBot;
import skid.ykow.module.modules.crystal.AnchorMacro;
import skid.ykow.module.modules.crystal.AutoCrystal;
import skid.ykow.module.modules.crystal.AutoHitCrystal;
import skid.ykow.module.modules.crystal.AutoInventoryTotem;
import skid.ykow.module.modules.crystal.AutoTotem;
import skid.ykow.module.modules.crystal.DoubleAnchor;
import skid.ykow.module.modules.crystal.HoverTotem;
import skid.ykow.module.modules.donut.AntiTrap;
import skid.ykow.module.modules.donut.AuctionSniper;
import skid.ykow.module.modules.donut.AutoSell;
import skid.ykow.module.modules.donut.AutoSpawnerSell;
import skid.ykow.module.modules.donut.BoneDropper;
import skid.ykow.module.modules.donut.NetheriteFinder;
import skid.ykow.module.modules.donut.RTPEndBaseFinder;
import skid.ykow.module.modules.donut.RtpBaseFinder;
import skid.ykow.module.modules.donut.ShulkerDropper;
import skid.ykow.module.modules.donut.SpawnerProtect;
import skid.ykow.module.modules.donut.TunnelBaseFinder;
import skid.ykow.module.modules.misc.AutoEat;
import skid.ykow.module.modules.misc.AutoFirework;
import skid.ykow.module.modules.misc.AutoMine;
import skid.ykow.module.modules.misc.AutoTPA;
import skid.ykow.module.modules.misc.AutoTool;
import skid.ykow.module.modules.misc.CordSnapper;
import skid.ykow.module.modules.misc.ElytraGlide;
import skid.ykow.module.modules.misc.FastPlace;
import skid.ykow.module.modules.misc.Freecam;
import skid.ykow.module.modules.misc.KeyPearl;
import skid.ykow.module.modules.misc.NameProtect;
import skid.ykow.module.modules.misc.PearlBoost;
import skid.ykow.module.modules.misc.QuickMacro;
import skid.ykow.module.modules.misc.TridentBoost;
import skid.ykow.module.modules.misc.WeatherNotifier;
import skid.ykow.module.modules.movement.FakeLag;
import skid.ykow.module.modules.render.Animations;
import skid.ykow.module.modules.render.BlockEsp;
import skid.ykow.module.modules.render.FullBright;
import skid.ykow.module.modules.render.HUD;
import skid.ykow.module.modules.render.KelpESP;
import skid.ykow.module.modules.render.NoRender;
import skid.ykow.module.modules.render.PlayerESP;
import skid.ykow.module.modules.render.ScoreboardModule;
import skid.ykow.module.modules.render.StorageESP;
import skid.ykow.module.modules.render.SwingSpeed;
import skid.ykow.module.modules.render.TargetHUD;
import skid.ykow.module.modules.render.TridentESP;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.utils.EncryptedString;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.ChatScreen;

public final class ModuleManager {
   private final List<Module> modules = new ArrayList<>();

   public ModuleManager() {
      this.keyCodec();
      this.d();
   }

   public void keyCodec() {
      this.add(new ElytraSwap());
      this.add(new Hitbox());
      this.add(new MaceSwap());
      this.add(new ShieldBreaker());
      this.add(new StaticHitboxes());
      this.add(new TriggerBot());
      this.add(new ConfigDebug());
      this.add(new Ykow());
      this.add(new SelfDestruct());
      this.add(new Friends());
      this.add(new Animations());
      this.add(new FullBright());
      this.add(new HUD());
      this.add(new KelpESP());
      this.add(new NoRender());
      this.add(new PlayerESP());
      this.add(new StorageESP());
      this.add(new SwingSpeed());
      this.add(new TargetHUD());
      this.add(new TridentESP());
      this.add(new ScoreboardModule());
      this.add(new BlockEsp());
      this.add(new AutoEat());
      this.add(new AutoFirework());
      this.add(new AutoMine());
      this.add(new AutoTPA());
      this.add(new AutoTool());
      this.add(new CordSnapper());
      this.add(new ElytraGlide());
      this.add(new FastPlace());
      this.add(new Freecam());
      this.add(new KeyPearl());
      this.add(new NameProtect());
      this.add(new WeatherNotifier());
      this.add(new TridentBoost());
      this.add(new PearlBoost());
      this.add(new AnchorMacro());
      this.add(new AutoCrystal());
      this.add(new AutoHitCrystal());
      this.add(new AutoInventoryTotem());
      this.add(new AutoTotem());
      this.add(new DoubleAnchor());
      this.add(new HoverTotem());
      this.add(new AntiTrap());
      this.add(new AuctionSniper());
      this.add(new AutoSell());
      this.add(new AutoSpawnerSell());
      this.add(new BoneDropper());
      this.add(new NetheriteFinder());
      this.add(new RtpBaseFinder());
      this.add(new RTPEndBaseFinder());
      this.add(new ShulkerDropper());
      this.add(new TunnelBaseFinder());
      this.add(new SpawnerProtect());
      this.add(new FakeLag());
      this.add(new QuickMacro());
      Friends a = Friends.getInstance();
      a.setEnabled(true);
   }

   public List<Module> elementCodec() {
      return this.modules.stream().filter(Module::isEnabled).toList();
   }

   public List<Module> c() {
      return this.modules;
   }

   public void d() {
      skid.ykow.Ykow.INSTANCE.getEventBus().register(this);

      for (Module next : this.modules) {
         next.addsetting(
            new BindSetting(EncryptedString.of("Keybind"), next.getKeybind(), true).setDescription(EncryptedString.of("Key to enabled the module"))
         );
      }
   }

   public List<Module> keyCodec(Category category) {
      return this.modules.stream().filter(module -> module.getCategory() == category).toList();
   }

   public Module getModuleByClass(Class<? extends Module> obj) {
      return this.modules.stream().filter(obj::isInstance).findFirst().orElse(null);
   }

   public void add(Module module) {
      skid.ykow.Ykow.INSTANCE.getEventBus().register(module);
      this.modules.add(module);
   }

   @EventListener
   public void keyCodec(KeyEvent keyEvent) {
      if (skid.ykow.Ykow.mc.player != null && !(skid.ykow.Ykow.mc.currentScreen instanceof ChatScreen)) {
         if (!(skid.ykow.Ykow.mc.currentScreen instanceof ClickGUI)) {
            this.modules.forEach(module -> {
               if (module.getKeybind() == keyEvent.key && keyEvent.mode == 1) {
                  if (module instanceof SelfDestruct && SelfDestruct.isActive) {
                     return;
                  }

                  if (module instanceof Ykow && SelfDestruct.hasSelfDestructed) {
                     return;
                  }

                  module.toggle();
               }
            });
         }
      }
   }
}
