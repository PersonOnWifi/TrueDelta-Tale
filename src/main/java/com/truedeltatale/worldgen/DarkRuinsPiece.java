package com.truedeltatale.worldgen;

import com.truedeltatale.TrueDeltaTale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A ruined Dark World building, built block by block (no NBT templates).
 *
 * Layout (local coordinates, y = 0 is the floor):
 *  - checkerboard floor, decayed perimeter walls with a 3-wide doorway on the south side
 *  - a tall tower in the north-west corner so the ruin is visible from a long way off
 *  - a chest in the middle guarded by obvious traps: a stone pressure plate on a red
 *    concrete cross, with TNT directly under the plate
 *  - rare tier: bigger, and the chest sits on an obsidian island inside a lava moat
 */
public class DarkRuinsPiece extends StructurePiece {

    public static final int COMMON_SIZE = 11;
    public static final int RARE_SIZE = 15;

    private static final ResourceLocation COMMON_LOOT =
            new ResourceLocation(TrueDeltaTale.MODID, "chests/dark_ruins_common");
    private static final ResourceLocation RARE_LOOT =
            new ResourceLocation(TrueDeltaTale.MODID, "chests/dark_ruins_rare");

    private final BlockPos origin; // minimum corner, at floor level
    private final boolean rare;
    private final long seed;

    public static int sizeFor(boolean rare) {
        return rare ? RARE_SIZE : COMMON_SIZE;
    }

    private static BoundingBox boxFor(BlockPos origin, boolean rare) {
        int size = sizeFor(rare);
        int height = rare ? 32 : 22;
        return new BoundingBox(origin.getX(), origin.getY() - 12, origin.getZ(),
                origin.getX() + size - 1, origin.getY() + height, origin.getZ() + size - 1);
    }

    public DarkRuinsPiece(BlockPos origin, boolean rare, long seed) {
        super(ModStructures.DARK_RUINS_PIECE.get(), 0, boxFor(origin, rare));
        this.origin = origin;
        this.rare = rare;
        this.seed = seed;
    }

    public DarkRuinsPiece(CompoundTag tag) {
        super(ModStructures.DARK_RUINS_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("OX"), tag.getInt("OY"), tag.getInt("OZ"));
        this.rare = tag.getBoolean("Rare");
        this.seed = tag.getLong("Seed");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OX", origin.getX());
        tag.putInt("OY", origin.getY());
        tag.putInt("OZ", origin.getZ());
        tag.putBoolean("Rare", rare);
        tag.putLong("Seed", seed);
    }

    // ------------------------------------------------------------------ helpers

    private BlockPos at(int dx, int dy, int dz) {
        return origin.offset(dx, dy, dz);
    }

