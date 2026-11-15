package com.truedeltatale.worldgen;

import com.truedeltatale.TrueDeltaTale;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, TrueDeltaTale.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, TrueDeltaTale.MODID);

    public static final RegistryObject<StructureType<DarkRuinsStructure>> DARK_RUINS =
            STRUCTURE_TYPES.register("dark_ruins", () -> () -> DarkRuinsStructure.CODEC);

    public static final RegistryObject<StructurePieceType> DARK_RUINS_PIECE =
            PIECES.register("dark_ruins_piece", () -> (StructurePieceType.ContextlessType) DarkRuinsPiece::new);

    private ModStructures() {}
}
