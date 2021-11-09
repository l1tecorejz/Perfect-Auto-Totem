package me.l1tecorejz.meteoraddons.pat.modules;

import me.l1tecorejz.meteoraddons.pat.utils.inv;
import me.l1tecorejz.meteoraddons.pat.PerfectAutoTotem;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

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

        /*if (mainhand_item instanceof SwordItem && cfg_swordfag.get())
        {
            if (offhand_item instanceof EnchantedGoldenAppleItem) return;

            if (cursor_item instanceof EnchantedGoldenAppleItem)
            {
                Click(45);
                return;
            }

            int egap_id = -1, gap_id = -1;

            for (Slot slot : mc.player.currentScreenHandler.slots)
            {
                Item item = slot.getStack().getItem();
                if (item instanceof EnchantedGoldenAppleItem)
                {
                    egap_id = slot.id;
                    break;
                }

                if (gap_id == -1 && item == Items.GOLDEN_APPLE) gap_id = slot.id;
            }

            if (egap_id == -1)
            {
                if (cursor_item == Items.GOLDEN_APPLE) Click(45);
                else if (gap_id != -1) Move(gap_id);

                return;
            }

            Move(egap_id);
            return;
        }*/

        if (offhand_item == Items.END_CRYSTAL || mainhand_item == Items.END_CRYSTAL) return;

        if (cursor_item == Items.END_CRYSTAL)
        {
            inv.Click(45);
            return;
        }

        int crystal_id = -1;

        for (Slot slot : mc.player.currentScreenHandler.slots)
        {
            Item item = slot.getStack().getItem();
            if (item != Items.END_CRYSTAL) continue;

            crystal_id = slot.id;
            break;
        }

        if (crystal_id == -1) return;

        Move(crystal_id);
    }

    private void Move(int id)
    {
        if (AutoTotem.instance.cfg_version.get() == AutoTotem.Versions.one_dot_12) inv.Click(id);
        else inv.Swap(id, 40);
    }

    //

    /*private final SettingGroup sg_general = settings.getDefaultGroup();

    private final Setting<Boolean> cfg_swordfag = sg_general.add(new BoolSetting.Builder()
        .name("swordfag-helper")
        .description("Puts gapples in ur offhand if u r using sword.")
        .defaultValue(false)
        .build()
    );*/
}
