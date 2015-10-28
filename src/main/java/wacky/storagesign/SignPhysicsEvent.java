package wacky.storagesign;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class SignPhysicsEvent implements Listener {

	StorageSignCore plugin;

    public SignPhysicsEvent(StorageSignCore plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(plugin.isStorageSign(event.getBlock()));
    }
}
