package net.coolicee.durabilityoverlay;

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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class DurabilityOverlayArmorRenderer implements ArmorRenderer {

    private static final Identifier OVERLAY_TEXTURE =
            Identifier.of("durabilityoverlay", "textures/armor/durability_overlay_1.png");

    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            ItemStack stack,
            LivingEntity entity,
            EquipmentSlot slot,
            int light,
            BipedEntityModel<LivingEntity> model
    ) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        float durability = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
        int color = durabilityToColor(durability);

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        RenderLayer layer = RenderLayer.getArmorCutoutNoCull(OVERLAY_TEXTURE);
        VertexConsumer consumer = vertexConsumers.getBuffer(layer);

        model.setVisible(false);
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
        }

        consumer.color(r, g, b, 1.0f);
        model.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
        consumer.color(1f, 1f, 1f, 1f); // reset
    }

    private int durabilityToColor(float durability) {
        int red = (int) ((1.0f - durability) * 255);
        int green = (int) (durability * 255);
        return (red << 16) | (green << 8);
    }
}
