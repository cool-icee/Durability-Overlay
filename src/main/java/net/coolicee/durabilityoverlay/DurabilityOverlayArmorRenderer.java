package net.coolicee.durabilityoverlay;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Durability-overlay armor renderer that:
 *  - tints only armor geometry (not the whole entity),
 *  - renders only the relevant parts for each armor slot,
 *  - uses per-vertex packed color so tinting won't leak.
 *
 * Works with your 1.21 mappings (BipedEntityModel fields shown earlier).
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
        if (!(stack.getItem() instanceof ArmorItem armorItem)) return;

        // Resolve safe armor texture
        Identifier tex = getArmorTextureSafe(armorItem, slot);
        if (tex == null) return;

        // Durability color -> RGB int (0xRRGGBB)
        int rgb = getDurabilityColor(stack);

        // Pack to ARGB (model.render expects packedColor)
        int packedColor = 0xFF000000 | rgb;

        // Acquire vertex consumer for armor texture
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(tex));

        // Save original visibility of parts so we can restore after rendering
        boolean headVis = contextModel.head.visible;
        boolean hatVis  = contextModel.hat.visible;
        boolean bodyVis = contextModel.body.visible;
        boolean rArmVis = contextModel.rightArm.visible;
        boolean lArmVis = contextModel.leftArm.visible;
        boolean rLegVis = contextModel.rightLeg.visible;
        boolean lLegVis = contextModel.leftLeg.visible;

        try {
            // Hide everything, then enable only parts relevant to the slot
            contextModel.head.visible = false;
            contextModel.hat.visible = false;
            contextModel.body.visible = false;
            contextModel.rightArm.visible = false;
            contextModel.leftArm.visible = false;
            contextModel.rightLeg.visible = false;
            contextModel.leftLeg.visible = false;

            switch (slot) {
                case HEAD -> {
                    contextModel.head.visible = true;
                    contextModel.hat.visible = true;
                }
                case CHEST -> {
                    contextModel.body.visible = true;
                    contextModel.rightArm.visible = true;
                    contextModel.leftArm.visible = true;
                }
                case LEGS -> {
                    // For leggings, vanilla uses layer 2 that affects legs + body slightly.
                    contextModel.body.visible = true;
                    contextModel.rightLeg.visible = true;
                    contextModel.leftLeg.visible = true;
                }
                case FEET -> {
                    contextModel.rightLeg.visible = true;
                    contextModel.leftLeg.visible = true;
                }
            }

            // Render only the enabled parts of the contextModel with the chosen tint.
            // 1.21 signature: render(matrices, VertexConsumer, int light, int overlay, int packedColor)
            contextModel.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, packedColor);
        } finally {
            // Restore visibility regardless of what happened
            contextModel.head.visible = headVis;
            contextModel.hat.visible = hatVis;
            contextModel.body.visible = bodyVis;
            contextModel.rightArm.visible = rArmVis;
            contextModel.leftArm.visible = lArmVis;
            contextModel.rightLeg.visible = rLegVis;
            contextModel.leftLeg.visible = lLegVis;
        }
    }

    // Durability -> 0xRRGGBB
    private int getDurabilityColor(ItemStack stack) {
        if (!stack.isDamageable()) return 0xFFFFFF;
        float percent = 1f - (stack.getDamage() / (float) stack.getMaxDamage());

        if (percent >= 0.75f) return 0x00FF00; // green
        if (percent >= 0.50f) return 0xFFFF00; // yellow
        if (percent >= 0.25f) return 0xFFA500; // orange
        return 0xFF0000;                         // red
    }

    // Build a safe Identifier for vanilla armor textures (fallback to diamond)
    private Identifier getArmorTextureSafe(ArmorItem armorItem, EquipmentSlot slot) {
        String material;
        if (armorItem.getMaterial() == ArmorMaterials.NETHERITE) material = "netherite";
        else if (armorItem.getMaterial() == ArmorMaterials.DIAMOND) material = "diamond";
        else if (armorItem.getMaterial() == ArmorMaterials.IRON) material = "iron";
        else if (armorItem.getMaterial() == ArmorMaterials.GOLD) material = "gold";
        else if (armorItem.getMaterial() == ArmorMaterials.CHAIN) material = "chainmail";
        else if (armorItem.getMaterial() == ArmorMaterials.LEATHER) material = "leather";
        else material = "diamond";

        String layer = (slot == EquipmentSlot.LEGS) ? "2" : "1";
        String path = "minecraft:textures/models/armor/" + material + "_layer_" + layer + ".png";

        Identifier id = Identifier.tryParse(path);
        if (id == null) {
            id = Identifier.tryParse("minecraft:textures/models/armor/diamond_layer_1.png");
        }
        return id;
    }
}