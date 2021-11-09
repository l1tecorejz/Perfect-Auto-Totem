package me.l1tecorejz.meteoraddons.pat.utils;

import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import static meteordevelopment.meteorclient.utils.Utils.mc;

public class inv
{
    private static void ClickSlot(int id, int button, SlotActionType action)
    {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, button, action, mc.player);
    }

    public static void Click(int id) {ClickSlot(id, 0, SlotActionType.PICKUP);}

    public static void Swap(int id, int button) {ClickSlot(id, button, SlotActionType.SWAP);}

    public static int GetFirstHotbarSlotId()
    {
        if (mc.player.currentScreenHandler instanceof PlayerScreenHandler) return 36;
        return mc.player.currentScreenHandler.slots.size() - 9;
    }
}
