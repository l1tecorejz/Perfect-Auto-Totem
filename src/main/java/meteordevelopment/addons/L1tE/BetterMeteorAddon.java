package meteordevelopment.addons.L1tE;

import meteordevelopment.addons.L1tE.modules.AutoTotem;
import meteordevelopment.addons.L1tE.modules.Offhand;
import meteordevelopment.meteorclient.MeteorAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;

import java.lang.invoke.MethodHandles;

public class BetterMeteorAddon extends MeteorAddon
{
    public static final Category CATEGORY = new Category("l1tecorejz'",
        Items.TOTEM_OF_UNDYING.getDefaultStack());

	@Override public void onInitialize()
    {
		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("meteordevelopment.addons.L1tE", (lookupInMethod, klass) ->
            (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		// Modules
        Modules.get().add(new Offhand());
		Modules.get().add(new AutoTotem());
	}

	@Override public void onRegisterCategories() {Modules.registerCategory(CATEGORY);}
}
