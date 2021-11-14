package me.l1tecorejz.meteoraddons.pat;

import me.l1tecorejz.meteoraddons.pat.modules.AutoTotem;
import me.l1tecorejz.meteoraddons.pat.modules.Offhand;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class PerfectAutoTotem extends MeteorAddon
{
    public static final Logger LOG = LogManager.getLogger();

	@Override public void onInitialize()
    {
        LOG.info("Initializing Perfect Auto Totem");

		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("me.l1tecorejz.meteoraddons.pat", (lookupInMethod, klass) ->
            (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		// Modules
        Modules.get().add(new Offhand());
		Modules.get().add(new AutoTotem());
	}

    public static final Category CATEGORY = new Category("l1tecorejz'");

	@Override public void onRegisterCategories() {Modules.registerCategory(CATEGORY);}
}
