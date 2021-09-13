package meteordevelopment.addons.L1tE.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@SuppressWarnings("ConstantConditions")
public class inv
{
    private static final MinecraftClient mc = MinecraftClient.getInstance();

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
