package com.affehund.voidtotem;

import com.affehund.voidtotem.core.ClientPacketHandler;

import net.fabricmc.api.ClientModInitializer;

/**
 * @author Affehund
 *
 */
public class VoidTotemFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPacketHandler.register();
	}
}
