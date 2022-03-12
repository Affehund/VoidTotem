package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.ChestLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModDataGeneration {
    private static final Logger DATA_GEN_LOGGER = LogManager.getLogger();

    /*
    client-side data generators
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

    public static final class ItemModelGen extends ItemModelProvider {

        public ItemModelGen(DataGenerator gen, String modid, ExistingFileHelper existingFileHelper) {
            super(gen, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys()) {
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item != null && ModConstants.MOD_ID.equals(id.getNamespace())) {
                    if (item instanceof BlockItem) {
                        return;
                    } else {
                        this.defaultItem(id, item);
                    }
                }
            }
        }

        private void defaultItem(ResourceLocation id, Item item) {
            this.withExistingParent(id.getPath(), "item/generated").texture("layer0",
                    new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
            DATA_GEN_LOGGER.debug("Generated item model for: " + item.getRegistryName());
        }
    }


    /*
     server-side data generators
    */

    public static class AdvancementGen extends AdvancementProvider {

        public AdvancementGen(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, existingFileHelper);
        }

        @Override
        protected void registerAdvancements(@NotNull Consumer<Advancement> consumer, @NotNull ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                    .parent(Advancement.Builder.advancement()
                            .build(new ResourceLocation(ModConstants.ADVANCEMENT_ADVENTURE_TOTEM_PATH)))
                    .display(VoidTotem.VOID_TOTEM_ITEM.get(),
                            new TranslatableComponent(ModConstants.ADVANCEMENT_VOID_TOTEM_TITLE),
                            new TranslatableComponent(ModConstants.ADVANCEMENT_VOID_TOTEM_DESC),
                            null, FrameType.GOAL, true, true, false)
                    .addCriterion("used_void_totem", UsedTotemTrigger.TriggerInstance.usedTotem(VoidTotem.VOID_TOTEM_ITEM.get()))
                    .save(consumer, new ResourceLocation(ModConstants.MOD_ID, ModConstants.ADVANCEMENT_ADVENTURE_VOID_TOTEM_PATH), existingFileHelper);
        }
    }

    public static final class BlockTagsGen extends BlockTagsProvider {
        public BlockTagsGen(DataGenerator generatorIn, String modId, ExistingFileHelper existingFileHelper) {
            super(generatorIn, modId, existingFileHelper);
        }
    }

    public static final class ItemTagsGen extends ItemTagsProvider {

        public ItemTagsGen(DataGenerator gen, BlockTagsProvider provider, String modID, ExistingFileHelper existingFileHelper) {
            super(gen, provider, modID, existingFileHelper);
        }

        @Override
        protected void addTags() {
            this.tag(VoidTotemTags.ADDITIONAL_TOTEMS);
            this.tag(VoidTotemTags.CURIOS_CHARM).addTag(VoidTotemTags.ADDITIONAL_TOTEMS).add(VoidTotem.VOID_TOTEM_ITEM.get());
        }
    }

    public static class LootTableGen extends LootTableProvider {
        public LootTableGen(DataGenerator dataGeneratorIn) {
            super(dataGeneratorIn);
        }

        @Override
        protected @NotNull List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
            return ImmutableList.of(Pair.of(Chests::new, LootContextParamSets.CHEST));
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, @NotNull ValidationContext validationContext) {
            map.forEach((resourceLocation, lootTable) -> LootTables.validate(validationContext, resourceLocation, lootTable));
        }

        public static class Chests extends ChestLoot {

            @Override
            public void accept(@NotNull BiConsumer<ResourceLocation, LootTable.Builder> builder) {
                var list = ImmutableList.of(BuiltInLootTables.END_CITY_TREASURE);
                var voidTotemLootPool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.33F))
                        .setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(VoidTotem.VOID_TOTEM_ITEM.get()).setWeight(1));

                createInjectPools(builder, list, LootTable.lootTable().withPool(voidTotemLootPool));
            }

            private void createInjectPools(BiConsumer<ResourceLocation, LootTable.Builder> consumer, List<ResourceLocation> list, LootTable.Builder builder) {
                list.forEach(reLoc -> consumer.accept(new ResourceLocation(ModConstants.MOD_ID, "inject/" + reLoc.getPath()), builder));
            }
        }
    }

    public static final class RecipeGen extends RecipeProvider {
        public RecipeGen(DataGenerator gen) {
            super(gen);
        }

        @Override
        protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
            ShapedRecipeBuilder.shaped(VoidTotem.VOID_TOTEM_ITEM.get()).pattern("cec").pattern("dtd").pattern(" e ")
                    .define('c', Items.CHORUS_FRUIT).define('e', Items.ENDER_EYE).define('d', Items.EMERALD)
                    .define('t', Items.TOTEM_OF_UNDYING).unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT))
                    .unlockedBy("has_ender_eye", has(Items.ENDER_EYE)).unlockedBy("has_emerald", has(Items.EMERALD))
                    .unlockedBy("has_totem", has(Items.TOTEM_OF_UNDYING)).save(consumer);
        }
    }
}
