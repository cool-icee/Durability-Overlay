package net.coolicee.durabilityoverlay.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;

public class DurabilityOverlayClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[DurabilityOverlay] Initializing client renderer...");

        ItemConvertible[] allArmor = Registries.ITEM.stream()
                .filter(item -> item.asItem() instanceof ArmorItem)
                .toArray(ItemConvertible[]::new);

        ArmorRenderer.register(new DurabilityOverlayArmorRenderer(), allArmor);

        System.out.println("[DurabilityOverlay] Registered renderer for " + allArmor.length + " armor items!");
    }
}