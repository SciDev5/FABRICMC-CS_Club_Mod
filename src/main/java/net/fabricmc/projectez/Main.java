package net.fabricmc.projectez;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.projectez.event.Event;
import net.fabricmc.projectez.event.client.render.hud.InGameHudRenderEvent;
import net.fabricmc.projectez.gui.SettingsGui;
import net.fabricmc.projectez.mods.*;
import net.fabricmc.projectez.mods.settings.ModSettings;
import net.fabricmc.projectez.util.ArrayListSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class Main implements ModInitializer {

	public static final String MOD_ID = "projectez";
	public static final String MOD_NAME = "Project E.Z.";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final Set<Mod> mods = new ArrayListSet<>();

	private static Main instance;
	private static final KeyBinding MOD_SETTINGS_KEY = new KeyBinding("projectez.settings.key",78,MOD_ID);

	@Override
	public void onInitialize() {
		if (instance != null) throw new IllegalStateException("MOD "+MOD_ID+" AKA. '"+MOD_NAME+"' ALREADY INITIALIZED");
		instance = this;

		LOGGER.info("INIT "+MOD_NAME);

		mods.add(new LightLevelDisplayMod());
		mods.add(new ArmorHUDMod());
		mods.add(new PotionHUDMod());
		mods.add(new FullBrightMod());
		mods.add(new SimpleZoomMod());

		for (Mod mod : mods) mod.init();
		for (Mod mod : mods) mod.setEnabled(false);

		KeyBindingHelper.registerKeyBinding(MOD_SETTINGS_KEY);

		registerEvents();
	}

	private void registerEvents() {
		ClientTickEvents.START_CLIENT_TICK.register(this::onPreTick);
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
		HudRenderCallback.EVENT.register((matrixStack,tickDelta)-> Event.call(new InGameHudRenderEvent(matrixStack,tickDelta)));
	}

	protected void onPreTick(MinecraftClient mc) {
		ModSettings.updateCustomKeybindings();
	}
	protected void onTick(MinecraftClient mc) {
		if (mc.currentScreen == null && mc.world != null) {
			if (MOD_SETTINGS_KEY.wasPressed())
				mc.openScreen(new SettingsGui());

			for (Mod mod : mods)
				if (mod.getSettings().getToggleKey().wasPressed())
					mod.setEnabled(!mod.getEnabled());
		}
	}

}
