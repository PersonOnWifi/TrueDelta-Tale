package com.truedeltatale;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    /** Every spell item. Used for Firegrave fusion and for repairing Knight's Swoon. */
    public static final TagKey<Item> MAGIC_ITEMS =
            ItemTags.create(new ResourceLocation(TrueDeltaTale.MODID, "magic_items"));

    private ModTags() {}
}
