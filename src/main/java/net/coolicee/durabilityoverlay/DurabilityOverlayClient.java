package net.coolicee.durabilityoverlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.item.ArmorItem;
import net.minecraft.registry.Registries;

@SuppressWarnings("unused")
public class DurabilityOverlayClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        System.out.println("[DurabilityOverlay] Client armor tint loaded");

        // Register for every armor item globally
        for (var item : Registries.ITEM) {
            if (item instanceof ArmorItem armor) {
                ArmorRenderer.register(new DurabilityOverlayArmorRenderer(), armor);
            }
        }
    }

}
