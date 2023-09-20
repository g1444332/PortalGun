package me.gorgeousone.portalgun;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PortalMain extends JavaPlugin implements Listener {

	private BlockHandler blockHandler;
	private BlockDragHandler blockDragHandler;

	@Override
	public void onEnable() {

		blockHandler = new BlockHandler(this);
		blockDragHandler = new BlockDragHandler(this, blockHandler);

		registerEvents();
	}

	@Override
	public void onDisable() {
		blockDragHandler.unload();
		blockHandler.unload();
	}

	private void registerEvents() {

		PluginManager manager = Bukkit.getPluginManager();

		manager.registerEvents(new FallingBlockLand(blockHandler), this);
		manager.registerEvents(new LocationBlock(blockHandler,this), this);
	}
}