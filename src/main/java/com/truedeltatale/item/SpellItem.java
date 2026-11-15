package com.truedeltatale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A right-click spell with a cooldown. If the item has durability, each cast uses one point. */
public class SpellItem extends Item {

    @FunctionalInterface
    public interface Spell {
        void cast(ServerLevel level, ServerPlayer player);
    }

    private final Spell spell;
    private final int cooldownTicks;
    private final boolean foil;

    public SpellItem(Properties properties, int cooldownTicks, boolean foil, Spell spell) {
        super(properties);
        this.spell = spell;
        this.cooldownTicks = cooldownTicks;
        this.foil = foil;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // A spent item is not destroyed, so it can still be repaired at an anvil.
        if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.truedeltatale.spent"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            spell.cast(serverLevel, serverPlayer);
            player.getCooldowns().addCooldown(this, cooldownTicks);
            player.awardStat(Stats.ITEM_USED.get(this));
            if (stack.isDamageableItem()) {
                stack.setDamageValue(stack.getDamageValue() + 1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return foil || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
}
