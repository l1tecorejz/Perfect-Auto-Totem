package me.l1tecorejz.meteoraddons.pat.modules;

import me.l1tecorejz.meteoraddons.pat.PerfectAutoTotem;
import me.l1tecorejz.meteoraddons.pat.utils.inv;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IExplosion;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.atomic.AtomicBoolean;

public class AutoTotem extends Module
{
    public static AutoTotem instance;

    public AutoTotem()
    {
        super(PerfectAutoTotem.CATEGORY, "auto-pop-it", "Best auto totem for 1.17+");
        instance = this;
        Validate.notNull(Offhand.instance);
    }

    //

    @EventHandler
    private void tick(TickEvent.Pre event)
    {
        if (delay_ticks_left > 0)
        {
            --delay_ticks_left;
            return;
        }

        if (cfg_anti_bow_kick.get() && mc.player.isUsingItem()) return;

        if (mc.player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler) return;

        if (Offhand.instance.isActive() && SmartCheck())
        {
            if (!(mc.currentScreen instanceof HandledScreen) && mc.player.currentScreenHandler instanceof PlayerScreenHandler)
                Offhand.instance.tick();
            return;
        }

        ItemStack
            offhand_stack = mc.player.getInventory().getStack(40),
            cursor_stack = mc.player.currentScreenHandler.getCursorStack();

        final boolean
            is_holding_totem = cursor_stack.getItem() == Items.TOTEM_OF_UNDYING,
            is_totem_in_offhand = offhand_stack.getItem() == Items.TOTEM_OF_UNDYING,
            can_click_offhand = mc.player.currentScreenHandler instanceof PlayerScreenHandler;

        if (cfg_masturbation.get() && (!(mc.currentScreen instanceof HandledScreen) || !isClassic()))
        {
            if (cfg_masturbation_delay.get() == 0 || (mc.player.age % cfg_masturbation_delay.get()) == 0)
                should_override_totem = true;
        }

        if (is_totem_in_offhand && !should_override_totem) return;

        final int totem_id = GetTotemId();
        if (totem_id == -1 && !is_holding_totem) return;

        if (is_holding_totem && can_click_offhand)
        {
            inv.Click(45);
            return;
        }

        if (!can_click_offhand)
        {
            if (isClassic())
            {
                ItemStack mainhand_stack = mc.player.getInventory().getStack(selected_slot);
                if (mainhand_stack.getItem() == Items.TOTEM_OF_UNDYING)
                {
                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket
                        (PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                    mc.player.setStackInHand(Hand.OFF_HAND, mc.player.getInventory().getStack(selected_slot));
                    mc.player.getInventory().setStack(selected_slot, offhand_stack);
                    mc.player.clearActiveItem();
                    return;
                }

                if (is_holding_totem)
                    inv.Click(inv.GetFirstHotbarSlotId() + selected_slot);

                should_override_totem = false;
            }
            else
            {
                if (totem_id == -1 && is_holding_totem)
                {
                    for (Slot slot : mc.player.currentScreenHandler.slots)
                    {
                        if (!slot.getStack().isEmpty()) continue;
                        inv.Click(slot.id);
                        return;
                    }

                    inv.Click(inv.GetFirstHotbarSlotId() + selected_slot);
                    return;
                }
            }
        }

        inv.Move(totem_id);

        should_override_totem = !is_totem_in_offhand && ShouldOverrideTotem();
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Sent event)
    {
        if (event.packet instanceof ClickSlotC2SPacket)
        {
            delay_ticks_left = cfg_action_delay.get();

            if (isClassic())    // prevent gap disease
                mc.player.stopUsingItem();
        }
        else if (event.packet instanceof PlayerActionC2SPacket packet)
        {
            if (packet.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND)
                delay_ticks_left = cfg_action_delay.get();
        }
        if (event.packet instanceof UpdateSelectedSlotC2SPacket packet)
        {
            selected_slot = packet.getSelectedSlot();
        }
    }

    @EventHandler
    private void onPacketReceived(PacketEvent.Receive event)
    {
        if (event.packet instanceof EntityStatusS2CPacket packet)
        {
            if (packet.getStatus() != 35 || packet.id != mc.player.getId()) return;

            if (mc.player.currentScreenHandler instanceof PlayerScreenHandler) return;

            if (cfg_close_screen.get())
                mc.player.closeHandledScreen();

            ItemStack mainhand_stack = mc.player.getInventory().getStack(selected_slot);
            if (mainhand_stack.getItem() == Items.TOTEM_OF_UNDYING)
            {
                mainhand_stack.decrement(1);
                return;
            }

            ItemStack offhand_stack = mc.player.getOffHandStack();
            if (offhand_stack.getItem() == Items.TOTEM_OF_UNDYING)
                offhand_stack.decrement(1);
        }
        else if (event.packet instanceof UpdateSelectedSlotS2CPacket packet)
        {
            // TODO: fix small desync
            selected_slot = packet.getSlot();
        }
        else if (event.packet instanceof CloseScreenS2CPacket)
        {
            should_override_totem = true;
        }
    }

    @Override
    public void onActivate()
    {
        should_override_totem = true;
        selected_slot = mc.player.getInventory().selectedSlot;
    }

    //

    public static boolean isClassic()
    {
        return instance.cfg_version.get() == Versions.one_dot_12;
    }

    private int GetTotemId()
    {
        final int hotbar_start = inv.GetFirstHotbarSlotId();
        for (int i = hotbar_start; i < hotbar_start + 9; ++i)
        {
            if (mc.player.currentScreenHandler.getSlot(i).getStack().getItem() != Items.TOTEM_OF_UNDYING) continue;
            return i;
        }

        for (int i = 0; i < hotbar_start; ++i)
        {
            if (mc.player.currentScreenHandler.getSlot(i).getStack().getItem() != Items.TOTEM_OF_UNDYING) continue;
            return i;
        }

        return -1;
    }

    private boolean ShouldOverrideTotem()
    {
        return cfg_version.get() != Versions.one_dot_17 ||
            (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler) && cfg_version.get() == Versions.one_dot_17);
    }

