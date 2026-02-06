package net.coolicee.durabilityoverlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.HashSet;
import java.util.Set;

public class DurabilityOverlayClient implements ClientModInitializer {

    private final Set<Item> registeredArmor = new HashSet<>();

    @Override
    public void onInitializeClient() {
        System.out.println("[DurabilityOverlay] Client armor tint initialized");

        for (Item item : Registries.ITEM) {
            if (item instanceof ArmorItem armor) {
                if (!registeredArmor.contains(item)) {
                    try {
                        ArmorRenderer.register(new DurabilityOverlayArmorRenderer(), armor);
                        registeredArmor.add(item);
                    } catch (IllegalArgumentException e) {
                        // Already registered, ignore
                        System.out.println("[DurabilityOverlay] Renderer already exists for " + item);
                    }
                }
            }
        }
    }
}