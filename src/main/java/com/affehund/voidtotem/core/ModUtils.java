package com.affehund.voidtotem.core;
import java.util.Arrays;
import java.util.List;

import com.affehund.voidtotem.VoidTotem;

import com.jab125.thonkutil.api.annotations.SubscribeEvent;
import com.jab125.thonkutil.api.events.server.entity.TotemUseEvent;
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
	public static boolean isVoidTotemOrTotem(ItemStack stack) {
		Item item = stack.getItem();
		boolean isVoidTotem = item == VoidTotem.VOID_TOTEM_ITEM;
		boolean isTotemOfUndying = VoidTotem.CONFIG.ALLOW_TOTEM_OF_UNDYING && item == Items.TOTEM_OF_UNDYING;
		return isVoidTotem || isTotemOfUndying;
	}

	@SubscribeEvent
	public static void useVoidTotem(TotemUseEvent event) {
		LivingEntity livingEntity = event.getEntity();
		if (VoidTotem.CONFIG.BLACKLISTED_DIMENSIONS.contains(livingEntity.world.getRegistryKey().getValue().toString())) return;
		if (event.getSource() != DamageSource.OUT_OF_WORLD && livingEntity.getY() > -64)  return;
		ItemStack totem = event.findTotem(VoidTotem.VOID_TOTEM_ITEM);
		if (totem == ItemStack.EMPTY) {
			if (VoidTotem.CONFIG.ALLOW_TOTEM_OF_UNDYING) {
				totem = event.findTotem(Items.TOTEM_OF_UNDYING);
			}
		}
		if (VoidTotem.CONFIG.USE_TOTEM_FROM_INVENTORY && totem == ItemStack.EMPTY) {
			if (livingEntity instanceof ServerPlayerEntity player)
			for (ItemStack itemStack : player.getInventory().main) { // for each player inventory slot
				if (ModUtils.isVoidTotemOrTotem(itemStack)) { // is valid item
					totem = itemStack;
					break;
				}
			}
		}
		event.setTotemActivateItem(totem);

		if (livingEntity instanceof ServerPlayerEntity serverPlayerEntity)
		if (serverPlayerEntity.networkHandler.requestedTeleportPos != null) return;

		if (livingEntity.hasPlayerRider()) livingEntity.removeAllPassengers();
		livingEntity.stopRiding();

		long lastBlockPos = ((ILivingEntityMixinAccessor) livingEntity).getBlockPosAsLong();
		BlockPos teleportPos = BlockPos.fromLong(lastBlockPos);

		double[]xyz = new double[]{0,0,0};
		boolean teleportedToBlock = false;
		for (int i = 0; i < 16; i++) { // try 16 times to teleport the entity to a good spot

			double x = teleportPos.getX() + (livingEntity.getRandom().nextDouble() - 0.5D) * 4.0D;
			double y = MathHelper.clamp(livingEntity.getRandom().nextInt() * livingEntity.world.getHeight(), 0.0D,
					livingEntity.world.getHeight() - 1);
			double z = teleportPos.getZ() + (livingEntity.getRandom().nextDouble() - 0.5D) * 4.0;
			xyz = new double[]{x, y, z};

			if (livingEntity.teleport(x, y, z, true)) { // if can teleport break
				teleportedToBlock = true;
				break;
			}
		}

		if (!teleportedToBlock) { // if can't teleport to a block teleport to height set in config
			livingEntity.teleport(teleportPos.getX(), VoidTotem.CONFIG.TELEPORT_HEIGHT, teleportPos.getZ());
			if (livingEntity instanceof ServerPlayerEntity serverPlayerEntity)
			serverPlayerEntity.networkHandler.floatingTicks = 0;
		}

		livingEntity.addScoreboardTag(ModConstants.NBT_TAG); // add tag to prevent fall damage
		event.saveEntity();
		event.regenerateHealth(); // setHealth
		event.playActivateAnimation();
		totem.decrement(1);
		System.out.println("Saved Mob. + Teleported: " + Arrays.toString(xyz));
	}
}