    private void put(WorldGenLevel level, BoundingBox bb, int dx, int dy, int dz, BlockState state) {
        BlockPos pos = at(dx, dy, dz);
        if (bb.isInside(pos) && !level.isOutsideBuildHeight(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private BlockState read(WorldGenLevel level, BoundingBox bb, int dx, int dy, int dz) {
        BlockPos pos = at(dx, dy, dz);
        if (bb.isInside(pos) && !level.isOutsideBuildHeight(pos)) {
            return level.getBlockState(pos);
        }
        return Blocks.STONE.defaultBlockState(); // treat as solid so loops stop
    }

    private static boolean fillable(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty() || state.canBeReplaced()
                || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS);
    }

    /** A stone pressure plate with TNT under it, marked by a red concrete cross. Obvious on purpose. */
    private void trapPad(WorldGenLevel level, BoundingBox bb, int cx, int cz) {
        put(level, bb, cx, 0, cz, Blocks.TNT.defaultBlockState());
        put(level, bb, cx, 1, cz, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        BlockState red = Blocks.RED_CONCRETE.defaultBlockState();
        put(level, bb, cx + 1, 0, cz, red);
        put(level, bb, cx - 1, 0, cz, red);
        put(level, bb, cx, 0, cz + 1, red);
        put(level, bb, cx, 0, cz - 1, red);
    }

    // ------------------------------------------------------------------ generation

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                            RandomSource ignored, BoundingBox bb, ChunkPos chunkPos, BlockPos pivot) {
        RandomSource rand = RandomSource.create(seed);
        int size = sizeFor(rare);
        int c = size / 2;
        int wallHeight = rare ? 7 : 5;

        // 1. Foundation, floor, and clearing the space above.
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                for (int dy = -1; dy >= -12; dy--) {
                    if (!fillable(read(level, bb, dx, dy, dz))) {
                        break;
                    }
                    put(level, bb, dx, dy, dz, Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                }

                boolean cracked = rand.nextInt(14) == 0;
                BlockState floor;
                if (cracked) {
                    floor = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
                } else if (((dx + dz) & 1) == 0) {
                    floor = Blocks.BLACK_CONCRETE.defaultBlockState();
                } else {
                    floor = Blocks.PURPLE_CONCRETE.defaultBlockState();
                }
                put(level, bb, dx, 0, dz, floor);

                for (int dy = 1; dy <= wallHeight + 3; dy++) {
                    if (!read(level, bb, dx, dy, dz).isAir()) {
                        put(level, bb, dx, dy, dz, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // 2. Decayed perimeter walls and pillars, with a doorway in the south wall.
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                boolean edge = dx == 0 || dz == 0 || dx == size - 1 || dz == size - 1;
                if (!edge) {
                    continue;
                }
                boolean corner = (dx == 0 || dx == size - 1) && (dz == 0 || dz == size - 1);
                boolean door = dz == size - 1 && Math.abs(dx - c) <= 1;
                boolean pillar = corner || (dx + dz) % 4 == 0;

                for (int dy = 1; dy <= wallHeight + 1; dy++) {
                    float roll = rand.nextFloat();
                    int pick = rand.nextInt(100);

                    boolean keep;
                    if (door && dy <= 3) {
                        keep = false;
                    } else if (door && dy == 4) {
                        keep = true;
                    } else if (dy == 1) {
                        keep = true;
                    } else if (pillar) {
                        keep = dy <= wallHeight || roll < 0.55F;
                    } else {
                        keep = dy <= wallHeight && roll > 0.12F * (dy - 1) + 0.05F;
                    }
                    if (!keep) {
                        continue;
                    }

                    BlockState state;
                    if (pillar) {
                        state = dy == wallHeight + 1
                                ? Blocks.CHISELED_DEEPSLATE.defaultBlockState()
                                : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
                    } else if (dy == 3 && ((dx + dz) & 1) == 1 && !door) {
                        state = Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
                    } else if (pick < 55) {
                        state = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                    } else if (pick < 85) {
                        state = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
                    } else {
                        state = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
                    }
                    put(level, bb, dx, dy, dz, state);
                }
            }
        }

        // 3. Scattered cobwebs inside.
        for (int dx = 1; dx < size - 1; dx++) {
            for (int dz = 1; dz < size - 1; dz++) {
                if (rand.nextInt(25) == 0) {
                    put(level, bb, dx, 1 + rand.nextInt(2), dz, Blocks.COBWEB.defaultBlockState());
                }
            }
        }

        // 4. Rare tier: obsidian island in a lava moat, with a single obsidian bridge on the north side.
        if (rare) {
            for (int dx = c - 2; dx <= c + 2; dx++) {
                for (int dz = c - 2; dz <= c + 2; dz++) {
                    boolean inner = Math.abs(dx - c) <= 1 && Math.abs(dz - c) <= 1;
                    boolean bridge = dx == c && dz == c - 2;
                    put(level, bb, dx, -1, dz, Blocks.OBSIDIAN.defaultBlockState());
                    if (inner || bridge) {
                        put(level, bb, dx, 0, dz, Blocks.OBSIDIAN.defaultBlockState());
                    } else {
                        put(level, bb, dx, 0, dz, Blocks.LAVA.defaultBlockState());
                    }
                }
            }
        }

        // 5. Obvious traps between the doorway and the chest.
        int laneZ = c + (rare ? 4 : 2);
        int flankX = rare ? 4 : 3;
        int flankZ = c + (rare ? 5 : 3);
        trapPad(level, bb, c, laneZ);
        trapPad(level, bb, c - flankX, flankZ);
        trapPad(level, bb, c + flankX, flankZ);

        // 6. Chests.
        createChest(level, bb, rand, at(c, 1, c), rare ? RARE_LOOT : COMMON_LOOT, null);
        if (rare) {
            // Rare ruins also hold a common chest in the north-east corner room.
            createChest(level, bb, rand, at(size - 3, 1, 2), COMMON_LOOT, null);
        }

        // 7. The tower, so the ruin can be seen from far away.
        int towerHeight = rare ? 26 : 16;
        for (int dy = 1; dy <= towerHeight; dy++) {
            boolean wide = dy <= towerHeight * 3 / 5;
            for (int tx = 0; tx < 3; tx++) {
                for (int tz = 0; tz < 3; tz++) {
                    if (!wide && !(tx == 1 && tz == 1)) {
                        continue;
                    }
                    BlockState state = ((dy + tx + tz) & 1) == 0
                            ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                            : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                    put(level, bb, tx, dy, tz, state);
                }
            }
        }
        put(level, bb, 1, towerHeight + 1, 1, Blocks.SEA_LANTERN.defaultBlockState());
        put(level, bb, 1, towerHeight + 2, 1, Blocks.CRYING_OBSIDIAN.defaultBlockState());
    }
}
