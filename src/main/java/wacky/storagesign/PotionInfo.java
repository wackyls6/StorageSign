package wacky.storagesign;

import org.bukkit.Material;
import org.bukkit.potion.PotionType;
import org.bukkit.util.NumberConversions;

public class PotionInfo {

	protected Material mat;
	protected PotionType pot;
	protected short damage;

	public PotionInfo(Material mat, String[] str){
		this.mat = mat;
		if(str.length == 2){//ダメージ値
			short old = NumberConversions.toShort(str[1]);
			if(old == 0){//文字列
				pot = getType(str[1].substring(0, 6));
				damage = NumberConversions.toShort(str[1].charAt(6));
				return;
			}
			if(old > 16384) this.mat = Material.SPLASH_POTION;
			switch(old % 32){
			case 1:
				pot = PotionType.REGEN;
			case 2:
				pot = PotionType.SPEED;
			case 3:
				pot = PotionType.FIRE_RESISTANCE;
			case 4:
				pot = PotionType.POISON;
			case 5:
				pot = PotionType.INSTANT_HEAL;
			case 6:
				pot = PotionType.NIGHT_VISION;
			case 8:
				pot = PotionType.WEAKNESS;
			case 9:
				pot = PotionType.STRENGTH;
			case 10:
				pot = PotionType.SLOWNESS;
			case 11:
				pot = PotionType.JUMP;
			case 12:
				pot = PotionType.INSTANT_DAMAGE;
			case 13:
				pot = PotionType.WATER_BREATHING;
			case 14:
				pot = PotionType.INVISIBILITY;
			}
			if(old % 8192 > 64) damage = 1;
			else if(old % 8192 > 32) damage = 2;

		}else{
			pot = getType(str[1]);
			damage = NumberConversions.toShort(str[2]);
		}
	}

	private PotionType getType(String substring) {
		if(substring.equals("BREAT")) return PotionType.WATER_BREATHING;//例外
		else if(substring.equals("HEAL")) return PotionType.INSTANT_HEAL;
		else if(substring.equals("DAMAG")) return PotionType.INSTANT_HEAL;
        else{ //後ろ切れてるかも
            for(PotionType p : PotionType.values()) {
                if(p.toString().startsWith(substring)) return p;
            }
        }
		return null;
	}

	public static String getShortType(PotionType pot){
		if(pot == PotionType.WATER_BREATHING) return "BREAT";
		else if(pot == PotionType.INSTANT_HEAL) return "HEAL";
		else if(pot == PotionType.INSTANT_DAMAGE) return "DAMAG";
		else if(pot.toString().length() <= 5) return pot.toString();
		return pot.toString().substring(0, 5);
	}


	public short getDamage() {
		return damage;
	}

	public Material getMaterial() {
		return mat;
	}

	public PotionType getPotionType() {
		return pot;
	}

}
