package wacky.storagesign;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.NumberConversions;

public class StorageSign {

    protected Material mat;
    protected short damage;
    protected Enchantment ench;
    protected PotionType pot;
    protected int amount;
    protected int stack;
    protected boolean isEmpty;
    //StorageSignだと確認してから使っちくりー
    public StorageSign(ItemStack item) {
    	String[] str = item.getItemMeta().getLore().get(0).split(" ");
    	if(str[0].matches("Empty")) isEmpty = true;
    	else {
    		mat = getMaterial(str[0].split(":")[0]);
    		if(mat == Material.ENCHANTED_BOOK){
    			damage = NumberConversions.toShort(str[0].split(":")[2]);
    			ench = Enchantment.getByName(str[0].split(":")[1]);//旧仕様も
    			if(ench == null) ench = Enchantment.getById(NumberConversions.toInt(str[0].split(":")[1]));
    		}
    		else if(mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION){
    			PotionInfo pi = new PotionInfo(mat, str[0].split(":"));
    			mat = pi.getMaterial();
    			damage = pi.getDamage();
    			pot = pi.getPotionType();
    		}else if(str[0].contains(":")) damage = NumberConversions.toShort(str[0].split(":")[1]);
    		amount = NumberConversions.toInt(str[1]);
    	}
    	stack = item.getAmount();
    }

    //Sto(ry
    public StorageSign(Sign sign) {
        String[] line2 = sign.getLine(1).trim().split(":");
        mat = getMaterial(line2[0]);
        isEmpty = mat == null || mat == Material.AIR;
        if(line2.length == 2) damage = NumberConversions.toShort(line2[1]);
        if(mat == Material.ENCHANTED_BOOK){
        	damage = NumberConversions.toShort(line2[2]);
        	ench = Enchantment.getById(NumberConversions.toInt(line2[1]));
        }
        else if(mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION){
			PotionInfo pi = new PotionInfo(mat, line2);
			mat = pi.getMaterial();
			damage = pi.getDamage();
			pot = pi.getPotionType();
        }
        amount = NumberConversions.toInt(sign.getLine(2));
        isEmpty = amount == 0;
        stack = 1;
    }

	protected Material getMaterial(String str) {
    	//後ろのせいで判別不能なアイテムたち
        if (str.matches("EmptySign")) return Material.PORTAL;
        if (str.matches("HorseEgg")){
        	damage = 1;
        	return Material.PORTAL;
        }
        if (str.startsWith("REDSTONE_TORCH")) return Material.REDSTONE_TORCH_ON;
        if (str.startsWith("RS_COMPARATOR")) return Material.REDSTONE_COMPARATOR;
        if (str.startsWith("STAINGLASS_P")) return Material.STAINED_GLASS_PANE;
        if (str.startsWith("BROWN_MUSH_B")) return Material.HUGE_MUSHROOM_1;
        if (str.startsWith("RED_MUSH_BLO")) return Material.HUGE_MUSHROOM_2;
        if (str.startsWith("ENCHBOOK")) return Material.ENCHANTED_BOOK;
        if (str.startsWith("SPOTION")) return Material.SPLASH_POTION;
        if (str.startsWith("LPOTION")) return Material.LINGERING_POTION;

        Material mat = Material.matchMaterial(str);
        if (mat == null) { //後ろ切れる程度なら対応可
            for (Material m : Material.values()) {
                if(m.toString().startsWith(str)) return m;
            }
        }
        return mat;//nullなら空.
    }

    protected String getShortName() {//看板用15文字以内
        if (mat == null || mat == Material.AIR) return "";
        else if (mat == Material.PORTAL){
        	if(damage == 0) return "EmptySign";
        	if(damage == 1) return "HorseEgg";
        }
        else if (mat == Material.STAINED_GLASS_PANE) return damage == 0 ? "STAINGLASS_PANE" : "STAINGLASS_P:" + damage;
        else if (mat == Material.REDSTONE_COMPARATOR) return "RS_COMPARATOR";
        else if (mat == Material.REDSTONE_TORCH_ON) return "REDSTONE_TORCH";
        else if (mat == Material.HUGE_MUSHROOM_1) return damage == 0 ? "BROWN_MUSH_BLOC" : "BROWN_MUSH_B:" + damage;
        else if (mat == Material.HUGE_MUSHROOM_2) return damage == 0 ? "RED_MUSH_BLOCK" : "RED_MUSH_BLO:" + damage;
        else if (mat == Material.ENCHANTED_BOOK) return "ENCHBOOK:" + ench.getId() + ":" + damage;

        else if (mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION)
        {
        	String prefix = "";
        	if(mat == Material.SPLASH_POTION) prefix = "S";
        	else if(mat == Material.LINGERING_POTION) prefix = "L";

        	return prefix + "POTION:" + PotionInfo.getShortType(pot) +":" + damage;
        }

        int limit = 15;
        if (damage != 0) limit -= String.valueOf(damage).length() + 1;
        if (mat.toString().length() > limit) {
            if (damage != 0) return mat.toString().substring(0, limit) + ":" + damage;
            return mat.toString().substring(0, limit);
        } else {
            if (damage != 0) return mat.toString() + ":" + damage;
            return mat.toString();
        }
    }

