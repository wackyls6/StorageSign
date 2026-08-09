package wacky.storagesign.client.paper;

import com.github.teruteru128.logger.Logger;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSignOpenEvent;
import wacky.storagesign.StorageSign;
import wacky.storagesign.common.StorageSignEventCompoment;

public class PaperSSEvent  implements Listener {
  private Logger logger;
  public void init(Logger logger){
    this.logger = logger;
  }

  @EventHandler
  public void onPlayerOpenSign(PlayerOpenSignEvent event){
    logger.debug("★onPlayerOpenSign:Start");

    logger.trace("event.isCancelled(): " + event.isCancelled());
    if (event.isCancelled()) {
      logger.debug("★this Event is Cancelled!");
      return;
    }

    Block block = event.getSign().getBlock();
    boolean isStorageSign = StorageSignEventCompoment.isStorageSign(block, logger);
    logger.trace("isStorageSign: " + isStorageSign);
    if (isStorageSign) {
      logger.debug("StorageSignEdit Cancel.");
      event.setCancelled(true);
    }

    logger.debug("★onPlayerOpenSign:End");
  }
}
