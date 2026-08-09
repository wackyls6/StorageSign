package wacky.storagesign;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.NumberConversions;

public class EnchantInfo {

  protected Material mat;
  protected Enchantment ench;
  protected short damage = 0;
  private Logger _logger;

  public EnchantInfo(Material mat, String[] str, Logger logger) {
    this.mat = mat;
    this._logger = logger;

    _logger.debug("EnchantInfo:Start");
    ench = getEnch(str[1]);
    damage = NumberConversions.toShort(str[2]);
    _logger.trace("ench:" + ench);
    _logger.trace("damage:" + damage);
  }

  public static String getShortType(Enchantment e) {
    String key = e.getKey().getKey();
		if (key.matches("fire_protection")) {
			return "fire_p";
		} else if (key.matches("fire_aspect")) {
			return "fire_a";
		} else if (key.length() <= 5) {
			return key;
		}
    return key.substring(0, 5);
  }

  private Enchantment getEnch(String substring) {
    _logger.debug("getEnch: Start");
    //後ろ切れても可.
    for (Enchantment e : Registry.ENCHANTMENT) {
      _logger.trace(" e: " + e);
      _logger.trace(" e.getKey().getKey().startsWith(substring): " + e.getKey().getKey().startsWith(substring));
			if (e.getKey().getKey().startsWith(substring)) {
				return e;
			}
    }
    _logger.error("EnchantType is Not Found!substring:" + substring);
    return null;
  }

  public short getDamage() {
    return damage;
  }

  public Material getMaterial() {
    return mat;
  }

  public Enchantment getEnchantType() {
    return ench;
  }

}
