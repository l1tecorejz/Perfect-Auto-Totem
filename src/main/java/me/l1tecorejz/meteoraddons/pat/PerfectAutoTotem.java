package me.l1tecorejz.meteoraddons.pat;

import me.l1tecorejz.meteoraddons.pat.modules.AutoTotem;
import me.l1tecorejz.meteoraddons.pat.modules.Offhand;
import meteordevelopment.meteorclient.MeteorAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;

import java.lang.invoke.MethodHandles;

public class PerfectAutoTotem extends MeteorAddon
{
	@Override public void onInitialize()
    {
		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("me.l1tecorejz.meteoraddons.pat", (lookupInMethod, klass) ->
            (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		// Modules
        Modules.get().add(new Offhand());
		Modules.get().add(new AutoTotem());
	}

    public static final Category CATEGORY = new Category("l1tecorejz'", Items.TOTEM_OF_UNDYING.getDefaultStack());

	@Override public void onRegisterCategories() {Modules.registerCategory(CATEGORY);}
}
