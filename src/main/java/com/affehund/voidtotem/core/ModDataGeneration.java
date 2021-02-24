package com.affehund.voidtotem.core;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class ModDataGeneration {
	private static final Logger DATAGEN_LOGGER = LogManager.getLogger();

	public static final class LanguageGen extends LanguageProvider {
		public final String modID;

		public LanguageGen(DataGenerator gen, String modid, String locale) {
			super(gen, ModConstants.MOD_ID, locale);
			this.modID = modid;
		}

		@Override
		protected void addTranslations() {
			String locale = this.getName().replace("Languages: ", "");
			switch (locale) {
			case "de_de":
				add("_comment", "Translation (de_de) by Affehund");
				add(VoidTotem.VOID_TOTEM_ITEM.get(), "Totem der Unsterblichkeit in der Leere");
				add(ModConstants.VOID_TOTEM_TOOLTIP,
						"Lege diese Totem in deine Haupt-/Nebenhand, um zu verhindern, du stirbst, wenn du in die Leere fällst.");
				break;
			case "en_us":
				add("_comment", "Translation (en_us) by Affehund");
				add(VoidTotem.VOID_TOTEM_ITEM.get(), "Totem of Void Undying");
				add(ModConstants.VOID_TOTEM_TOOLTIP,
						"Put this totem in your main-/offhand to prevent dying if you fall in the void.");
				break;
			}
		}
	}

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

		protected void defaultItem(ResourceLocation id, Item item) {
			this.withExistingParent(id.getPath(), "item/generated").texture("layer0",
					new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
			DATAGEN_LOGGER.debug("Generated item model for: " + item.getRegistryName());
		}
	}

	public static final class RecipeGen extends RecipeProvider implements IConditionBuilder {
		public RecipeGen(DataGenerator gen) {
			super(gen);
		}

		@Override
		protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
			// chorus fruit, eye of ender, chorus fruit
			// emerald, totem of undying, emerald
			// -, eye of ender, -
			ShapedRecipeBuilder.shapedRecipe(VoidTotem.VOID_TOTEM_ITEM.get()).patternLine("cec").patternLine("dtd")
					.patternLine(" e ").key('c', Items.CHORUS_FRUIT).key('e', Items.ENDER_EYE).key('d', Items.EMERALD)
					.key('t', Items.TOTEM_OF_UNDYING).addCriterion("has_chorus_fruit", hasItem(Items.CHORUS_FRUIT))
					.addCriterion("has_ender_eye", hasItem(Items.ENDER_EYE))
					.addCriterion("has_emerald", hasItem(Items.EMERALD))
					.addCriterion("has_totem", hasItem(Items.TOTEM_OF_UNDYING)).build(consumer);
		}
	}
}
