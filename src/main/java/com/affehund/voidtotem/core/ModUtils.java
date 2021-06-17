package com.affehund.voidtotem.core;
import java.util.List;

import com.affehund.voidtotem.VoidTotemFabric;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * A class with some utilities methods for the mod.
 * 
 * @author Affehund
 *
 */
public class ModUtils {
	public static ItemStack findCuriosItem(Item item, ServerPlayerEntity player) {
		return TrinketsApi.getTrinketComponent(player).map(component -> {
			List<Pair<SlotReference, ItemStack>> res = component.getEquipped(item);
			return res.size() > 0 ? res.get(0).getRight() : ItemStack.EMPTY;
		}).orElse(ItemStack.EMPTY);
	}

	public static boolean isVoidTotemOrTotem(ItemStack stack) {
		Item item = stack.getItem();
		boolean isVoidTotem = item == VoidTotemFabric.VOID_TOTEM_ITEM;
		boolean isTotemOfUndying = VoidTotemFabric.CONFIG.ALLOW_TOTEM_OF_UNDYING ? item == Items.TOTEM_OF_UNDYING
				: false;
		if (isVoidTotem || isTotemOfUndying) {
			return true;
		}
		return false;
	}

	public static ItemStack copyAndRemoveItemStack(ItemStack itemStack, ServerPlayerEntity player) {
		ItemStack itemStackCopy = itemStack.copy();
		if (!itemStack.isEmpty()) { // add stats if stack isn't empty / null
			player.incrementStat(Stats.USED.getOrCreateStat(itemStack.getItem()));
			Criteria.USED_TOTEM.trigger(player, itemStack);
		}
		itemStack.decrement(1);
		return itemStackCopy;
	}

	public static boolean tryUseVoidTotem(LivingEntity livingEntity, DamageSource source) {
		if (VoidTotemFabric.CONFIG.BLACKLISTED_DIMENSIONS
				.contains(livingEntity.world.getRegistryKey().getValue().toString())) // dim on blacklist
			return false;
		if (source != DamageSource.OUT_OF_WORLD && livingEntity.getX() > -64)
			return false;
		if (livingEntity instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) livingEntity;
			player.networkHandler.floatingTicks = 0;

			ItemStack itemstack = null;
			boolean foundValidStack = false;

			if (!VoidTotemFabric.CONFIG.NEEDS_TOTEM) { // totem is needed (config)
				itemstack = ItemStack.EMPTY;
				foundValidStack = true;
			} else if (VoidTotemFabric.CONFIG.USE_TOTEM_FROM_INVENTORY) { // totems in the
				// player inv used (config)
				for (int i = 0; i < player.getInventory().size(); i++) { // for each player inventory slot
					ItemStack stack = player.getInventory().getStack(i);
					if (ModUtils.isVoidTotemOrTotem(stack)) { // is valid item
						itemstack = ModUtils.copyAndRemoveItemStack(stack, player);
						foundValidStack = true;
						break;
					}
				}
			} else {
				if (FabricLoader.getInstance().isModLoaded(ModConstants.TRINKETS_MOD_ID)) { // curios api is loaded
					ItemStack curiosVoidTotemStack = ModUtils.findCuriosItem(VoidTotemFabric.VOID_TOTEM_ITEM, player);
					ItemStack curiosVanillaTotemStack = VoidTotemFabric.CONFIG.ALLOW_TOTEM_OF_UNDYING
							? ModUtils.findCuriosItem(Items.TOTEM_OF_UNDYING, player)
							: ItemStack.EMPTY;
					if (curiosVoidTotemStack != ItemStack.EMPTY) {
						itemstack = ModUtils.copyAndRemoveItemStack(curiosVoidTotemStack, player);
						foundValidStack = true;
					} else if (curiosVanillaTotemStack != ItemStack.EMPTY) {
						itemstack = ModUtils.copyAndRemoveItemStack(curiosVanillaTotemStack, player);
						foundValidStack = true;
					}
				}

				if (!foundValidStack) {
					for (Hand hand : Hand.values()) { // for each hand (main-/offhand)
						ItemStack stack = player.getStackInHand(hand);
						if (ModUtils.isVoidTotemOrTotem(stack)) { // is valid item
							itemstack = ModUtils.copyAndRemoveItemStack(stack, player);
							foundValidStack = true;
							break;
						}
					}
				}
			}

			if (itemstack != null && foundValidStack) { // check if stack isn't null and if there
				if (player.networkHandler.requestedTeleportPos != null) // wants to teleport
					return false;

				if (player.hasPlayerRider()) { // has passenger and remove it
					player.removeAllPassengers();
				}
				player.stopRiding();

				long lastBlockPos = ((IPlayerEntityMixinAccessor) player).getBlockPosAsLong();
				BlockPos teleportPos = BlockPos.fromLong(lastBlockPos);

				boolean teleportedToBlock = false;
				for (int i = 0; i < 16; i++) { // try 16 times to teleport the player to a good spot

					double x = teleportPos.getX() + (player.getRandom().nextDouble() - 0.5D) * 4.0D;
					double y = MathHelper.clamp(player.getRandom().nextInt() * player.world.getHeight(), 0.0D,
							player.world.getHeight() - 1);
					double z = teleportPos.getZ() + (player.getRandom().nextDouble() - 0.5D) * 4.0;

					if (player.teleport(x, y, z, true)) { // if can teleport break
						teleportedToBlock = true;
						break;
					}
				}

				if (!teleportedToBlock) { // if can't teleport to a block teleport to height set in config
					player.teleport(teleportPos.getX(), VoidTotemFabric.CONFIG.TELEPORT_HEIGHT, teleportPos.getZ());
					player.networkHandler.floatingTicks = 0;
				}

				player.addScoreboardTag(ModConstants.NBT_TAG); // add tag to prevent fall damage
				player.setHealth(1.0f); // setHealth

				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeItemStack(itemstack);
				buf.writeInt(player.getId());
				ServerPlayNetworking.send(player, ModConstants.IDENTIFIER_TOTEM_EFFECT_PACKET, buf);
				for (ServerPlayerEntity player2 : PlayerLookup.tracking((ServerWorld) player.world,
						player.getBlockPos())) {
					ServerPlayNetworking.send(player2, ModConstants.IDENTIFIER_TOTEM_EFFECT_PACKET, buf);
				}
				return true;
			}
		}
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static void playActivateAnimation(ItemStack stack, Entity entity) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30); // particles
		mc.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE,
				entity.getSoundCategory(), 1.0F, 1.0F, false); // sound
		if (entity == mc.player) {
			mc.gameRenderer.showFloatingItem(stack); // animation
		}
	}
}
