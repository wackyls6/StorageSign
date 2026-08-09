package wacky.storagesign.runnnable;

import com.github.teruteru128.logger.Logger;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import wacky.storagesign.StorageSign;

public class ExportSign extends BukkitRunnable {

  private Logger _logger;
  private InventoryMoveItemEvent _event;
  private ItemStack _moveItem;
  private Inventory _inventory;
  private Inventory _destInventory;
  private Block _block;

  public ExportSign(ItemStack moveItemStack, Inventory sourceInventory, Inventory destInventory, InventoryMoveItemEvent event, Block block, Logger logger){
    _logger = logger;
    _logger.debug("★exportSign:Start.");
    _logger.trace("moveItem: " + moveItemStack);
    _logger.trace("moveItem.getAmount(): " + moveItemStack.getAmount());
    _logger.trace("sourceInventory.getLocation(): " + sourceInventory.getLocation());
    _moveItem = moveItemStack;
    _inventory = sourceInventory;
    _destInventory = destInventory;
    _event = event;
    _block = block;
    _logger.debug("★exportSign:End.");
  }
  /**
   * Runs this operation.
   */
  @Override
  public void run() {
    _destInventory = _event.getSource();
    Sign sign = (Sign) _block.getState();
    StorageSign ss = new StorageSign(sign, _block.getType(), _logger);;

    _logger.debug("exportSign:Start.");
    _logger.trace("item: " + _moveItem);
    _logger.trace("!inv.containsAtLeast(item, item.getMaxStackSize(): " + !_inventory.containsAtLeast(_moveItem, _moveItem.getMaxStackSize()));
    _logger.trace("storageSign.getAmount(): " + ss.getAmount());
    _logger.trace("item.getAmount(): " + _moveItem.getAmount());
    if (!_inventory.containsAtLeast(_moveItem, _moveItem.getMaxStackSize())
        && ss.getAmount() >= _moveItem.getAmount()) {
      int stacks = 0;
      int amount = 0;
      ItemStack[] contents = _destInventory.getContents();

      _logger.trace("dest.getType(): " + _destInventory.getType());
      _logger.trace("item.getType(): " + _moveItem.getType());
      if (_destInventory.getType() == InventoryType.BREWING) {
        switch (_moveItem.getType()) {
          case NETHER_WART://上
          case SUGAR:
          case REDSTONE:
          case GLOWSTONE_DUST:
          case GUNPOWDER:
          case RABBIT_FOOT:
          case GLISTERING_MELON_SLICE:
          case GOLDEN_CARROT:
          case MAGMA_CREAM:
          case GHAST_TEAR:
          case SPIDER_EYE:
          case FERMENTED_SPIDER_EYE:
          case DRAGON_BREATH:
          case PUFFERFISH:
          case TURTLE_HELMET:
          case PHANTOM_MEMBRANE:
          case BREEZE_ROD:
          case SLIME_BLOCK:
          case COBWEB:
          case STONE:
            //上から搬入
            _logger.debug("This item is PHANTOM_MEMBRANE.");
            _logger.trace("inv.getLocation().getBlockY()" + _inventory.getLocation().getBlockY());
            _logger.trace("dest.getLocation().getBlockY()" + _destInventory.getLocation().getBlockY());
            _logger.trace("inv.getLocation().getBlockY() > dest.getLocation().getBlockY()" + (_inventory.getLocation().getBlockY() > _destInventory.getLocation().getBlockY()));
            _logger.trace("contents[3]: " + contents[3]);
            if (_inventory.getLocation().getBlockY() > _destInventory.getLocation().getBlockY()) {
              _logger.debug("This item import at up.");
              if (contents[3] != null && !_moveItem.isSimilar(contents[3])) {
                //他のアイテムが詰まってる
                _logger.debug("This item is clog.");
                return;
              } else {
                _logger.debug("This item Export.");
                break;
              }
            } else {
              _logger.debug("This item import not.");
              return;
            }

          case BLAZE_POWDER:
            //横or上
            _logger.debug("This item is BLAZE_POWDER.");
            _logger.trace("inv.getLocation().getBlockY()" + _inventory.getLocation().getBlockY());
            _logger.trace("dest.getLocation().getBlockY()" + _destInventory.getLocation().getBlockY());
            _logger.trace("inv.getLocation().getBlockY() > dest.getLocation().getBlockY()" + (_inventory.getLocation().getBlockY() > _destInventory.getLocation().getBlockY()));
            _logger.trace("inv.getLocation().getBlockY() == dest.getLocation().getBlockY(): " + (_inventory.getLocation().getBlockY() == _destInventory.getLocation().getBlockY()));
            if (_inventory.getLocation().getBlockY() > _destInventory.getLocation().getBlockY()) {//上から搬入
              _logger.debug("This item import at up.");
              _logger.trace("contents[3]: " + contents[3]);
              if (contents[3] != null && !_moveItem.isSimilar(contents[3])) {
                //他のアイテムが詰まってる
                _logger.debug("This item is clog.");
                return;
              } else {
                _logger.debug("This item import not.");
                break;
              }
            } else if (_inventory.getLocation().getBlockY() == _destInventory.getLocation().getBlockY()) {//横
              _logger.debug("This item import at beside.");
              _logger.trace("contents[4]: " + contents[4]);
              if (contents[4] != null && contents[4].getAmount() == 64) {
                //パウダー詰まり
                _logger.debug("This item is clog.");
                return;
              } else {
                _logger.debug("This item Export.");
                break;
              }
            } else {
              _logger.debug("This item import not.");
              return;
            }

          case POTION:
          case SPLASH_POTION:
          case LINGERING_POTION:
            //横or下
            _logger.debug("This item is POTIONS.");
            _logger.trace("inv.getLocation().getBlockY()" + _inventory.getLocation().getBlockY());
            _logger.trace("dest.getLocation().getBlockY()" + _destInventory.getLocation().getBlockY());
            _logger.trace("inv.getLocation().getBlockY() <= dest.getLocation().getBlockY()" + (_inventory.getLocation().getBlockY() <= _destInventory.getLocation().getBlockY()));
            if (_inventory.getLocation().getBlockY() <= _destInventory.getLocation().getBlockY()) {
              _logger.debug("This item import at beside or down.");
              _logger.trace("contents[0]: " + contents[0]);
              _logger.trace("contents[1]: " + contents[1]);
              _logger.trace("contents[2]: " + contents[2]);
              if (contents[0] != null && contents[1] != null && contents[2] != null) {
                _logger.debug("This item is clog.");
                return;
              } else {
                _logger.debug("This item Export.");
                break;
              }
            } else {
              _logger.debug("This item import not.");
              return;
            }
          default://ロスト回避
            _logger.debug("This Item not support BREW Item.");
            return;
        }
      } else if (_destInventory.getType() == InventoryType.FURNACE
          || _destInventory.getType() == InventoryType.BLAST_FURNACE
          || _destInventory.getType() == InventoryType.SMOKER) {
        _logger.debug("This Type is Furnace Series.");
        _logger.trace("inv.getLocation().getBlockY()" + _inventory.getLocation().getBlockY());
        _logger.trace("dest.getLocation().getBlockY()" + _destInventory.getLocation().getBlockY());
        _logger.trace("inv.getLocation().getBlockY() > dest.getLocation().getBlockY(): " + (_inventory.getLocation().getBlockY() > _destInventory.getLocation().getBlockY()));
        if (_inventory.getLocation().getBlockY() > _destInventory.getLocation().getBlockY()) {//上から搬入
          _logger.debug("This item import at up.");
          _logger.trace("contents[0]: " + contents[0]);
          if (contents[0] != null && !_moveItem.isSimilar(contents[0])) {
            //他のアイテムが詰まってる
            _logger.debug("This item is clog.");
            return;
          }
        } else {
          //横から(下から)
          _logger.debug("This item import at beside or down.");
          _logger.trace("contents[1]: " + contents[1]);
          if (!_moveItem.getType().isFuel() || contents[1] != null && !_moveItem.isSimilar(contents[1])) {
            //燃料以外 or 他のアイテムが詰まってる
            _logger.debug("This item is clog or not Fuel.");
            return;
          }
        }
      }

      _logger.debug("Export Item");
      //PANPANによるロスト回避
      for (int i = 0; i < contents.length; i++) {
        _logger.trace("i: " + i);
        _logger.trace("contents[i]: " + contents[i]);
        if (_moveItem.isSimilar(contents[i])) {
          stacks++;
          amount += contents[i].getAmount();
          _logger.trace(" stacks: " + stacks);
          _logger.trace(" amount: " + amount);
        }
      }
      _logger.trace("amount == stacks * item.getMaxStackSize(): " + (amount
          == stacks * _moveItem.getMaxStackSize()));
      _logger.trace("dest.firstEmpty() == -1: " + (_destInventory.firstEmpty() == -1));
      if (amount == stacks * _moveItem.getMaxStackSize() && _destInventory.firstEmpty() == -1) {
        _logger.debug("Item less than Stack.not Export.");
        return;
      }

      _logger.debug("Export Item to Inventory.");
      ItemStack cItem = _moveItem.clone();
      // 合計スタック数-存在数で、転送されたアイテム数分アイテムを補充する
      // stacksが0なら、最大値
      int addAmount = 0;
      if(stacks == 0) {
        addAmount = cItem.getMaxStackSize();
      } else {
        addAmount = cItem.getMaxStackSize() * stacks - amount;
      }
      _logger.debug("amount:" + amount);
      _logger.debug("addAmount:" + addAmount);
      _logger.debug("ss.getAmount:" + ss.getAmount());
      cItem.setAmount(addAmount);
      _inventory.addItem(cItem);
      ss.addAmount(-cItem.getAmount());
    }
    for (int i = 0; i < 4; i++) {
      _logger.trace("set Line i:" + i + ". Text: " + ss.getSigntext(i));
      sign.getSide(Side.FRONT).setLine(i, ss.getSigntext(i));
    }
    sign.update();
    _logger.debug("ExportSign:End");
  }
}
