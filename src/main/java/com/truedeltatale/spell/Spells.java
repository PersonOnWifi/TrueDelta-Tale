package com.truedeltatale.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/** All spell behaviour. Each method matches {@link com.truedeltatale.item.SpellItem.Spell}. */
public final class Spells {

    private static final DustParticleOptions PINK = new DustParticleOptions(new Vector3f(1.0F, 0.25F, 0.65F), 1.4F);
    private static final DustParticleOptions CYAN = new DustParticleOptions(new Vector3f(0.4F, 0.9F, 1.0F), 1.4F);
    private static final DustParticleOptions ORANGE = new DustParticleOptions(new Vector3f(1.0F, 0.5F, 0.1F), 1.4F);
    private static final DustParticleOptions YELLOW = new DustParticleOptions(new Vector3f(1.0F, 0.9F, 0.2F), 1.4F);

    private Spells() {}

    // ------------------------------------------------------------------ single target

    /** Rude Buster: a heavy magenta slash that hits one target and knocks it back hard. */
    public static void rudeBuster(ServerLevel level, ServerPlayer player) {
        Hit hit = raycast(level, player, 24.0);
        beam(level, hit.start(), hit.point(), PINK);
        sound(level, player, SoundEvents.PLAYER_ATTACK_STRONG, 1.2F, 0.6F);
        if (hit.entity() != null) {
            LivingEntity target = hit.entity();
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    25, 0.4, 0.4, 0.4, 0.3);
            magic(level, player, target, 12.0F);
            target.knockback(1.6, player.getX() - target.getX(), player.getZ() - target.getZ());
        }
    }

    /** Fireshock: fire damage and sets the target alight. */
    public static void fireshock(ServerLevel level, ServerPlayer player) {
        Hit hit = raycast(level, player, 24.0);
        beam(level, hit.start(), hit.point(), ORANGE);
        beam(level, hit.start(), hit.point(), ParticleTypes.FLAME);
        sound(level, player, SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
        if (hit.entity() != null) {
            LivingEntity target = hit.entity();
            target.setSecondsOnFire(8);
            magic(level, player, target, 8.0F);
        }
    }

    /** IceShock: ice damage and a deep freeze. If it kills, the target shatters like ice. */
    public static void iceshock(ServerLevel level, ServerPlayer player) {
        Hit hit = raycast(level, player, 24.0);
        beam(level, hit.start(), hit.point(), CYAN);
        beam(level, hit.start(), hit.point(), ParticleTypes.SNOWFLAKE);
        sound(level, player, SoundEvents.GLASS_BREAK, 0.8F, 1.4F);
        if (hit.entity() != null) {
            LivingEntity target = hit.entity();
            target.setTicksFrozen(target.getTicksRequiredToFreeze() + 120);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
            magic(level, player, target, 7.0F);
            if (target.isDeadOrDying()) {
                shatter(level, target);
            }
        }
    }

    /** Thunder Buster (Rude Buster + Fireshock): a slash that calls down lightning. */
    public static void thunderBuster(ServerLevel level, ServerPlayer player) {
        Hit hit = raycast(level, player, 28.0);
        beam(level, hit.start(), hit.point(), YELLOW);
        sound(level, player, SoundEvents.PLAYER_ATTACK_STRONG, 1.2F, 0.5F);

        BlockPos strikePos;
        if (hit.entity() != null) {
            strikePos = hit.entity().blockPosition();
        } else {
            strikePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(hit.point()));
        }

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(strikePos));
            bolt.setCause(player);
            level.addFreshEntity(bolt);
        }
        if (hit.entity() != null) {
            LivingEntity target = hit.entity();
            magic(level, player, target, 10.0F);
            target.knockback(1.2, player.getX() - target.getX(), player.getZ() - target.getZ());
        }
    }

    // ------------------------------------------------------------------ area

    /** Snowgrave: a blizzard where you aim. Heavy damage and freezing to everything in the area. */
    public static void snowgrave(ServerLevel level, ServerPlayer player) {
        grave(level, player, false);
    }

    /** Firegrave (Snowgrave fusion): the same, but a firestorm. */
    public static void firegrave(ServerLevel level, ServerPlayer player) {
        grave(level, player, true);
    }

    private static void grave(ServerLevel level, ServerPlayer player, boolean fire) {
        double radius = fire ? 7.0 : 6.0;
        float damage = fire ? 18.0F : 16.0F;

        Hit hit = raycast(level, player, 20.0);
        Vec3 center = hit.point();
        beam(level, hit.start(), center, fire ? ORANGE : CYAN);

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(radius, 4.0, radius),
                e -> valid(player, e) && e.position().distanceTo(center) <= radius + 1.0);

        for (LivingEntity target : targets) {
            if (fire) {
                target.setSecondsOnFire(10);
            } else {
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 200);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 3));
            }
            magic(level, player, target, damage);
            if (!fire && target.isDeadOrDying()) {
                shatter(level, target);
            }
        }

        if (fire) {
            level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 2.0, center.z, 300, radius * 0.45, 2.5, radius * 0.45, 0.05);
            level.sendParticles(ParticleTypes.LAVA, center.x, center.y + 1.0, center.z, 40, radius * 0.4, 1.0, radius * 0.4, 0.0);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1.0, center.z, 1, 0, 0, 0, 0);
            level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2F, 0.7F);
        } else {
            level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 2.0, center.z, 300, radius * 0.45, 2.5, radius * 0.45, 0.05);
            level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0, center.z, 40, radius * 0.4, 1.0, radius * 0.4, 0.02);
            level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.6F);
            level.playSound(null, BlockPos.containing(center), SoundEvents.POWDER_SNOW_BREAK, SoundSource.PLAYERS, 1.2F, 0.8F);
        }
    }

    // ------------------------------------------------------------------ Knight's Swoon

    /** Knight's Swoon (IceShock + Firegrave): dash forward and stun everything in front of you. */
    public static void knightsSwoon(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize();
        final Vec3 forward = flat;

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(8.0, 3.0, 8.0), e -> valid(player, e));

        for (LivingEntity target : targets) {
            Vec3 to = target.position().subtract(player.position());
            double dist = to.horizontalDistance();
            boolean inFront = dist < 2.5
                    || (dist <= 8.0 && forward.dot(new Vec3(to.x, 0.0, to.z).normalize()) > 0.3);
            if (!inFront) {
                continue;
            }
            // Slowness VII means the target cannot move at all.
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 6));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
            if (target instanceof Mob mob) {
                mob.setTarget(null);
            }
            magic(level, player, target, 6.0F);
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    20, 0.4, 0.5, 0.4, 0.3);
        }

        player.setDeltaMovement(forward.x * 1.9, 0.3, forward.z * 1.9);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 3, false, false));

        level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + forward.x * 2, player.getY() + 1.0, player.getZ() + forward.z * 2,
                6, 1.0, 0.3, 1.0, 0.0);
        sound(level, player, SoundEvents.ENDER_DRAGON_FLAP, 1.0F, 1.5F);
    }

    // ------------------------------------------------------------------ helpers

    private record Hit(LivingEntity entity, Vec3 start, Vec3 point) {}

    /** What the caster is looking at: the first valid living entity, or the first block, or the end of the range. */
    private static Hit raycast(ServerLevel level, ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        BlockHitResult block = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }

        AABB search = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, player, start, end, search, e -> valid(player, e) && e.isPickable());

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity living) {
            return new Hit(living, start, entityHit.getLocation());
        }
        return new Hit(null, start, end);
    }

    /** Never hurts the caster, other players, owned pets, villagers, iron golems or armor stands. */
    private static boolean valid(ServerPlayer caster, Entity e) {
        if (!(e instanceof LivingEntity living) || !living.isAlive() || e.isSpectator()) {
            return false;
        }
        if (e == caster || e instanceof Player || e instanceof AbstractVillager
                || e instanceof IronGolem || e instanceof ArmorStand) {
            return false;
        }
        return !(e instanceof TamableAnimal pet && pet.isOwnedBy(caster));
    }

    private static boolean magic(ServerLevel level, ServerPlayer caster, LivingEntity target, float amount) {
        return target.hurt(level.damageSources().indirectMagic(caster, caster), amount);
    }

    private static void beam(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 0.5) {
            return;
        }
        Vec3 direction = delta.scale(1.0 / length);
        for (double d = 1.0; d < length; d += 0.6) {
            Vec3 p = from.add(direction.scale(d));
            level.sendParticles(particle, p.x, p.y - 0.15, p.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
    }

    private static void shatter(ServerLevel level, LivingEntity entity) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                40, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);
        level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.1F);
    }

    private static void sound(ServerLevel level, ServerPlayer player, SoundEvent event, float volume, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), event, SoundSource.PLAYERS, volume, pitch);
    }
}
