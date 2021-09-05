package meteordevelopment.addons.L1tE.modules;

import meteordevelopment.addons.L1tE.AddonByL1tE;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IExplosion;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoTotem extends Module
{
    public AutoTotem()
    {
        super(AddonByL1tE.CATEGORY, "auto-pop-it", "Simplest and best auto totem for 1.17+");
    }

    //

    @EventHandler private void onTickPre(TickEvent.Pre event)
    {
        Validate.notNull(mc.player);
        Validate.notNull(mc.interactionManager);

        if (mc.player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler) return;

        if (should_wait_next_tick.getAndSet(false)) return;

        ItemStack
            offhand_stack = mc.player.getInventory().getStack(40),
            cursor_stack = mc.player.currentScreenHandler.getCursorStack();

        final boolean
            is_holding_totem = cursor_stack.getItem() == Items.TOTEM_OF_UNDYING,
            can_click_offhand = mc.player.currentScreenHandler instanceof PlayerScreenHandler;

        if (offhand_stack.getItem() == Items.TOTEM_OF_UNDYING)
        {
            if (should_override_totem && cfg_version.get() == Versions.one_dot_16)
            {
                final int totem_id = GetTotemId();
                if (totem_id == -1) return;

                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                    totem_id, 40, SlotActionType.SWAP, mc.player);

                should_override_totem = false;
                return;
            }

            if (!(mc.currentScreen instanceof HandledScreen) && is_holding_totem)
            {
                for (int i = 0; i < 36; ++i)
                {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (!stack.isEmpty()) continue;

                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                        Idx2Id(i), 0, SlotActionType.PICKUP, mc.player);
                    return;
                }
            }

            return;
        }

        if (cfg_smart.get() && SmartCheck())
        {
            Offhand.instance.Do();
            return;
        }

        if (is_holding_totem && can_click_offhand)
        {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0,
                SlotActionType.PICKUP, mc.player);
            return;
        }

        if (cfg_version.get() == Versions.one_dot_12 && !can_click_offhand)
        {
            ItemStack mainhand_stack = mc.player.getInventory().getStack(selected_slot);
            if (mainhand_stack.getItem() == Items.TOTEM_OF_UNDYING)
            {
                if (cfg_close_screen.get()) mc.player.closeHandledScreen();
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket
                    (PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                return;
            }

            if (is_holding_totem)
            {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                    selected_slot + mc.player.currentScreenHandler.slots.size() - 9,
                    0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }

        final int totem_id = GetTotemId();
        if (totem_id == -1)
        {
            if (is_holding_totem)
            {
                for (int i = 0; i < 36; ++i)
                {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (!stack.isEmpty()) continue;

                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                        Idx2Id(i), 0, SlotActionType.PICKUP, mc.player);
                    return;
                }

                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                    selected_slot + mc.player.currentScreenHandler.slots.size() - 9,
                    0, SlotActionType.PICKUP, mc.player);
            }
            return;
        }

        if (cfg_version.get() == Versions.one_dot_12)
        {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                totem_id, 0, SlotActionType.PICKUP, mc.player);
            return;
        }

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
            totem_id, 40, SlotActionType.SWAP, mc.player);

        should_override_totem = true;
    }

    @EventHandler private void onEzLog(GameLeftEvent event)
    {
        Validate.notNull(mc.player);
        Validate.notNull(mc.interactionManager);

        int totem_id = GetTotemId();
        if (totem_id == -1) return;

        if (cfg_version.get() == Versions.one_dot_12)
        {
            // TODO
            return;
        }

        // TODO: make this shit smarter

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
            totem_id, selected_slot, SlotActionType.SWAP, mc.player);

        totem_id = GetTotemId();
        if (totem_id == -1) return;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
            totem_id, 40, SlotActionType.SWAP, mc.player);
    }

    @EventHandler private void onPacketSent(PacketEvent.Sent event)
    {
        if (event.packet instanceof ClickSlotC2SPacket)
        {
            // TODO: wait after some PlayerActionC2SPacket too?
            should_wait_next_tick.set(true);
            return;
        }

        if (event.packet instanceof UpdateSelectedSlotC2SPacket packet)
        {
            selected_slot = packet.getSelectedSlot();
        }
    }

    @EventHandler private void onPacketReceived(PacketEvent.Receive event)
    {
        if (event.packet instanceof UpdateSelectedSlotS2CPacket packet)
        {
            // TODO: fix small desync
            selected_slot = packet.getSlot();
        }
    }

    @Override public void onActivate()
    {
        Validate.notNull(mc.player);

        should_override_totem = true;
        selected_slot = mc.player.getInventory().selectedSlot;

        super.onActivate();
    }

    //

    private int GetTotemId()
    {
        Validate.notNull(mc.player);

        for (int i = 0; i < 45; ++i)
        {
            if (i == 40) continue;
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (item != Items.TOTEM_OF_UNDYING) continue;
            return Idx2Id(i);
        }

        for (Slot slot : mc.player.currentScreenHandler.slots)
        {
            if (slot.id == 45) continue;
            if (slot.getStack().getItem() != Items.TOTEM_OF_UNDYING) continue;
            return slot.id;
        }

        return -1;
    }

    private int Idx2Id(int idx)
    {
        Validate.notNull(mc.player);

        if (mc.player.currentScreenHandler instanceof PlayerScreenHandler)
        {
            if (PlayerInventory.isValidHotbarIndex(idx)) return idx + 36;
            if (idx >= 9 && idx < 36) return idx;
        }
        else
        {
            if (PlayerInventory.isValidHotbarIndex(idx)) return idx + mc.player.currentScreenHandler.slots.size() - 9;
            if (idx >= 9 && idx < 36) return idx + mc.player.currentScreenHandler.slots.size() - 45;
        }

        return -1;
    }

    private static final double cry_damage = (float)((int)((1 + 1) / 2.0D * 7.0D * 12.0D + 1.0D));
    private static final Explosion explosion = new Explosion
        (null, null, 0, 0, 0, 6.0F, false, Explosion.DestructionType.DESTROY);
    private boolean SmartCheck()
    {
        Validate.notNull(mc.player);
        Validate.notNull(mc.world);

        if (mc.player.isFallFlying()) return false; // TODO: return false only when speed is enough too pop totem
        if (GetLatency() >= 125) return false;  // TODO: assume TPS: 2.5 * interval_per_tick instead of 125

        float health = GetHealth();
        if (health < 10.f) return false;

        if(mc.player.fallDistance > 3.f && health - mc.player.fallDistance * 0.5 <= 1.f) return false;

        double resistance_coefficient = 1.d;
        if (mc.player.hasStatusEffect(StatusEffects.RESISTANCE))
        {
            resistance_coefficient -= ((Objects.requireNonNull
                    (mc.player.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 0.2);

            if (resistance_coefficient <= 0.d) return true;
        }

        double damage = cry_damage;

        switch (mc.world.getDifficulty())
        {
            case EASY -> damage = damage * 0.5d + 1.0d;
            case HARD -> damage *= 1.5d;
        }

        damage *= resistance_coefficient;

        float f = 2.0F + (float) Objects.requireNonNull
            (mc.player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue() / 4.0F;
        float g = (float) MathHelper.clamp((float) mc.player.getArmor() - damage / f,
            (float) mc.player.getArmor() * 0.2F, 20.0F);
        damage *= 1 - g / 25.0F;

        // Reduce by enchants
        ((IExplosion) explosion).set(mc.player.getPos(), 6.0F, false);

        int protLevel =
            EnchantmentHelper.getProtectionAmount(mc.player.getArmorItems(), DamageSource.explosion(explosion));
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);

        return health - damage > 1.f;
    }

    private float GetHealth()
    {
        Validate.notNull(mc.player);
        return mc.player.getHealth() + mc.player.getAbsorptionAmount(); // TODO: fix ghost absorption
    }

    private long GetLatency()   // TODO: need more accurate latency calculation
    {
        Validate.notNull(mc.player);
        PlayerListEntry playerListEntry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        return playerListEntry != null ? playerListEntry.getLatency() : 0L;
    }

    // vars

    private final AtomicBoolean should_wait_next_tick = new AtomicBoolean(false);
    private boolean should_override_totem;
    private int selected_slot = 0;

    // settings

    private final SettingGroup sg_general = settings.getDefaultGroup();

    public enum Versions
    {
        one_dot_12,
        one_dot_16,
        one_dot_17
    }

    private final Setting<Versions> cfg_version = sg_general.add(new EnumSetting.Builder<Versions>()
        .name("minecraft-version")
        .description("Rly best only on 1.17+!")
        .defaultValue(Versions.one_dot_17)
        .build()
    );

    private final Setting<Boolean> cfg_close_screen = sg_general.add(new BoolSetting.Builder()
        .name("close-screen")
        .visible(() -> cfg_version.get() == Versions.one_dot_12)
        .description("Closes any screen while putting totem in offhand.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> cfg_smart = sg_general.add(new BoolSetting.Builder()
        .name("smart")
        .description("Allows you to use offhand when you have enough hp.")
        .defaultValue(false)
        .build()
    );
}