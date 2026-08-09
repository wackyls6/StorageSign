package wacky.storagesign;

import com.github.teruteru128.logger.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class SignPhysicsEvent implements Listener {

  StorageSignCore plugin;
  private Logger _logger;

  public SignPhysicsEvent(StorageSignCore plugin, Logger logger) {
    this.plugin = plugin;
    this._logger = logger;

    _logger.debug("SignPhysicsEvent:Start");
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onBlockPhysics(BlockPhysicsEvent event) {
    _logger.debug("onBlockPhysics:Start");
    event.setCancelled(StorageSign.isStorageSign(event.getBlock(), _logger));
  }
}
