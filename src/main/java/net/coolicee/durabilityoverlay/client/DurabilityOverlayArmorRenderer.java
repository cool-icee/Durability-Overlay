package net.coolicee.durabilityoverlay.client;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class DurabilityOverlayArmorRenderer implements ArmorRenderer {
    private static final Identifier OVERLAY_TEXTURE = Identifier.of("durabilityoverlay", "textures/armor/durability_overlay_1.png");

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                       LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> model) {
        if (!(stack.getItem() instanceof ArmorItem armorItem)) return;

        // Set visible model parts
        model.setVisible(false);
        switch (slot) {
            case HEAD -> { model.head.visible = true; model.hat.visible = true; }
            case CHEST -> { model.body.visible = true; model.rightArm.visible = true; model.leftArm.visible = true; }
            case LEGS -> { model.body.visible = true; model.rightLeg.visible = true; model.leftLeg.visible = true; }
            case FEET -> { model.rightLeg.visible = true; model.leftLeg.visible = true; }
        }

        // Render base layer 1 (all armor)
        String matName = armorItem.getMaterial().name().toLowerCase();
        Identifier layer1 = Identifier.of("minecraft", "textures/models/armor/" + matName + "_layer_1.png");
        ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, layer1);

        // Render base layer 2 (chest/legs, non-turtle)
        if ((slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS) &&
                !armorItem.getMaterial().equals(ArmorMaterials.TURTLE)) {
            Identifier layer2 = Identifier.of("minecraft", "textures/models/armor/" + matName + "_layer_2.png");
            ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, layer2);
        }

        // Render leather dye overlay if applicable
        if (armorItem instanceof DyeableArmorItem dyeable) {
            int dyeColor = dyeable.getColor(stack);
            float r = ((dyeColor >> 16) & 255) / 255f;
            float g = ((dyeColor >> 8) & 255) / 255f;
            float b = (dyeColor & 255) / 255f;
            Identifier leatherOverlay = Identifier.of("minecraft", "textures/models/armor/leather_layer_1_overlay.png");
            RenderLayer leatherLayer = RenderLayer.getArmorCutoutNoCull(leatherOverlay);
            VertexConsumer vc = vertexConsumers.getBuffer(leatherLayer);
            vc.color(r, g, b, 1f);
            model.render(matrices, vc, light, OverlayTexture.DEFAULT_UV);
            vc.color(1f, 1f, 1f, 1f); // Reset
        }

        // Render durability overlay on top
        float durability = 1f - (float) stack.getDamage() / stack.getMaxDamage();
        int color = durabilityToColor(durability);
        float dr = ((color >> 16) & 255) / 255f;
        float dg = ((color >> 8) & 255) / 255f;
        float db = (color & 255) / 255f;
        RenderLayer overlayLayer = RenderLayer.getArmorCutoutNoCull(OVERLAY_TEXTURE);
        VertexConsumer consumer = vertexConsumers.getBuffer(overlayLayer);
        consumer.color(dr, dg, db, 1f);
        model.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
        consumer.color(1f, 1f, 1f, 1f); // Reset
    }

    private int durabilityToColor(float durability) {
        int red = (int) ((1f - durability) * 255);
        int green = (int) (durability * 255);
        return (red << 16) | (green << 8);
    }
}