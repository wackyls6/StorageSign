package wacky.storagesign;

import java.util.Objects;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;

/**
 * 不吉な瓶の情報クラス.
 * */
@Getter
public class OmniousBottleInfo {

  /**
   * 不吉な瓶のマテリアル.
   * */
  private final Material material;

  /**
   * 不吉な瓶のマテリアル.
   * */
  private final short amplifier;

  public OmniousBottleInfo(Material material, String[] itemStr){
    this.material = material;
    this.amplifier = Short.parseShort(itemStr[1]);
  }

  public static ItemStack GetItemStack(Material mat, int amount, short damage){
    ItemStack item = new ItemStack(mat);
    int amt = Math.min(amount, item.getMaxStackSize());
    // Paperでamtに0を入れるとなぜかアイテムスタックが消えてしまうため、0の場合は設定しない
    if(amt != 0) {
      item.setAmount(amt);
    }
    OminousBottleMeta ominousMeta = (OminousBottleMeta) item.getItemMeta();
    Objects.requireNonNull(ominousMeta).setAmplifier(damage);
    item.setItemMeta(ominousMeta);

    return item;
  }

  public static short GetAmplifierWithMeta(ItemMeta meta){
    OminousBottleMeta ominousMeta = (OminousBottleMeta) meta;
    if(ominousMeta.hasAmplifier()) {
      return (short)ominousMeta.getAmplifier();
    } else {
      return 0;
    }
  }

  public static String GetTagData(Material mat, short damage, int amount){
    return mat.toString() + ":" + damage + " " + amount;
  }

  public static String GetSignString(Material mat, short damage){
    return mat +":" + damage;
  }

  public static boolean IsSimilar(ItemMeta meta, short damage){
    OminousBottleMeta omiItemMeta = (OminousBottleMeta) meta;
    if (Objects.isNull(omiItemMeta)) {
      return false;
    }
    if(omiItemMeta.hasAmplifier() && ((short)omiItemMeta.getAmplifier()) == damage) {
      return true;
    } else {
      return (!omiItemMeta.hasAmplifier() && damage == 0);
    }
  }
}
