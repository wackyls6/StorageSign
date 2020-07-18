package wacky.storagesign;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.NumberConversions;

public class EnchantInfo {

	protected Material mat;
	protected Enchantment ench;
	protected short damage = 0;

	public EnchantInfo(Material mat, String[] str){
		this.mat = mat;
			ench = getEnch(str[1]);
			damage = NumberConversions.toShort(str[2]);
	}

	private Enchantment getEnch(String substring) {
		//後ろ切れても可.
		for(Enchantment e : Enchantment.values()) {
			if(e.getKey().getKey().startsWith(substring)) return e;
		}
		return null;
	}

	public static String getShortType(Enchantment e){
		String key = e.getKey().getKey();
		if(key.matches("fire_protection")) return "fire_p";
		else if(key.matches("fire_aspect")) return "fire_a";
		
		else if(key.length() <= 5) return key;
		return key.substring(0, 5);
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
