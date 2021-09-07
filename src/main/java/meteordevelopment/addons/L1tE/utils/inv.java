package meteordevelopment.addons.L1tE.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class inv
{
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static void ClickSlot(int id, int button, SlotActionType action)
    {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, button, action, mc.player);
    }

    public static void Click(int id)
    {
        ClickSlot(id, 0, SlotActionType.PICKUP);
    }

    public static void Swap(int id, int button)
    {
        ClickSlot(id, button, SlotActionType.SWAP);
    }

    public static int Idx2Id(int idx)
    {
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
}
