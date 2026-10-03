package skid.ykow.utils.meteorrejects;

import skid.ykow.mixin.CountPlacementModifierAccessor;
import skid.ykow.mixin.HeightRangePlacementModifierAccessor;
import skid.ykow.mixin.RarityFilterPlacementModifierAccessor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.Impl;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.ScatteredOreFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer.IndexedFeatures;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;

public class Ore {
   public int generationStep;
   public int featureIndex;
   public IntProvider countProvider = ConstantIntProvider.create(1);
   public HeightProvider heightProvider;
   public HeightContext heightContext;
   public float rarityChance = 1.0F;
   public float discardOnAirChance;
   public int oreSize;
   public Color oreColor;
   public boolean isScatteredOre;

   public static Map<RegistryKey<Biome>, List<Ore>> register() {
      WrapperLookup wrapperLookup = BuiltinRegistries.createWrapperLookup();
      Impl<PlacedFeature> placedFeatureRegistry = wrapperLookup.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE);
      List<RegistryEntry<Biome>> netherBiomes = ((DimensionOptions)((WorldPreset)wrapperLookup.getWrapperOrThrow(RegistryKeys.WORLD_PRESET)
               .getOrThrow(WorldPresets.DEFAULT)
               .value())
            .createDimensionsRegistryHolder()
            .dimensions()
            .get(DimensionOptions.NETHER))
         .chunkGenerator()
         .getBiomeSource()
         .getBiomes()
         .stream()
         .toList();
      List<IndexedFeatures> indexedFeatures = PlacedFeatureIndexer.collectIndexedFeatures(
         netherBiomes, registryEntry -> ((Biome)registryEntry.value()).getGenerationSettings().getFeatures(), true
      );
      Map<PlacedFeature, Ore> ores = new HashMap<>();
      RegistryKey<PlacedFeature> smallDebrisRegistry = OrePlacedFeatures.ORE_DEBRIS_SMALL;
      register(ores, indexedFeatures, placedFeatureRegistry, smallDebrisRegistry, 7, new Color(209, 27, 245));
      RegistryKey<PlacedFeature> largeDebrisRegistry = OrePlacedFeatures.ORE_ANCIENT_DEBRIS_LARGE;
      register(ores, indexedFeatures, placedFeatureRegistry, largeDebrisRegistry, 7, new Color(209, 27, 245));
      Map<RegistryKey<Biome>, List<Ore>> biomeOreMap = new HashMap<>();
      netherBiomes.forEach(
         registryEntry -> {
            biomeOreMap.put((RegistryKey<Biome>)registryEntry.getKey().get(), new ArrayList<>());
            Stream<PlacedFeature> stream = ((Biome)registryEntry.value())
               .getGenerationSettings()
               .getFeatures()
               .stream()
               .flatMap(RegistryEntryList::stream)
               .map(RegistryEntry::value);
            stream.filter(ores::containsKey).forEach(placedFeature -> biomeOreMap.get(registryEntry.getKey().get()).add(ores.get(placedFeature)));
         }
      );
      return biomeOreMap;
   }

   private static void register(
      Map<PlacedFeature, Ore> oreMap,
      List<IndexedFeatures> indexedFeatures,
      Impl<PlacedFeature> oreRegistry,
      RegistryKey<PlacedFeature> oreKey,
      int generationStep,
      Color oreColor
   ) {
      PlacedFeature orePlacement = (PlacedFeature)oreRegistry.getOrThrow(oreKey).value();
      int featureIndex = indexedFeatures.get(generationStep).indexMapping().applyAsInt(orePlacement);
      Ore ore = new Ore(orePlacement, generationStep, featureIndex, oreColor);
      oreMap.put(orePlacement, ore);
   }

   private Ore(PlacedFeature placedFeature, int generationStep, int featureIndex, Color oreColor) {
      this.generationStep = generationStep;
      this.featureIndex = featureIndex;
      this.oreColor = oreColor;
      this.heightContext = new HeightContext(
         null, HeightLimitView.create(MinecraftClient.getInstance().world.getBottomY(), MinecraftClient.getInstance().world.getDimension().logicalHeight())
      );

      for (Object placementModifier : placedFeature.placementModifiers()) {
         if (placementModifier instanceof CountPlacementModifier) {
            this.countProvider = ((CountPlacementModifierAccessor)placementModifier).getCount();
         } else if (placementModifier instanceof HeightRangePlacementModifier) {
            this.heightProvider = ((HeightRangePlacementModifierAccessor)placementModifier).getHeight();
         } else if (placementModifier instanceof RarityFilterPlacementModifier) {
            this.rarityChance = ((RarityFilterPlacementModifierAccessor)placementModifier).getChance();
         }
      }

      FeatureConfig featureConfig = ((ConfiguredFeature)placedFeature.feature().value()).config();
      if (featureConfig instanceof OreFeatureConfig) {
         this.discardOnAirChance = ((OreFeatureConfig)featureConfig).discardOnAirChance;
         this.oreSize = ((OreFeatureConfig)featureConfig).size;
         if (((ConfiguredFeature)placedFeature.feature().value()).feature() instanceof ScatteredOreFeature) {
            this.isScatteredOre = true;
         }
      } else {
         throw new IllegalStateException("config for " + placedFeature + "is not OreFeatureConfig.class");
      }
   }

   private static String getAncientDebrisDetectionKey() {
      return "ANCIENT_DEBRIS_DETECTION_KEY";
   }
}
