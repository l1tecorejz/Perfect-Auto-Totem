package meteordevelopment.addons.L1tE.modules;

import meteordevelopment.addons.L1tE.BetterMeteorAddon;
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
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.atomic.AtomicBoolean;

import static meteordevelopment.addons.L1tE.utils.inv.*;

@SuppressWarnings("ConstantConditions")
public class AutoTotem extends Module
{
    public static AutoTotem instance;

    public AutoTotem()
    {
        super(BetterMeteorAddon.CATEGORY, "auto-pop-it", "Best auto totem for 1.17+");
        instance = this;
        Validate.notNull(Offhand.instance);
    }

    //

    @EventHandler private void onTickPre(TickEvent.Pre event)
    {
        if (mc.player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler) return;

        if (should_wait_next_tick.getAndSet(false)) return;

        if (Offhand.instance.isActive() && SmartCheck())
        {
            if (!(mc.currentScreen instanceof HandledScreen) &&
                mc.player.currentScreenHandler instanceof PlayerScreenHandler)
                Offhand.instance.Do();
            return;
        }

        ItemStack
            offhand_stack = mc.player.getInventory().getStack(40),
            cursor_stack = mc.player.currentScreenHandler.getCursorStack();

        final boolean
            is_holding_totem = cursor_stack.getItem() == Items.TOTEM_OF_UNDYING,
            is_totem_in_offhand = offhand_stack.getItem() == Items.TOTEM_OF_UNDYING;
        boolean can_click_offhand = mc.player.currentScreenHandler instanceof PlayerScreenHandler;

        if (is_totem_in_offhand && !ShouldOverrideTotem())
        {
            if (!(mc.currentScreen instanceof HandledScreen) &&
                (should_click_blank || (cfg_version.get() != Versions.one_dot_12 && is_holding_totem)))
            {
                should_click_blank = false;

                for (Slot slot : mc.player.currentScreenHandler.slots)
                {
                    if (!slot.getStack().isEmpty()) continue;
                    Click(slot.id);
                    return;
                }
            }

            return;
        }

        final int totem_id = GetTotemId();
        if (totem_id == -1 && !is_holding_totem) return;

        if (!can_click_offhand && cfg_close_screen.get())
        {
            mc.player.closeHandledScreen();
            can_click_offhand = true;
        }

        if (is_holding_totem && can_click_offhand)
        {
            Click(45);
            return;
        }

        if (cfg_version.get() == Versions.one_dot_12 && !can_click_offhand)
        {
            ItemStack mainhand_stack = mc.player.getInventory().getStack(selected_slot);
            if (mainhand_stack.getItem() == Items.TOTEM_OF_UNDYING)
            {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket
                    (PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                return;
            }

            if (is_holding_totem)
            {
                Click(GetFirstHotbarSlotId() + selected_slot);
                return;
            }
        }

        if (totem_id == -1)
        {
            if (is_holding_totem)
            {
                for (Slot slot : mc.player.currentScreenHandler.slots)
                {
                    if (!slot.getStack().isEmpty()) continue;
                    Click(slot.id);
                    return;
                }

                Click(GetFirstHotbarSlotId() + selected_slot);
            }
            return;
        }

        if (cfg_version.get() == Versions.one_dot_12)
        {
            Click(totem_id);
            should_click_blank = true;
            return;
        }

        Swap(totem_id, 40);

        should_override_totem = !is_totem_in_offhand;
    }

    @EventHandler private void onEzLog(GameLeftEvent event)
    {
        int totem_id = GetTotemId();
        if (totem_id == -1) return;

        if (cfg_version.get() == Versions.one_dot_12)
        {
            // TODO
            return;
        }

        // TODO: make this shit smarter

        Swap(totem_id, selected_slot);

        totem_id = GetTotemId();
        if (totem_id == -1) return;

        Swap(totem_id, 40);
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
        if (event.packet instanceof EntityStatusS2CPacket packet)
        {
            if (mc.player.currentScreenHandler instanceof PlayerScreenHandler) return;
            if (packet.getStatus() != 35 || packet.getEntity(mc.world) != mc.player) return;

            if (mc.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING && // inaccurate
                mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING)
                mc.player.getOffHandStack().decrement(1);
        }
        else if (event.packet instanceof UpdateSelectedSlotS2CPacket packet)
        {
            // TODO: fix small desync
            selected_slot = packet.getSlot();
        }
        else if (event.packet instanceof OpenScreenS2CPacket || event.packet instanceof CloseScreenS2CPacket)
        {
            should_override_totem = true;
        }
    }

    @Override public void onActivate()
    {
        should_override_totem = true;
        selected_slot = mc.player.getInventory().selectedSlot;

        super.onActivate();
    }

    //

    private int GetTotemId()
    {
        final int hotbar_start = GetFirstHotbarSlotId();
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
        return should_override_totem && (cfg_version.get() == Versions.one_dot_16 ||
            (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler) &&
                cfg_version.get() == Versions.one_dot_17));
    }

    private static final double cry_damage = (float)((int)((1 + 1) / 2.0D * 7.0D * 12.0D + 1.0D));
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
        if (mc.player.hasStatusEffect(StatusEffects.RESISTANCE))
        {
            resistance_coefficient -= (mc.player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 0.2;
            if (resistance_coefficient <= 0.d) return true;
        }

        double damage = cry_damage;

        switch (mc.world.getDifficulty())
        {
            case EASY -> damage = damage * 0.5d + 1.0d;
            case HARD -> damage *= 1.5d;
        }

        damage *= resistance_coefficient;

        EntityAttributeInstance attribute_instance =
            mc.player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);

        float f = 2.0F + (float) attribute_instance.getValue() / 4.0F;
        float g = (float) MathHelper.clamp((float) mc.player.getArmor() - damage / f,
            (float) mc.player.getArmor() * 0.2F, 20.0F);
        damage *= 1 - g / 25.0F;

        // Reduce by enchants
        ((IExplosion) explosion).set(mc.player.getPos(), 6.0F, false);

        int protLevel =
            EnchantmentHelper.getProtectionAmount(mc.player.getArmorItems(), DamageSource.explosion(explosion));
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);

        return health - damage > 2.0F;
    }

    private float GetHealth()
    {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount(); // TODO: fix ghost absorption
    }

    private long GetLatency()   // TODO: need more accurate latency calculation
    {
        PlayerListEntry playerListEntry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        return playerListEntry != null ? playerListEntry.getLatency() : 0L;
    }

    // vars

    private final AtomicBoolean should_wait_next_tick = new AtomicBoolean(false);
    private boolean should_override_totem, should_click_blank;
    private int selected_slot = 0;

    // settings

    private final SettingGroup sg_general = settings.getDefaultGroup();

    public enum Versions
    {
        one_dot_12,
        one_dot_16,
        one_dot_17
    }

    public final Setting<Versions> cfg_version = sg_general.add(new EnumSetting.Builder<Versions>()
        .name("minecraft-server-version")
        .defaultValue(Versions.one_dot_17)
        .build());

    private final Setting<Boolean> cfg_close_screen = sg_general.add(new BoolSetting.Builder()
        .name("close-screen")
        .description("Closes any screen handler while putting totem in offhand.")
        .defaultValue(false)
        .build());
}
