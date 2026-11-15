package com.truedeltatale;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TrueDeltaTale.MODID)
public final class ModEvents {

    /** Knight's Swoon + any other magic item in an anvil = fully repaired (5 uses). */
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.is(ModItems.KNIGHTS_SWOON.get())
                && left.isDamaged()
                && right.is(ModTags.MAGIC_ITEMS)
                && !right.is(ModItems.KNIGHTS_SWOON.get())) {
            ItemStack output = left.copy();
            output.setDamageValue(0);
            event.setOutput(output);
            event.setCost(3);
            event.setMaterialCost(1);
        }
    }

    private ModEvents() {}
}
