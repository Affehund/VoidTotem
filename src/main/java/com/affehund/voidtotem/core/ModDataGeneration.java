package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ModDataGeneration {
    private static final Logger DATAGEN_LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * A sub class for the creation of the language files.
     *
     * @author Affehund
     * @see LanguageProvider
     */
    public static final class LanguageGen extends LanguageProvider {

        public LanguageGen(DataGenerator gen, String locale) {
            super(gen, ModConstants.MOD_ID, locale);
        }

        @Override
        protected void addTranslations() {
            String locale = this.getName().replace("Languages: ", "");
            switch (locale) {
                case "de_de" -> {
                    add("_comment", "Translation (de_de) by Affehund");
                    add(VoidTotem.VOID_TOTEM_ITEM.get(), "Totem der Unsterblichkeit in der Leere");
                    add(ModConstants.TOOLTIP_VOID_TOTEM,
                            "Lege diese Totem in deine Haupt-/Nebenhand, um zu verhindern, dass du stirbst, wenn du in die Leere fällst.");
                    add(ModConstants.ADVANCEMENT_VOID_TOTEM_TITLE, "Post mortem 2");
                    add(ModConstants.ADVANCEMENT_VOID_TOTEM_DESC,
                            "Benutze ein Totem der Unsterblichkeit in der Leere, um dem Tod, wenn du in die Leere fällst, von der Schippe zu springen");
                }
                case "en_us" -> {
                    add("_comment", "Translation (en_us) by Affehund");
                    add(VoidTotem.VOID_TOTEM_ITEM.get(), "Totem of Void Undying");
                    add(ModConstants.TOOLTIP_VOID_TOTEM,
                            "Put this totem in your main-/offhand to prevent dying if you fall in the void.");
                    add(ModConstants.ADVANCEMENT_VOID_TOTEM_TITLE, "Postmortal 2");
                    add(ModConstants.ADVANCEMENT_VOID_TOTEM_DESC,
                            "Use a Totem of Void Undying to cheat death when falling in the void");
                }
            }
        }
    }

    /**
     * A sub class to create the item model for the void totem.
     *
     * @author Affehund
     * @see ItemModelProvider
     */
    public static final class ItemModelGen extends ItemModelProvider {
        private final Set<Item> blacklist = new HashSet<>();

        public ItemModelGen(DataGenerator gen, String modid, ExistingFileHelper existingFileHelper) {
            super(gen, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys()) {
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item != null && ModConstants.MOD_ID.equals(id.getNamespace()) && !this.blacklist.contains(item)) {
                    if (item instanceof BlockItem) {
                        return;
                    } else {
                        this.defaultItem(id, item);
                    }
                }
            }
        }

        /**
         * Used to create a item/generated item for the given resoure location and item.
         *
         * @param id   ResourceLocation
         * @param item Item
         */
        protected void defaultItem(ResourceLocation id, Item item) {
            this.withExistingParent(id.getPath(), "item/generated").texture("layer0",
                    new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
            DATAGEN_LOGGER.debug("Generated item model for: " + item.getRegistryName());
        }
    }

    /**
     * A sub class to create the void totem recipe.
     *
     * @author Affehund
     * @see RecipeProvider
     */
    public static final class RecipeGen extends RecipeProvider {
        public RecipeGen(DataGenerator gen) {
            super(gen);
        }

        @Override
        protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
            /**
             * The void totem is created with a shaped recipe:
             *
             * chorus fruit, eye of ender, chorus fruit,
             *
             * emerald, totem of undying, emerald
             *
             * null, eye of ender, null
             */
            ShapedRecipeBuilder.shaped(VoidTotem.VOID_TOTEM_ITEM.get()).pattern("cec").pattern("dtd").pattern(" e ")
                    .define('c', Items.CHORUS_FRUIT).define('e', Items.ENDER_EYE).define('d', Items.EMERALD)
                    .define('t', Items.TOTEM_OF_UNDYING).unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT))
                    .unlockedBy("has_ender_eye", has(Items.ENDER_EYE)).unlockedBy("has_emerald", has(Items.EMERALD))
                    .unlockedBy("has_totem", has(Items.TOTEM_OF_UNDYING)).save(consumer);
        }

    }

    /**
     * A sub class with that block tags could be generated, only needed for the
     * ItemTagsGen.
     *
     * @author Affehund
     * @see BlockTagsProvider
     * @see ItemTagsGen
     */
    public static final class BlockTagsGen extends BlockTagsProvider {
        public BlockTagsGen(DataGenerator generatorIn, String modId, ExistingFileHelper existingFileHelper) {
            super(generatorIn, modId, existingFileHelper);
        }
    }

    /**
     * A sub class to create a tag to add the void totem and totem of undying to the
     * curios charm slot.
     *
     * @author Affehund
     * @see ItemTagsProvider
     */
    public static final class ItemTagsGen extends ItemTagsProvider {

        public ItemTagsGen(DataGenerator gen, BlockTagsProvider provider, String modID,
                           ExistingFileHelper existingFileHelper) {
            super(gen, provider, modID, existingFileHelper);
        }

        @Override
        protected void addTags() {
            this.tag(CURIOS_CHARM).add(VoidTotem.VOID_TOTEM_ITEM.get()).add(Items.TOTEM_OF_UNDYING);
        }
    }

    public static final Tag.Named<Item> CURIOS_CHARM = modTag(ModConstants.CURIOS_CHARM_SLOT,
            ModConstants.CURIOS_MOD_ID);

    /**
     * @param name  String
     * @param modID String
     * @return an INamedTag for a given string (the tag name) and another string
     * (the mod id).
     */
    private static Tag.Named<Item> modTag(String name, String modID) {
        return ItemTags.bind(modID + ":" + name);
    }

    /**
     * A sub class to create the advancement when you use the totem of void undying.
     *
     * @author Affehund
     * @see AdvancementProvider
     */
    public static class AdvancementGen extends AdvancementProvider {
        private final DataGenerator generator;

        public AdvancementGen(DataGenerator generatorIn) {
            super(generatorIn);
            this.generator = generatorIn;
        }

        private void registerAdvancements(Consumer<Advancement> consumer) {
            Advancement.Builder.advancement()
                    .parent(Advancement.Builder.advancement()
                            .build(new ResourceLocation(ModConstants.ADVANCEMENT_ADVENTURE_TOTEM_PATH)))
                    .display(VoidTotem.VOID_TOTEM_ITEM.get(),
                            new TranslatableComponent(ModConstants.ADVANCEMENT_VOID_TOTEM_TITLE),
                            new TranslatableComponent(ModConstants.ADVANCEMENT_VOID_TOTEM_DESC),
                            (ResourceLocation) null, FrameType.GOAL, true, true, false)
                    .addCriterion("used_totem", UsedTotemTrigger.TriggerInstance.usedTotem(VoidTotem.VOID_TOTEM_ITEM.get()))
                    .save(consumer, ModConstants.ADVANCEMENT_ADVENTURE_VOID_TOTEM_PATH);
        }

        @Override
        public void run(@Nullable HashCache cache) {
            Path outputFolder = this.generator.getOutputFolder();
            Consumer<Advancement> consumer = (advancement) -> {

                Path path = outputFolder.resolve("data/" + advancement.getId().getNamespace() + "/advancements/"
                        + advancement.getId().getPath() + ".json");
                try {
                    DataProvider.save(GSON, cache, advancement.deconstruct().serializeToJson(), path);
                    DATAGEN_LOGGER.debug("Creating advancement {}", advancement.getId());
                } catch (IOException e) {
                    DATAGEN_LOGGER.error("Couldn't create advancement {}", path, e);
                }
            };
            registerAdvancements(consumer);
        }
    }

    /**
     * A sub class to create the end city loot table injection for the void totem.
     *
     * @author Affehund
     */
    public static class LootTableGen extends LootTableProvider {
        private final DataGenerator generator;

        public LootTableGen(DataGenerator dataGeneratorIn) {
            super(dataGeneratorIn);
            this.generator = dataGeneratorIn;
        }

        @Override
        public void run(@Nonnull HashCache cache) {
            Map<ResourceLocation, LootTable> tables = new HashMap<>();

            LootPool.Builder voidtotem_loot_builder = LootPool.lootPool().name("main")
                    .when(LootItemRandomChanceCondition.randomChance(0.33F)).setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(VoidTotem.VOID_TOTEM_ITEM.get()).setWeight(1));
            tables.put(ModConstants.LOCATION_END_CITY_TREASURE_INJECTION,
                    LootTable.lootTable().withPool(voidtotem_loot_builder).build());

            this.writeLootTables(cache, tables);
        }

        private void writeLootTables(HashCache cache, Map<ResourceLocation, LootTable> tables) {
            Path outputFolder = this.generator.getOutputFolder();
            tables.forEach((key, lootTable) -> {
                Path path = outputFolder
                        .resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
                try {
                    DataProvider.save(GSON, cache, LootTables.serialize(lootTable), path);
                    DATAGEN_LOGGER.debug("Creating loot table {}", key.getPath());
                } catch (IOException e) {
                    DATAGEN_LOGGER.error("Couldn't create loot table {}", key.getPath(), e);
                }
            });
        }
    }
}
