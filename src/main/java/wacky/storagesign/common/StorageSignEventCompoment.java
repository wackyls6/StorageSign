package wacky.storagesign.common;

import com.github.teruteru128.logger.Logger;
import org.bukkit.block.Block;
import wacky.storagesign.StorageSign;

public class StorageSignEventCompoment {
  public static boolean isStorageSign(Block block, Logger logger) {
    boolean isStorageSign = StorageSign.isStorageSign(block, logger);
    logger.trace("isStorageSign: " + isStorageSign);
    return isStorageSign;
  }
}
