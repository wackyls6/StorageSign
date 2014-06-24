package wacky.storagesign;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;

public class StorageSignCore extends JavaPlugin implements Listener{

	public void onEnable()
	{
		ShapedRecipe storageSignRecipe = new ShapedRecipe(StorageSign.emptySign(1));
		storageSignRecipe.shape("CCC","CSC","CCC");
		storageSignRecipe.setIngredient('C', Material.CHEST);
		storageSignRecipe.setIngredient('S', Material.SIGN);
		getServer().addRecipe(storageSignRecipe);
		getServer().getPluginManager().registerEvents(this, this);

		this.saveDefaultConfig();
	}

	public void onDisable(){}

	public static String LargeChest(int i)
	{
		return String.valueOf(i / 3456) + "LC " + String.valueOf(i % 3456 / 64) + "s " + String.valueOf(i % 64);
	}

	public static String LargeChest(String str)
	{
		return LargeChest(NumberConversions.toInt(str));
	}

	public boolean isStorageSign(ItemStack item)
	{
		if(item.getType() != Material.SIGN) return false;
		if(!item.getItemMeta().hasDisplayName()) return false;
		if(!item.getItemMeta().getDisplayName().matches("StorageSign")) return false;
		if(!item.getItemMeta().hasLore()) return false;
		return true;
	}

	public boolean isStorageSign(Block block)
	{
		if(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)
		{
			Sign sign = (Sign) block.getState();
			if(sign.getLine(0).matches("StorageSign")) return true;
		}
		return false;
	}

	public static Material getMaterial(String str)
	{
		if(str.matches("EmptySign")) return Material.LOCKED_CHEST;
		if(str.matches("REDSTONE_TORCH")) return Material.REDSTONE_TORCH_ON;
		if(str.matches("RS_COMPARATOR")) return Material.REDSTONE_COMPARATOR;
		if(str.matches("STAINGLASS_P")) return Material.STAINED_GLASS_PANE;
		Material mat = Material.matchMaterial(str);
		if(mat == null)
		{//看板の文字数制限に対応.
			for(Material m : Material.values())
			{
				if(m.toString().startsWith(str)) return m;
			}
		}
		return mat;//ぬるぽなら報告オナシャス
	}

