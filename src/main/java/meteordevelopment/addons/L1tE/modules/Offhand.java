package meteordevelopment.addons.L1tE.modules;

import meteordevelopment.addons.L1tE.AddonByL1tE;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.lang3.Validate;

public class Offhand extends Module
{
    public static Offhand instance;

    public Offhand()
    {
        super(AddonByL1tE.CATEGORY, "offhand",
            "Working only with auto totem in smart mode.");
        instance = this;
    }

    public void Do()
    {
        Validate.notNull(mc.player);
        Validate.notNull(mc.interactionManager);

        if (!isActive()) return;

        final Item
            offhand_item = mc.player.getOffHandStack().getItem(),
            mainhand_item = mc.player.getMainHandStack().getItem();

        // TODO: swordfag

        if (offhand_item == Items.END_CRYSTAL || mainhand_item == Items.END_CRYSTAL) return;

        int crystal_id = -1;

        for (Slot slot : mc.player.currentScreenHandler.slots)
        {
            Item item = slot.getStack().getItem();
            if (item != Items.END_CRYSTAL) continue;

            crystal_id = slot.id;
            break;
        }

        if (crystal_id == -1) return;

        // TODO: add 1.12.2 mode

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
            crystal_id, 40, SlotActionType.SWAP, mc.player);
    }
}
