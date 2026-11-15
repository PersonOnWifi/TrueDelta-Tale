package com.truedeltatale.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * One structure class for both tiers. The JSON under data/truedeltatale/worldgen/structure
 * sets "rare": false (common ruins) or "rare": true (rare ruins).
 *
 * The footprint always sits inside a single chunk (11x11 or 15x15 around the chunk centre),
 * so placement never has to cross chunk borders.
 */
public class DarkRuinsStructure extends Structure {

    public static final Codec<DarkRuinsStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            settingsCodec(instance),
            Codec.BOOL.optionalFieldOf("rare", false).forGetter(s -> s.rare)
    ).apply(instance, DarkRuinsStructure::new));

    private final boolean rare;

    public DarkRuinsStructure(StructureSettings settings, boolean rare) {
        super(settings);
        this.rare = rare;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();

        int surface = height(context, x, z, Heightmap.Types.WORLD_SURFACE_WG);
        int ground = height(context, x, z, Heightmap.Types.OCEAN_FLOOR_WG);
        if (surface > ground) {
            return Optional.empty(); // water at the centre
        }

        // Reject steep terrain by comparing the four corners of the footprint.
        int half = DarkRuinsPiece.sizeFor(rare) / 2;
        int min = surface;
        int max = surface;
        for (int[] corner : new int[][] {{-half, -half}, {half, -half}, {-half, half}, {half, half}}) {
            int h = height(context, x + corner[0], z + corner[1], Heightmap.Types.WORLD_SURFACE_WG);
            int g = height(context, x + corner[0], z + corner[1], Heightmap.Types.OCEAN_FLOOR_WG);
            if (h > g) {
                return Optional.empty(); // water at a corner
            }
            min = Math.min(min, h);
            max = Math.max(max, h);
        }
        if (max - min > 7) {
            return Optional.empty();
        }

        BlockPos origin = new BlockPos(x - half, surface, z - half);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(new BlockPos(x, surface, z),
                builder -> builder.addPiece(new DarkRuinsPiece(origin, rare, seed))));
    }

    private static int height(GenerationContext context, int x, int z, Heightmap.Types type) {
        return context.chunkGenerator().getFirstOccupiedHeight(x, z, type, context.heightAccessor(), context.randomState());
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DARK_RUINS.get();
    }
}
