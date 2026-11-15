package com.truedeltatale;

import com.truedeltatale.item.SpellItem;
import com.truedeltatale.spell.Spells;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TrueDeltaTale.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TrueDeltaTale.MODID);

    private static Item.Properties props(Rarity rarity) {
        return new Item.Properties().stacksTo(1).rarity(rarity);
    }

    // Cooldowns are in ticks (20 ticks = 1 second).
    public static final RegistryObject<Item> RUDE_BUSTER = ITEMS.register("rude_buster",
            () -> new SpellItem(props(Rarity.UNCOMMON), 30, false, Spells::rudeBuster));

    public static final RegistryObject<Item> FIRESHOCK = ITEMS.register("fireshock",
            () -> new SpellItem(props(Rarity.UNCOMMON), 25, false, Spells::fireshock));

    public static final RegistryObject<Item> ICESHOCK = ITEMS.register("iceshock",
            () -> new SpellItem(props(Rarity.UNCOMMON), 25, false, Spells::iceshock));

    public static final RegistryObject<Item> SNOWGRAVE = ITEMS.register("snowgrave",
            () -> new SpellItem(props(Rarity.RARE), 160, false, Spells::snowgrave));

    public static final RegistryObject<Item> THUNDER_BUSTER = ITEMS.register("thunder_buster",
            () -> new SpellItem(props(Rarity.RARE), 50, false, Spells::thunderBuster));

    public static final RegistryObject<Item> FIREGRAVE = ITEMS.register("firegrave",
            () -> new SpellItem(props(Rarity.EPIC), 160, false, Spells::firegrave));

    /** Legendary: only 5 uses. When spent it stays in your inventory and can be repaired at an anvil. */
    public static final RegistryObject<Item> KNIGHTS_SWOON = ITEMS.register("knights_swoon",
            () -> new SpellItem(new Item.Properties().durability(5).rarity(Rarity.EPIC), 40, true, Spells::knightsSwoon));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TrueDeltaTale.MODID))
                    .icon(() -> new ItemStack(SNOWGRAVE.get()))
                    .displayItems((params, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());

    private ModItems() {}
}
