package net.coolicee.durabilityoverlay;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.OverlayTexture;

public class DurabilityOverlayArmorRenderer implements ArmorRenderer{

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

        Identifier tex = getArmorTextureSafe(armorItem, slot);
        if (tex == null) return;

        // durability color → packed ARGB
        int rgb = getDurabilityColor(stack);
        int packedColor = 0xFF000000 | rgb;  // force 255 alpha

        VertexConsumer vc =
                vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(tex));

        // 1.21 render signature:
        // render(matrices, vc, light, overlay, packedColor)
        contextModel.render(
                matrices,
                vc,
                light,
                OverlayTexture.DEFAULT_UV,
                packedColor
        );
    }

    private int getDurabilityColor(ItemStack stack) {
        if (!stack.isDamageable()) return 0xFFFFFF;
        float p = 1f - stack.getDamage() / (float) stack.getMaxDamage();

        if (p >= 0.75f) return 0x00FF00;
        if (p >= 0.50f) return 0xFFFF00;
        if (p >= 0.25f) return 0xFFA500;
        return 0xFF0000;
    }

    private Identifier getArmorTextureSafe(ArmorItem armorItem, EquipmentSlot slot) {
        String mat;
        if (armorItem.getMaterial() == ArmorMaterials.NETHERITE) mat = "netherite";
        else if (armorItem.getMaterial() == ArmorMaterials.DIAMOND) mat = "diamond";
        else if (armorItem.getMaterial() == ArmorMaterials.IRON) mat = "iron";
        else if (armorItem.getMaterial() == ArmorMaterials.GOLD) mat = "gold";
        else if (armorItem.getMaterial() == ArmorMaterials.CHAIN) mat = "chainmail";
        else if (armorItem.getMaterial() == ArmorMaterials.LEATHER) mat = "leather";
        else mat = "diamond";

        String layer = (slot == EquipmentSlot.LEGS) ? "2" : "1";

        String path = "minecraft:textures/models/armor/" + mat + "_layer_" + layer + ".png";
        Identifier id = Identifier.tryParse(path);

        if (id == null) {
            id = Identifier.tryParse(
                    "minecraft:textures/models/armor/diamond_layer_1.png"
            );
        }

        return id;
    }
}
