package meteordevelopment.addons.L1tE.modules;

import meteordevelopment.addons.L1tE.AddonByL1tE;
import meteordevelopment.meteorclient.systems.modules.Module;

public class Offhand extends Module
{
    public static Offhand instance;
    
    public Offhand()
    {
        super(AddonByL1tE.CATEGORY, "offhand",
            "Any other offhand module wont work with perfect auto totem.");
        instance = this;
    }

    // coming soon...

    public void Do()
    {
        if (!isActive()) return;


    }
}
