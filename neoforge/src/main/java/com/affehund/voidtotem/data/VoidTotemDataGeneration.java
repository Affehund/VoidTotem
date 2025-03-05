package com.affehund.voidtotem.data;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotemForge;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class VoidTotemDataGeneration {

    /*
    client-side data generators
     */

    public static final class LanguageGen extends LanguageProvider {

        public LanguageGen(PackOutput packOutput, String locale) {
            super(packOutput, ModConstants.MOD_ID, locale);
        }

        @Override
        protected void addTranslations() {
            String locale = this.getName().replace("Languages: ", "");
            switch (locale) {
                case "de_de" -> {
                    add("_comment", "Translation (de_de) by Affehund");
                    add(VoidTotemForge.VOID_TOTEM_ITEM.get(), "Totem der Unsterblichkeit in der Leere");
                    add(ModConstants.TOOLTIP_VOID_TOTEM,
                            "Lege es in deine Haupt-/Nebenhand, um zu verhindern zu sterben, wenn du in die Leere fällst.");
                    add(ModConstants.ADVANCEMENT_VOID_TOTEM_TITLE, "Post mortem 2");
                    add(ModConstants.ADVANCEMENT_VOID_TOTEM_DESC,
                            "Benutze ein Totem der Unsterblichkeit in der Leere, um dem Tod, wenn du in die Leere fällst, von der Schippe zu springen");
                    add("trinkets.slot.charm.charm", "Amulett");
                }
                case "en_us" -> {
                    add("_comment", "Translation (en_us) by Affehund");
                    add(VoidTotemForge.VOID_TOTEM_ITEM.get(), "Totem of Void Undying");
                    add(ModConstants.TOOLTIP_VOID_TOTEM,
                            "Put in your main-/offhand to prevent dying if you fall in the void.");
                    add(ModConstants.ADVANCEMENT_VOID_TOTEM_TITLE, "Postmortal 2");
                    add(ModConstants.ADVANCEMENT_VOID_TOTEM_DESC,
                            "Use a Totem of Void Undying to cheat death when falling in the void");
                    add("trinkets.slot.charm.charm", "Charm");

                    addAutoConfigOption("ADD_END_CITY_TREASURE");
                    addAutoConfigOption("BLOCKLISTED_DIMENSIONS");
                    addAutoConfigOption("DISPLAY_TOTEM_ON_CHEST");
                    addAutoConfigOption("GIVE_TOTEM_EFFECTS");
                    addAutoConfigOption("IS_INVERTED_BLOCKLIST");
                    addAutoConfigOption("NEEDS_TOTEM");
                    addAutoConfigOption("SHOW_TOTEM_TOOLTIP");
                    addAutoConfigOption("TELEPORT_HEIGHT_OFFSET");
                    addAutoConfigOption("USE_TOTEM_FROM_INVENTORY");
                }
            }
        }

        private void addAutoConfigOption(String key) {
            add("text.autoconfig." + ModConstants.MOD_ID + ".option." + key, StringUtils.capitalizeFirstLetter(key.toLowerCase(Locale.ROOT).replace("_", " ")));
        }
    }

    public static final class ItemModelGen extends ItemModelProvider {

        public ItemModelGen(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
            super(packOutput, ModConstants.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            this.singleTexture(VoidTotemForge.VOID_TOTEM_ITEM.get());
        }

        private void singleTexture(Item item) {
            var registryName = BuiltInRegistries.ITEM.getKey(item);
            this.singleTexture(Objects.requireNonNull(registryName).getPath(), ResourceLocation.withDefaultNamespace("item/generated"), "layer0", this.modLoc("item/" + registryName.getPath()));
        }
    }


    /*
     server-side data generators
    */

    public static class AdvancementGen implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.@NotNull Provider provider, @NotNull Consumer<AdvancementHolder> consumer, @NotNull ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                    .parent(Advancement.Builder.advancement()
                            .build(ResourceLocation.withDefaultNamespace(ModConstants.ADVANCEMENT_ADVENTURE_TOTEM_PATH)))
                    .display(VoidTotemForge.VOID_TOTEM_ITEM.get(),
                            Component.translatable(ModConstants.ADVANCEMENT_VOID_TOTEM_TITLE),
                            Component.translatable(ModConstants.ADVANCEMENT_VOID_TOTEM_DESC),
                            null, AdvancementType.GOAL, true, true, false)
                    .addCriterion("used_void_totem", UsedTotemTrigger.TriggerInstance.usedTotem(VoidTotemForge.VOID_TOTEM_ITEM.get()))
                    .save(consumer, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, ModConstants.ADVANCEMENT_ADVENTURE_VOID_TOTEM_PATH), existingFileHelper);

        }
    }

    public static class BlockTagsGen extends BlockTagsProvider {
        public BlockTagsGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, ModConstants.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.@NotNull Provider p_256380_) {
        }
    }


    public static final class RecipeGen extends RecipeProvider {
        public RecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, VoidTotemForge.VOID_TOTEM_ITEM.get()).pattern("cec").pattern("ltl").pattern(" e ")
                    .define('c', Items.CHORUS_FRUIT).define('e', Items.ENDER_EYE).define('l', Items.LAPIS_LAZULI)
                    .define('t', Items.TOTEM_OF_UNDYING).unlockedBy("has_totem", has(Items.TOTEM_OF_UNDYING)).save(recipeOutput);
        }
    }
}