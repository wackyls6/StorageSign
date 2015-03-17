package wacky.storagesign;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.NumberConversions;

public class StorageSign {

    protected Material mat;
    protected short damage;
    protected short extraData;
    protected int amount;
    protected int stack;
    protected boolean isEmpty;
    //StorageSignだと確認してから使っちくりー
    public StorageSign(ItemStack item) {
        String[] str = item.getItemMeta().getLore().get(0).split(" ");
        if(str[0].matches("Empty")) isEmpty = true;
        else {
            mat = getMaterial(str[0].split(":")[0]);
            if(str[0].contains(":")) damage = NumberConversions.toShort(str[0].split(":")[1]);
            if(mat == Material.ENCHANTED_BOOK) extraData = NumberConversions.toShort(str[0].split(":")[2]);
            amount = NumberConversions.toInt(str[1]);
        }
        stack = item.getAmount();
    }

    //Sto(ry
    public StorageSign(Sign sign) {
        String[] line2 = sign.getLine(1).trim().split(":");
        mat = getMaterial(line2[0]);
        isEmpty = mat == null || mat == Material.AIR;
        if(line2.length >= 2) damage = NumberConversions.toShort(line2[1]);
        if(line2.length == 3) extraData = NumberConversions.toShort(line2[2]);
        amount = NumberConversions.toInt(sign.getLine(2));
        isEmpty = amount == 0;
        stack = 1;
    }


    protected Material getMaterial(String str) {
        if (str.matches("EmptySign")) return Material.CHEST;
        if (str.startsWith("REDSTONE_TORCH")) return Material.REDSTONE_TORCH_ON;
        if (str.startsWith("RS_COMPARATOR")) return Material.REDSTONE_COMPARATOR;
        if (str.startsWith("STAINGLASS_P")) return Material.STAINED_GLASS_PANE;
        if (str.startsWith("BROWN_MUSH_B")) return Material.HUGE_MUSHROOM_1;
        if (str.startsWith("RED_MUSH_BLO")) return Material.HUGE_MUSHROOM_2;
        if (str.startsWith("ENCHBOOK")) return Material.ENCHANTED_BOOK;

        Material mat = Material.matchMaterial(str);
        if (mat == null) { //看板の文字数制限に対応
            for (Material m : Material.values()) {
                if(m.toString().startsWith(str)) return m;
            }
        }
        return mat;//nullなら空.
    }

    protected String getShortName() {
        if (mat == null || mat == Material.AIR) return "";
        else if (mat == Material.CHEST) return "EmptySign";//1.6でバグるって報告あったので
        else if (!Bukkit.getBukkitVersion().startsWith("1.6") && mat == Material.STAINED_GLASS_PANE) return damage == 0 ? "STAINGLASS_PANE" : "STAINGLASS_P:" + damage;
        else if (mat == Material.REDSTONE_COMPARATOR) return "RS_COMPARATOR";
        else if (mat == Material.REDSTONE_TORCH_ON) return "REDSTONE_TORCH";
        else if (mat == Material.HUGE_MUSHROOM_1) return damage == 0 ? "BROWN_MUSH_BLOC" : "BROWN_MUSH_B:" + damage;
        else if (mat == Material.HUGE_MUSHROOM_2) return damage == 0 ? "RED_MUSH_BLOCK" : "RED_MUSH_BLO:" + damage;
        else if (mat == Material.ENCHANTED_BOOK) return "ENCHBOOK:" + damage + ":" + extraData;

        int limit = 15;
        if (extraData != 0) limit -= String.valueOf(extraData).length() + 1;
        if (damage != 0 || extraData != 0) limit -= String.valueOf(damage).length() + 1;
        if (mat.toString().length() > limit) {
            if (extraData != 0) return mat.toString().substring(0, limit) + ":" + damage + ":" + extraData;
            if (damage != 0) return mat.toString().substring(0, limit) + ":" + damage;
            return mat.toString().substring(0, limit);
        } else {
            if (extraData != 0) return mat.toString() + ":" + damage + ":" + extraData;
            if (damage != 0) return mat.toString() + ":" + damage;
            return mat.toString();
        }
    }

    public ItemStack getStorageSign() {
        ItemStack item = new ItemStack(Material.SIGN, stack);
        item.getItemMeta();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("StorageSign");
        List<String> list = new ArrayList<String>();
        //IDとMaterial名が混ざってたり、エンチャ本対応したり
        if (isEmpty) list.add("Empty");
        else if (mat == Material.CHEST) list.add("EmptySign " + amount);
        else if (extraData != 0) list.add(mat.toString() + ":" + damage + ":" + extraData + " " + amount);
        else if (damage != 0) list.add(mat.toString() + ":" + damage + " " + amount);
        else list.add(mat.toString() + " " + amount);
        meta.setLore(list);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack emptySign(int amount) {
        ItemStack emptySign = new ItemStack(Material.SIGN, amount);
        ItemMeta meta = emptySign.getItemMeta();
        List<String> list = new ArrayList<String>();
        meta.setDisplayName("StorageSign");
        list.add("Empty");
        meta.setLore(list);
        emptySign.setItemMeta(meta);
        return emptySign;
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
        if (mat == Material.CHEST) return emptySign(1);
        if (mat == Material.ENCHANTED_BOOK) {
            ItemStack item = new ItemStack(mat, 1);
            EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta)item.getItemMeta();
            enchantMeta.addStoredEnchant(Enchantment.getById(damage), extraData, true);
            item.setItemMeta(enchantMeta);
            return item;
        }
        return new ItemStack(mat, 1, damage);
    }

    public boolean isSimilar(ItemStack item) {
        if(item == null) return false;
        if (mat == Material.ENCHANTED_BOOK && item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta)item.getItemMeta();
            if(enchantMeta.getStoredEnchants().size() == 1)
            {
                Enchantment ench = enchantMeta.getStoredEnchants().keySet().toArray(new Enchantment[0])[0];
                if (ench.getId() == damage && enchantMeta.getStoredEnchantLevel(ench) == extraData) return true;
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

    public short getExtraData() {
        return extraData;
    }

    public void setExtraData(short extraData) {
        this.extraData = extraData;
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
}
