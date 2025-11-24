package net.coolicee.durabilityoverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Renders vanilla armor models but multiplies them by a durability-based tint.
 * Safe: does not change item icons or atlas state; only affects worn models.
 */
public class DurabilityOverlayArmorRenderer implements ArmorRenderer {

    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            ItemStack stack,
            LivingEntity entity,
            EquipmentSlot slot,
            int light,
            BipedEntityModel<LivingEntity> contextModel
    ) {
        // Only handle actual armor items
        if (!(stack.getItem() instanceof ArmorItem)) {
            return; // let vanilla handle it
        }
        // Compute durability tint
        int color = getDurabilityColor(stack);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Set shader color for tint
        RenderSystem.setShaderColor(r, g, b, 1.0f);

        // Get vanilla armor texture
        Identifier tex = getArmorTexture((ArmorItem) stack.getItem(), slot);

        // Render the armor model with the texture
        ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, contextModel, tex);

        // Reset shader color so nothing else is affected
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /**
     * Returns the durability-stage color for the item (0xRRGGBB).
     * Green -> Yellow -> Orange -> Red based on fractions described by you.
     */
    private int getDurabilityColor(ItemStack stack) {
        if (!stack.isDamageable()) return 0xFFFFFF;

        float percent = 1f - ((float) stack.getDamage() / stack.getMaxDamage());

        if (percent >= 0.75f)
            return 0x00FF00; // green
        if (percent >= 0.50f)
            return 0xFFFF00; // yellow
        if (percent >= 0.25f)
            return 0xFFA500; // orange
        if (percent >= 0.0f)
            return 0xFF0000;// red
        return 0xFFFFFF;
    }
    /**
     * Returns the vanilla armor texture for a given armor item and slot.
     */
    private Identifier getArmorTexture(ArmorItem armorItem, EquipmentSlot slot) {
        // Use the vanilla material name
        String materialName = switch (armorItem.getMaterial().toString().toLowerCase()) {
            case "diamond" -> "diamond";
            case "netherite" -> "netherite";
            case "iron" -> "iron";
            case "gold" -> "gold";
            case "chain" -> "chainmail";
            case "leather" -> "leather";
            default -> "diamond"; // fallback
        };

        String layer = (slot == EquipmentSlot.LEGS) ? "2" : "1";

// Use tryParse instead of constructor
        Identifier texture = Identifier.tryParse("minecraft:textures/models/armor/" + materialName + "_layer_" + layer + ".png");

// Safety fallback
        if (texture == null) {
            texture = Identifier.tryParse("minecraft:textures/models/armor/diamond_layer_1.png");
        }
        return texture;
    }
}
