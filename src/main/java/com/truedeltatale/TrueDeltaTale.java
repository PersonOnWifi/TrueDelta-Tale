package com.truedeltatale;

import com.truedeltatale.worldgen.ModStructures;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TrueDeltaTale.MODID)
public class TrueDeltaTale {
    public static final String MODID = "truedeltatale";

    public TrueDeltaTale() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
        ModStructures.STRUCTURE_TYPES.register(bus);
        ModStructures.PIECES.register(bus);
    }
}
