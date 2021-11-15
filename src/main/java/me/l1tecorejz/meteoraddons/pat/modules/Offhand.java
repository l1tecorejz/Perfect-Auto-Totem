package me.l1tecorejz.meteoraddons.pat.modules;

import me.l1tecorejz.meteoraddons.pat.PerfectAutoTotem;
import me.l1tecorejz.meteoraddons.pat.utils.inv;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import static me.l1tecorejz.meteoraddons.pat.utils.inv.GetFirstHotbarSlotId;

public class Offhand extends Module
{
    public static Offhand instance;

    public Offhand()
    {
        super(PerfectAutoTotem.CATEGORY, "offhand",
            "Working only with auto totem in smart mode.");
        instance = this;
    }

    public void tick()
    {
        if (!isActive()) return;

        final Item
            offhand_item = mc.player.getOffHandStack().getItem(),
            mainhand_item = mc.player.getMainHandStack().getItem(),
            cursor_item = mc.player.currentScreenHandler.getCursorStack().getItem();

        if (offhand_item == Items.END_CRYSTAL || mainhand_item == Items.END_CRYSTAL) return;

        if (cursor_item == Items.END_CRYSTAL)
        {
            inv.Click(45);
            return;
        }

        final int crystal_id = GetCryId();

        if (crystal_id == -1) return;

        inv.Move(crystal_id);
    }

    private int GetCryId()
    {
        final int hotbar_start = GetFirstHotbarSlotId();
        for (int i = hotbar_start; i < hotbar_start + 9; ++i)
        {
            if (mc.player.currentScreenHandler.getSlot(i).getStack().getItem() != Items.END_CRYSTAL) continue;
            return i;
        }

        for (int i = 0; i < hotbar_start; ++i)
        {
            if (mc.player.currentScreenHandler.getSlot(i).getStack().getItem() != Items.END_CRYSTAL) continue;
            return i;
        }

        return -1;
    }
}
