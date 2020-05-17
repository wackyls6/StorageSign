package wacky.storagesign;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.NumberConversions;
import wacky.storagesign.PotionInfo;

public class StorageSign {

	protected Material smat;
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
    			EnchantInfo ei = new EnchantInfo(mat, str[0].split(":"));
    			damage = ei.getDamage();
    			ench = ei.getEnchantType();
    		}
    		else if(mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION){
    			PotionInfo pi = new PotionInfo(mat, str[0].split(":"));
    			mat = pi.getMaterial();
    			damage = pi.getDamage();
    			pot = pi.getPotionType();
    		}else if(str[0].contains(":")) damage = NumberConversions.toShort(str[0].split(":")[1]);
    		else if(mat== Material.STONE_SLAB )  mat = Material.SMOOTH_STONE_SLAB;//1.13の滑らかハーフと1.14の石ハーフ区別
    		amount = NumberConversions.toInt(str[1]);
    	}
    	smat = item.getType();
    	stack = item.getAmount();
    }

    //Sto(ry
   /* public StorageSign(Sign sign) {
    	this(sign,Material.OAK_SIGN);
    }*/
    
	public StorageSign(Sign sign,Material signmat) {//上と統合したい
        String[] line2 = sign.getLine(1).trim().split(":");
        mat = getMaterial(line2[0]);
        isEmpty = mat == null || mat == Material.AIR;
        
		 if(mat == Material.ENCHANTED_BOOK){
			EnchantInfo ei = new EnchantInfo(mat, line2);
        	damage = ei.getDamage();
        	ench = ei.getEnchantType();
        }
        else if(mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION){
			PotionInfo pi = new PotionInfo(mat, line2);
			mat = pi.getMaterial();
			damage = pi.getDamage();
			pot = pi.getPotionType();
        }
        else if(line2.length == 2) damage = NumberConversions.toShort(line2[1]);
        else if(mat== Material.STONE_SLAB )  mat = Material.SMOOTH_STONE_SLAB;//1.13の滑らかハーフと1.14の石ハーフ区別
		 
        amount = NumberConversions.toInt(sign.getLine(2));
        isEmpty = amount == 0;
        stack = 1;

        //壁掛け看板のチェック
        if(signmat == Material.OAK_WALL_SIGN) smat = Material.OAK_SIGN;
        else if(signmat == Material.BIRCH_WALL_SIGN) smat = Material.BIRCH_SIGN;
        else if(signmat == Material.SPRUCE_WALL_SIGN) smat = Material.SPRUCE_SIGN;
        else if(signmat == Material.JUNGLE_WALL_SIGN) smat = Material.JUNGLE_SIGN;
        else if(signmat == Material.ACACIA_WALL_SIGN) smat = Material.ACACIA_SIGN;
        else if(signmat == Material.DARK_OAK_WALL_SIGN) smat = Material.DARK_OAK_SIGN;
        //else if(signmat == Material.CRIMSON_WALL_SIGN) smat = Material.CRIMSON_SIGN;
        //else if(signmat == Material.WARPED_WALL_SIGN) smat = Material.WARPED_SIGN;
        else smat = signmat;
        
    }

	//変換
	protected Material getMaterial(String str) {
		if (str.matches("")) return Material.AIR;
       // if (str.matches("EmptySign")) return Material.END_PORTAL;
        else if(str.matches("EmptySign") || str.matches("OakStorageSign")) {
        	damage = 1;
        	return Material.OAK_SIGN;
        }
        else if(str.matches("SpruceStorageSign")) {
        	damage = 1;
        	return Material.SPRUCE_SIGN;
        }
        else if(str.matches("BirchStorageSign")) {
        	damage = 1;
        	return Material.BIRCH_SIGN;
        }
        else if(str.matches("JungleStorageSign")) {
        	damage = 1;
        	return Material.JUNGLE_SIGN;
        }
        else if(str.matches("AcaciaStorageSign")) {
        	damage = 1;
        	return Material.ACACIA_SIGN;
        }
        else if(str.matches("DarkOakStorageSign")) {
        	damage = 1;
        	return Material.DARK_OAK_SIGN;
        }
        if (str.matches("HorseEgg")){
        	damage = 1;
        	return Material.END_PORTAL;//ガスト卵でよくね？
        }
        //if (str.startsWith("REDSTONE_TORCH")) return Material.REDSTONE_TORCH;
        //if (str.startsWith("RS_COMPARATOR")) return Material.COMPARATOR;
        //if (str.startsWith("STAINGLASS_P")) return Material.LEGACY_STAINED_GLASS_PANE;
        //if (str.startsWith("BROWN_MUSH_B")) return Material.BROWN_MUSHROOM_BLOCK;
        //if (str.startsWith("RED_MUSH_BLO")) return Material.RED_MUSHROOM_BLOCK;
        
        //1.13→1.14用
        if (str.startsWith("SIGN")) return Material.OAK_SIGN;
        if (str.startsWith("ROSE_RED")) return Material.RED_DYE;
        if (str.startsWith("DANDELION_YELLOW")) return Material.YELLOW_DYE;
        if (str.startsWith("CACTUS_GREEN")) return Material.GREEN_DYE;
        
        //省略用
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
	
    protected String getShortName() {//2行目の記載内容
        if (mat == null || mat == Material.AIR) return "";
        else if (mat == Material.END_PORTAL){
        	if(damage == 0) return "OakStorageSign";
        	if(damage == 1) return "HorseEgg";
        }else if(mat == Material.OAK_SIGN && damage == 1) return "OakStorageSign";
        else if(mat == Material.SPRUCE_SIGN && damage == 1) return "SpruceStorageSign";
        else if(mat == Material.BIRCH_SIGN && damage == 1) return "BirchStorageSign";
        else if(mat == Material.JUNGLE_SIGN && damage == 1) return "JungleStorageSign";
        else if(mat == Material.ACACIA_SIGN && damage == 1) return "AcaciaStorageSign";
        else if(mat == Material.DARK_OAK_SIGN && damage == 1) return "DarkOakStorageSign";
        //else if (mat == Material.LEGACY_STAINED_GLASS_PANE) return damage == 0 ? "STAINGLASS_PANE" : "STAINGLASS_P:" + damage;
        //else if (mat == Material.LEGACY_REDSTONE_COMPARATOR) return "RS_COMPARATOR";
        //else if (mat == Material.LEGACY_REDSTONE_TORCH_ON) return "REDSTONE_TORCH";
        //else if (mat == Material.LEGACY_HUGE_MUSHROOM_1) return damage == 0 ? "BROWN_MUSH_BLOC" : "BROWN_MUSH_B:" + damage;
        //else if (mat == Material.LEGACY_HUGE_MUSHROOM_2) return damage == 0 ? "RED_MUSH_BLOCK" : "RED_MUSH_BLO:" + damage;
        else if (mat == Material.ENCHANTED_BOOK) return "ENCHBOOK:" + EnchantInfo.getShortType(ench) + ":" + damage;

        else if (mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION)
        {
        	String prefix = "";
        	if(mat == Material.SPLASH_POTION) prefix = "S";
        	else if(mat == Material.LINGERING_POTION) prefix = "L";

        	return prefix + "POTION:" + PotionInfo.getShortType(pot) +":" + damage;
        }

        int limit = 99;//リミットブレイク
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
        ItemStack item = new ItemStack(smat, stack);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("StorageSign");
        List<String> list = new ArrayList<>();
        //IDとMaterial名が混ざってたり、エンチャ本対応したり
        if (isEmpty) list.add("Empty");
        else if (mat == Material.ENCHANTED_BOOK) list.add(mat.toString() + ":" + ench.getKey().getKey() + ":" + damage + " " + amount);
        else if (mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION){
        	list.add(mat.toString() + ":" + pot.toString() + ":" + damage + " " + amount);
        }
        else list.add(getShortName() +" "+ amount);
        meta.setLore(list);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack emptySign() {
    	return emptySign(Material.OAK_SIGN);
    	
    }

	public static ItemStack emptySign(Material smat) {
        ItemStack emptySign = new ItemStack(smat);
        ItemMeta meta = emptySign.getItemMeta();
        List<String> list = new ArrayList<>();
        meta.setDisplayName("StorageSign");
        list.add("Empty");
        meta.setLore(list);
        emptySign.setItemMeta(meta);
        return emptySign;
	}
    

    private ItemStack emptyHorseEgg() {
        ItemStack emptyHorseEgg = new ItemStack(Material.GHAST_SPAWN_EGG);
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

    public ItemStack getContents() {//中身取得、一部アイテム用の例外
        if (mat == null) return null;
        if (mat == Material.END_PORTAL){
        	if(damage == 0) return emptySign();
        	if(damage == 1) return emptyHorseEgg();
        }if(mat == Material.STONE_SLAB) return new ItemStack(mat,1);//ダメージ値0にする
        else if(mat == Material.OAK_SIGN  || mat == Material.SPRUCE_SIGN || mat == Material.BIRCH_SIGN || mat == Material.JUNGLE_SIGN || mat == Material.ACACIA_SIGN  || mat == Material.DARK_OAK_SIGN) {
        	if(damage == 0) return new ItemStack(mat,1);
        	else return emptySign(mat);
        }
        else  if (mat == Material.ENCHANTED_BOOK) {
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
        }else if(mat == Material.FIREWORK_ROCKET){
        	ItemStack item = new ItemStack(mat, 1);
        	FireworkMeta fireworkMeta = (FireworkMeta)item.getItemMeta();
        	fireworkMeta.setPower(damage);
        	item.setItemMeta(fireworkMeta);
        	return item;
        }
        if(damage == 0) return new ItemStack(mat, 1);//大半はダメージなくなった
        return new ItemStack(mat, 1, damage);//ツール系のみダメージあり
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
        }else if(isShulker(mat)){
        	//後回し
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
	
	public Material getSmat() {
		return smat;
	}
	
	private boolean isShulker(Material mat) {
		switch(mat){
		case SHULKER_BOX:
		case WHITE_SHULKER_BOX:
		case ORANGE_SHULKER_BOX:
		case MAGENTA_SHULKER_BOX:
		case LIGHT_BLUE_SHULKER_BOX:
		case YELLOW_SHULKER_BOX:
		case LIME_SHULKER_BOX:
		case PINK_SHULKER_BOX:
		case GRAY_SHULKER_BOX:
		case LIGHT_GRAY_SHULKER_BOX:
		case CYAN_SHULKER_BOX:
		case PURPLE_SHULKER_BOX:
		case BLUE_SHULKER_BOX:
		case BROWN_SHULKER_BOX:
		case GREEN_SHULKER_BOX:
		case RED_SHULKER_BOX:
		case BLACK_SHULKER_BOX:
			return true;
		default:
		}
		return false;
	}
	
}