	public static String getShortenName(Material mat, String meta)
	{
		if(mat == Material.AIR) return "";//1.6でバグるって報告あったので
		else if(mat == Material.LOCKED_CHEST) return "EmptySign";
		else if(!Bukkit.getBukkitVersion().startsWith("1.6") && mat == Material.STAINED_GLASS_PANE) return "STAINGLASS_P:" + meta;
		else if(mat == Material.REDSTONE_COMPARATOR) return "RS_COMPARATOR";
		else if(mat == Material.REDSTONE_TORCH_ON) return "REDSTONE_TORCH";
		else if(meta.matches("0") || meta.matches(""))
		{
			if(mat.toString().length() > 15 )
			{
				return mat.toString().substring(0, 15);
			}else{
				return mat.toString();
			}
		}else{
			if(mat.toString().length() > 14 - meta.length())
			{
				return mat.toString().substring(0, 14 - meta.length()) + ":" + meta;
			}else{
				return mat.toString() + ":" + meta;
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		//手持ちがブロックだと叩いた看板を取得できないことがあるため
		if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
			try {
				block = player.getTargetBlock(null, 3);
			} catch (IllegalStateException ex) {
				return;
			}
		} else {
			block = event.getClickedBlock();
		}
		if (block == null) return;
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
		{

			if(!isStorageSign(block)) return;
			event.setUseItemInHand(Result.DENY);
			event.setUseInteractedBlock(Result.DENY);
			if(!player.hasPermission("storagesign.use"))
			{
				player.sendMessage(ChatColor.RED + this.getConfig().getString("no-permisson"));
				event.setCancelled(true);
				return;
			}
			Sign sign = (Sign) block.getState();
			StorageSign storageSign = new StorageSign(sign);
			ItemStack itemInHand = player.getItemInHand();
			Material mat;

			//アイテム登録
			if(storageSign.getMaterial() == null || storageSign.getMaterial() == Material.AIR)
			{
				mat = itemInHand.getType();
				if(mat == Material.AIR) return;
				else if(isStorageSign(itemInHand)) storageSign.setMaterial(Material.LOCKED_CHEST);
				else if(mat == Material.ENCHANTED_BOOK)
				{
					EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta)itemInHand.getItemMeta();
					if(enchantMeta.getStoredEnchants().size() == 1)
					{
						Enchantment ench = enchantMeta.getStoredEnchants().keySet().toArray(new Enchantment[0])[0];
						storageSign.setMaterial(mat);
						storageSign.setDamage((short) ench.getId());
						storageSign.setExtraData((short) enchantMeta.getStoredEnchantLevel(ench));
					}
				}else{
					storageSign.setMaterial(mat);
					storageSign.setDamage(itemInHand.getDurability());
				}

				for(int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
				sign.update();
				return;
			}

			if(isStorageSign(itemInHand))
			{
				//看板合成
				StorageSign itemSign = new StorageSign(itemInHand);
				if(itemSign.getMaterial() == storageSign.getMaterial() && itemSign.getDamage() == storageSign.getDamage() && itemSign.getExtraData() == storageSign.getExtraData() && this.getConfig().getBoolean("manual-import"))
				{
					storageSign.addAmount(itemSign.getAmount() * itemSign.getStackSize());
					itemSign.setAmount(0);
					player.setItemInHand(itemSign.getStorageSign());
				}//空看板収納
				else if(itemSign.isEmpty() && storageSign.getMaterial() == Material.LOCKED_CHEST && this.getConfig().getBoolean("manual-import"))
				{
					storageSign.addAmount(itemSign.getStackSize());
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				}
				//中身分割機能
				else if(itemSign.isEmpty() && storageSign.getAmount() > itemInHand.getAmount() && this.getConfig().getBoolean("manual-export"))
				{
					itemSign.setMaterial(storageSign.getMaterial());
					itemSign.setDamage(storageSign.getDamage());
					itemSign.setExtraData(storageSign.getExtraData());
					itemSign.setAmount(storageSign.getAmount() / (itemSign.getStackSize() + 1));
					player.setItemInHand(itemSign.getStorageSign());
					storageSign.setAmount(itemSign.getAmount() + storageSign.getAmount() % (itemSign.getStackSize() + 1 ));//余りは看板に引き受けてもらう
				}
				//看板記入を一気に
				for(int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
				sign.update();
				return;
			}

			//ここから搬入
			mat = storageSign.getMaterial();//使い回しｗ
			if(storageSign.getContents().isSimilar(itemInHand))
			{
				if(!this.getConfig().getBoolean("manual-import")) return;
				if(player.isSneaking())
				{
					storageSign.addAmount(itemInHand.getAmount());
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				}
				else for(int i=0; i<player.getInventory().getSize(); i++)
				{
					ItemStack item = player.getInventory().getItem(i);
					if(storageSign.getContents().isSimilar(item))
					{
						storageSign.addAmount(item.getAmount());
						player.getInventory().clear(i);
					}
				}

				player.updateInventory();
			}

			else if(this.getConfig().getBoolean("manual-export"))//放出
			{
				if(storageSign.isEmpty()) return;
				ItemStack item = storageSign.getContents();

				int max = storageSign.getMaterial().getMaxStackSize();

				if(player.isSneaking()) storageSign.addAmount(-1);
				else if(storageSign.getAmount() > max)
				{
					item.setAmount(max);
					storageSign.addAmount(-max);
				}else{
					item.setAmount(storageSign.getAmount());
					storageSign.setAmount(0);
				}

				Location loc = player.getLocation();
				loc.setY(loc.getY() + 0.5);
				player.getWorld().dropItem(loc, item);
			}

			for(int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
			sign.update();
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event)
	{
		if(event.isCancelled())return;
		Sign sign = (Sign) event.getBlock().getState();

		if(sign.getLine(0).matches("StorageSign"))//変更拒否
		{
			event.setLine(0, sign.getLine(0));
			event.setLine(1, sign.getLine(1));
			event.setLine(2, sign.getLine(2));
			event.setLine(3, sign.getLine(3));
			sign.update();
			return;
		}

		else if(event.getLine(0).equalsIgnoreCase("storagesign"))//書き込んで生成禁止
		{
			if(event.getPlayer().hasPermission("storagesign.create"))
			{
				event.setLine(0, "StorageSign");
				sign.update();
			}
			else{
				event.getPlayer().sendMessage(ChatColor.RED + this.getConfig().getString("no-permisson"));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled())return;
		Block block = event.getBlock();
		Map<Location, StorageSign> breakSignMap = new HashMap<Location, StorageSign>();
		if(isStorageSign(block)) breakSignMap.put(block.getLocation(), new StorageSign((Sign)block.getState()));

		for(int i=0; i<5; i++)
		{
			int[] x = {0, 0, 0,-1, 1};
			int[] y = {1, 0, 0, 0, 0};
			int[] z = {0,-1, 1, 0, 0};
			block = event.getBlock().getRelative(x[i], y[i], z[i]);
			if(i==0 && block.getType() == Material.SIGN_POST && isStorageSign(block)) breakSignMap.put(block.getLocation(), new StorageSign((Sign)block.getState()));
			else if(block.getType() == Material.WALL_SIGN && block.getData() == i+1 && isStorageSign(block)) breakSignMap.put(block.getLocation(), new StorageSign((Sign)block.getState()));
		}
		if(breakSignMap.isEmpty()) return;
		if(!event.getPlayer().hasPermission("storagesign.break"))
		{
			event.getPlayer().sendMessage(ChatColor.RED + this.getConfig().getString("no-permisson"));
			event.setCancelled(true);
			return;
		}

		for(Location loc : breakSignMap.keySet())
		{
			StorageSign sign = breakSignMap.get(loc);
			loc.getWorld().dropItemNaturally(loc, sign.getStorageSign());
			loc.getBlock().setType(Material.AIR);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(event.isCancelled() || !isStorageSign(event.getItemInHand())) return;
		if(!event.getPlayer().hasPermission("storagesign.place"))
		{
			event.getPlayer().sendMessage(ChatColor.RED + this.getConfig().getString("no-permisson"));
			event.setCancelled(true);
			return;
		}
		StorageSign storageSign = new StorageSign(event.getItemInHand());
		Sign sign = (Sign)event.getBlock().getState();
		for(int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
		sign.update();
	}

	@EventHandler
	public void onItemMove(InventoryMoveItemEvent event)
	{
		if(event.isCancelled()) return;
		BlockState[] blockInventory =new BlockState[2];
		Boolean flag = false;
		Sign sign = null;
		StorageSign storageSign = null;

		if(this.getConfig().getBoolean("auto-import"))
		{
			if(event.getDestination().getHolder() instanceof Minecart);//何もしない
			else if(event.getDestination().getHolder() instanceof DoubleChest)
			{
				DoubleChest lc = (DoubleChest)event.getDestination().getHolder();
				blockInventory[0] = (BlockState) lc.getLeftSide();
				blockInventory[1] = (BlockState) lc.getRightSide();
			}else{
				blockInventory[0] = (BlockState) event.getDestination().getHolder();
			}

			importLoop:
				for(int j=0; j<2; j++)
				{
					if(blockInventory[j] == null) break;
					for(int i=0; i<5; i++)
					{
						int[] x = {0, 0, 0,-1, 1};
						int[] y = {1, 0, 0, 0, 0};
						int[] z = {0,-1, 1, 0, 0};
						Block block = blockInventory[j].getBlock().getRelative(x[i], y[i], z[i]);
						if(i==0 && block.getType() == Material.SIGN_POST && isStorageSign(block))
						{
							sign = (Sign) block.getState();
							storageSign = new StorageSign(sign);
							if(storageSign.getContents().isSimilar(event.getItem()))
							{
								flag = true;
								break importLoop;
							}
						}
						else if(i != 0 && block.getType() == Material.WALL_SIGN && block.getData() == i+1 && isStorageSign(block))
						{
							sign = (Sign) block.getState();
							storageSign = new StorageSign(sign);
							if(storageSign.getContents().isSimilar(event.getItem()))
							{
								flag = true;
								break importLoop;
							}
						}
					}
				}
			//搬入先が見つかった(搬入するとは言ってない)
			if(flag) importSign(sign, storageSign, event.getItem(), event.getDestination());
		}

		//搬出用にリセット
		if(this.getConfig().getBoolean("auto-export"))
		{
			blockInventory[0] = null;
			blockInventory[1] = null;
			flag = false;
			if(event.getSource().getHolder() instanceof Minecart);
			else if(event.getSource().getHolder() instanceof DoubleChest)
			{
				DoubleChest lc = (DoubleChest)event.getSource().getHolder();
				blockInventory[0] = (BlockState) lc.getLeftSide();
				blockInventory[1] = (BlockState) lc.getRightSide();
			}else{
				blockInventory[0] = (BlockState) event.getSource().getHolder();
			}

			exportLoop:
				for(int j=0; j<2; j++)
				{
					if(blockInventory[j] == null) break;
					for(int i=0; i<5; i++)
					{
						int[] x = {0, 0, 0,-1, 1};
						int[] y = {1, 0, 0, 0, 0};
						int[] z = {0,-1, 1, 0, 0};
						Block block = blockInventory[j].getBlock().getRelative(x[i], y[i], z[i]);
						if(i==0 && block.getType() == Material.SIGN_POST && isStorageSign(block))
						{
							sign = (Sign) block.getState();
							storageSign = new StorageSign(sign);
							if(storageSign.getContents().isSimilar(event.getItem()))
							{
								flag = true;
								break exportLoop;
							}
						}
						else if(i != 0 && block.getType() == Material.WALL_SIGN && block.getData() == i+1 && isStorageSign(block))
						{
							sign = (Sign) block.getState();
							storageSign = new StorageSign(sign);
							if(storageSign.getContents().isSimilar(event.getItem()))
							{
								flag = true;
								break exportLoop;
							}
						}
					}
				}
			if(flag) exportSign(sign, storageSign, event.getItem(), event.getSource(), event.getDestination());
		}
	}

	private void importSign(Sign sign, StorageSign storageSign, ItemStack item, Inventory inv)
	{
			//搬入　条件　1スタック以上アイテムが入っている
			if(inv.containsAtLeast(item, item.getMaxStackSize()))
			{
				inv.removeItem(item);
				storageSign.addAmount(item.getAmount());
			}
			for(int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
			sign.update();
	}

	private void exportSign(Sign sign, StorageSign storageSign, ItemStack item, Inventory inv, Inventory dest)
	{//1.7では問題なし.(1.6ではスキマ送り)
		if(!inv.containsAtLeast(item, item.getMaxStackSize()) && storageSign.getAmount() >= item.getAmount())
		{
				inv.addItem(item);
				storageSign.addAmount(-item.getAmount());
		}
		for(int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
		sign.update();
	}

	@EventHandler
	public void onPlayerCraft(CraftItemEvent event)
	{
		if(isStorageSign(event.getCurrentItem()) && !event.getWhoClicked().hasPermission("storagesign.craft"))
		{
			((CommandSender) event.getWhoClicked()).sendMessage(ChatColor.RED + this.getConfig().getString("no-permisson"));
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onInventoryPickup(InventoryPickupItemEvent event)
	{
		if(event.isCancelled() || !this.getConfig().getBoolean("auto-import")) return;

		InventoryHolder holder = event.getInventory().getHolder();
		if(holder instanceof BlockState)
		{
			Sign sign = null;
			StorageSign storageSign = null;
			boolean flag = false;
			for(int i=0; i<5; i++)
			{
				int[] x = {0, 0, 0,-1, 1};
				int[] y = {1, 0, 0, 0, 0};
				int[] z = {0,-1, 1, 0, 0};
				Block block = ((BlockState)holder).getBlock().getRelative(x[i], y[i], z[i]);
				if(i==0 && block.getType() == Material.SIGN_POST && isStorageSign(block))
				{
					sign = (Sign) block.getState();
					storageSign = new StorageSign(sign);
					if(storageSign.getContents().isSimilar(event.getItem().getItemStack()))
					{
						flag = true;
						break;
					}
				}
				else if(i != 0 && block.getType() == Material.WALL_SIGN && block.getData() == i+1 && isStorageSign(block))
				{
					sign = (Sign) block.getState();
					storageSign = new StorageSign(sign);
					if(storageSign.getContents().isSimilar(event.getItem().getItemStack()))
					{
						flag = true;
						break;
					}
				}
			}
			if(flag) importSign(sign, storageSign, event.getItem().getItemStack(), event.getInventory());
		}
	}
}