    private static final double cry_damage = (float)((int)((1D + 1D) / 2.0D * 7.0D * 12.0D + 1.0D));
    private static final Explosion explosion = new Explosion
        (null, null, 0, 0, 0, 6.0F, false, Explosion.DestructionType.DESTROY);
    private boolean SmartCheck()    // TODO: check wither explosion damage too
    {
        if (mc.player.isFallFlying()) return false; // TODO: return false only when speed is enough too pop totem
        if (GetLatency() >= 125) return false;  // TODO: assume TPS: 2.5 * interval_per_tick instead of 125

        float health = GetHealth();
        if (health < 10.0F) return false;

        // TODO: fix delayed fall damage
        if (mc.player.fallDistance > 3.f && health - mc.player.fallDistance * 0.5 <= 2.0F) return false;

        double resistance_coefficient = 1.d;

        var resistance_effect = mc.player.getStatusEffect(StatusEffects.RESISTANCE);

        if (resistance_effect != null)
        {
            resistance_coefficient -= (resistance_effect.getAmplifier() + 1) * 0.2;
            if (resistance_coefficient <= 0.d) return true;
        }

        double damage = cry_damage;

        switch (mc.world.getDifficulty())
        {
            case EASY -> damage = damage * 0.5d + 1.0d;
            case HARD -> damage *= 1.5d;
        }

        damage *= resistance_coefficient;

        EntityAttributeInstance attribute_instance = mc.player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);

        float f = 2.0F + (float) attribute_instance.getValue() / 4.0F;
        float g = (float) MathHelper.clamp((float) mc.player.getArmor() - damage / f,
            (float) mc.player.getArmor() * 0.2F, 20.0F);
        damage *= 1 - g / 25.0F;

        // Reduce by enchants
        ((IExplosion) explosion).set(mc.player.getPos(), 6.0F, false);

        int protLevel = EnchantmentHelper.getProtectionAmount(mc.player.getArmorItems(), DamageSource.explosion(explosion));
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);

        return health - damage > 2.0F;
    }

    private float GetHealth()
    {
        float health = mc.player.getHealth();

        if (mc.player.getStatusEffect(StatusEffects.ABSORPTION) != null)
            health += mc.player.getAbsorptionAmount();

        return health;
    }

    private long GetLatency()
    {
        PlayerListEntry playerListEntry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        return playerListEntry != null ? playerListEntry.getLatency() : 0L;
    }

    // vars

    //private final AtomicBoolean should_wait_next_tick = new AtomicBoolean(false);
    private boolean should_override_totem;
    private int selected_slot = 0;
    private int delay_ticks_left = 0;

    // settings

    private final SettingGroup sg_general = settings.getDefaultGroup();

    public enum Versions
    {
        one_dot_12("classic"),
        one_dot_16("1.16.5"),
        one_dot_17("1.17+");

        Versions(String name) {this.name = name;}

        String name;

        @Override public String toString() {return name;}
    }

    private final Setting<Versions> cfg_version = sg_general.add(new EnumSetting.Builder<Versions>()
        .name("mode")
        .defaultValue(Versions.one_dot_17)
        .build());

    private final Setting<Boolean> cfg_close_screen = sg_general.add(new BoolSetting.Builder()
        .name("close-screen-on-pop")
        .description("Closes any screen handler while putting totem in offhand.")
        .defaultValue(false)
        .build());

    private final Setting<Integer> cfg_action_delay = sg_general.add(new IntSetting.Builder()
        .name("action-delay")
        .defaultValue(0)
        .build());

    private final Setting<Boolean> cfg_anti_bow_kick = sg_general.add(new BoolSetting.Builder()
        .name("anti-bow-kick")
        .visible(() -> cfg_version.get() == Versions.one_dot_12)
        .defaultValue(true)
        .build());

    private final Setting<Boolean> cfg_masturbation = sg_general.add(new BoolSetting.Builder()
        .name("totem-spam")
        .defaultValue(false)
        .build());

    private final Setting<Integer> cfg_masturbation_delay = sg_general.add(new IntSetting.Builder()
        .name("spam-delay")
        .visible(() -> cfg_masturbation.get())
        .defaultValue(0)
        .build());
}