    public ItemStack getStorageSign() {
        ItemStack item = new ItemStack(Material.SIGN, stack);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("StorageSign");
        List<String> list = new ArrayList<>();
        //IDとMaterial名が混ざってたり、エンチャ本対応したり
        if (isEmpty) list.add("Empty");
        else if (mat == Material.PORTAL){
        	if(damage == 0) list.add("EmptySign " + amount);
        	if(damage == 1) list.add("HorseEgg " + amount);
        }
        else if (mat == Material.ENCHANTED_BOOK) list.add(mat.toString() + ":" + ench.getName() + ":" + damage + " " + amount);
        else if (mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION){
        	list.add(mat.toString() + ":" + pot.toString() + ":" + damage + " " + amount);
        }
        else if (damage != 0) list.add(mat.toString() + ":" + damage + " " + amount);
        else list.add(mat.toString() + " " + amount);
        meta.setLore(list);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack emptySign() {
        ItemStack emptySign = new ItemStack(Material.SIGN);
        ItemMeta meta = emptySign.getItemMeta();
        List<String> list = new ArrayList<>();
        meta.setDisplayName("StorageSign");
        list.add("Empty");
        meta.setLore(list);
        emptySign.setItemMeta(meta);
        return emptySign;
    }

    private ItemStack emptyHorseEgg() {
        ItemStack emptyHorseEgg = new ItemStack(Material.MONSTER_EGG);
        ItemMeta meta = emptyHorseEgg.getItemMeta();
        List<String> list = new ArrayList<>();
        meta.setDisplayName("HorseEgg");
        list.add("Empty");
        meta.setLore(list);
        emptyHorseEgg.setItemMeta(meta);
        return emptyHorseEgg;
	}

    public String getSigntext(int i) {
        String[] sign = new String[4];
        sign[0] = "StorageSign";
        sign[1] = getShortName();
        sign[2] = String.valueOf(amount);
        sign[3] = String.valueOf(amount / 3456) + "LC " + String.valueOf(amount % 3456 / 64) + "s " + String.valueOf(amount % 64);
        return sign[i];
    }

    public ItemStack getContents() {
        if (mat == null) return null;
        if (mat == Material.PORTAL){
        	if(damage == 0) return emptySign();
        	if(damage == 1) return emptyHorseEgg();
        }
        if (mat == Material.ENCHANTED_BOOK) {
            ItemStack item = new ItemStack(mat, 1);
            EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta)item.getItemMeta();
            enchantMeta.addStoredEnchant(ench, damage, true);
            item.setItemMeta(enchantMeta);
            return item;
        }
        else if(mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION){
        	ItemStack item = new ItemStack(mat, 1);
        	PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
        	potionMeta.setBasePotionData(new PotionData(pot, damage == 1, damage == 2));
            item.setItemMeta(potionMeta);
            return item;
        }
        return new ItemStack(mat, 1, damage);
    }

	//回収可能か判定、エンチャ本は本自身の合成回数を問わない
    public boolean isSimilar(ItemStack item) {
        if(item == null) return false;
        if (mat == Material.ENCHANTED_BOOK && item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta)item.getItemMeta();
            if(enchantMeta.getStoredEnchants().size() == 1)
            {
                Enchantment itemEnch = enchantMeta.getStoredEnchants().keySet().toArray(new Enchantment[0])[0];
                if (itemEnch == ench && enchantMeta.getStoredEnchantLevel(itemEnch) == damage) return true;
            }
            return false;
        }
        return getContents().isSimilar(item);
    }

    public Material getMaterial() {
        return mat;
    }
    public void setMaterial(Material mat) {
        this.mat = mat;
    }

    public short getDamage() {
        return damage;
    }

    public void setDamage(short damage) {
        this.damage = damage;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        isEmpty = amount == 0;
    }

    public void addAmount(int amount) {
        if(this.amount < -amount) this.amount = 0;
        else this.amount += amount;
        if(this.amount < 0) this.amount = Integer.MAX_VALUE;
        isEmpty = this.amount == 0;
    }

    public int getStackSize() {
        return stack;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

	public void setEnchant(Enchantment ench) {
		this.ench = ench;
	}

	public Enchantment getEnchant() {
		return ench;
	}

	public void setPotion(PotionType pot) {
		this.pot = pot;
	}

	public PotionType getPotion() {
		return pot;
	}
}
