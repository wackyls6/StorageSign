package wacky.storagesign;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;

import org.bukkit.DyeColor;


public class StorageSignCore extends JavaPlugin implements Listener{

	FileConfiguration config;
    static BannerMeta ominousBannerMeta;

	@Override
	public void onEnable() {
		config = this.getConfig();
		config.options().copyDefaults(true);
		config.options().header("StorageSign Configuration");
		this.saveConfig();

		//鯖別レシピが実装されたら
		Material[] sign = {Material.OAK_SIGN,Material.BIRCH_SIGN,Material.SPRUCE_SIGN,Material.JUNGLE_SIGN,Material.ACACIA_SIGN,Material.DARK_OAK_SIGN};
		for(int i= 0 ;i<6;i++) {
			
		ShapedRecipe storageSignRecipe = new ShapedRecipe(new NamespacedKey(this,"ssr"+i),StorageSign.emptySign(sign[i]));
		//ShapedRecipe storageSignRecipe = new ShapedRecipe(StorageSign.emptySign());
		storageSignRecipe.shape("CCC","CSC","CHC");
		storageSignRecipe.setIngredient('C', Material.CHEST);
		storageSignRecipe.setIngredient('S', sign[i]);
		if (config.getBoolean("hardrecipe")) storageSignRecipe.setIngredient('H', Material.ENDER_CHEST);
		else storageSignRecipe.setIngredient('H', Material.CHEST);
		getServer().addRecipe(storageSignRecipe);
		}

		getServer().getPluginManager().registerEvents(this, this);
		if(config.getBoolean("no-bud")) new SignPhysicsEvent(this);
	}

	@Override
	public void onDisable(){}

	public boolean isStorageSign(ItemStack item) {
		if (item == null) return false;

		switch (item.getType()) {
		case OAK_SIGN:
		case BIRCH_SIGN:
		case SPRUCE_SIGN:
		case JUNGLE_SIGN:
		case ACACIA_SIGN:
		case DARK_OAK_SIGN:

			if (!item.getItemMeta().hasDisplayName()) return false;
			if (!item.getItemMeta().getDisplayName().matches("StorageSign")) return false;
			return item.getItemMeta().hasLore();
		default:
		}
		return false;
	}

	public boolean isStorageSign(Block block) {
		switch (block.getType()) {
		case OAK_SIGN:
		case OAK_WALL_SIGN:
		case BIRCH_SIGN:
		case BIRCH_WALL_SIGN:
		case SPRUCE_SIGN:
		case SPRUCE_WALL_SIGN:
		case JUNGLE_SIGN:
		case JUNGLE_WALL_SIGN:
		case ACACIA_SIGN:
		case ACACIA_WALL_SIGN:
		case DARK_OAK_SIGN:
		case DARK_OAK_WALL_SIGN:
			Sign sign = (Sign) block.getState();
			if (sign.getLine(0).matches("StorageSign")) return true;
			break;
		default:
		}
		return false;
	}

	public boolean isHorseEgg(ItemStack item){
		if(item.getType() != Material.GHAST_SPAWN_EGG) return false;
		if(item.getItemMeta().hasLore()) return true;
		return false;
	}


	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block;
		//手持ちがブロックだと叩いた看板を取得できないことがあるとか
		if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
			try {
				block = player.getTargetBlock((Set) null, 3);
			} catch (IllegalStateException ex) {
				return;
			}
		} else {
			block = event.getClickedBlock();
		}
		if (block == null) return;
		if(player.getGameMode() == GameMode.SPECTATOR) return;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

			if (!isStorageSign(block)) return;
			if(event.getHand() == EquipmentSlot.OFF_HAND) return;//一応
			event.setUseItemInHand(Result.DENY);
			event.setUseInteractedBlock(Result.DENY);
			if (!player.hasPermission("storagesign.use")) {
				player.sendMessage(ChatColor.RED + config.getString("no-permisson"));
				event.setCancelled(true);
				return;
			}
			Sign sign = (Sign) block.getState();
			StorageSign storageSign = new StorageSign(sign,block.getType());
			ItemStack itemMainHand = event.getItem();
			Material mat;

			//アイテム登録
			if (storageSign.getMaterial() == null || storageSign.getMaterial() == Material.AIR) {
				if(itemMainHand == null) return;//申し訳ないが素手はNG
				mat = itemMainHand.getType();
				if (isStorageSign(itemMainHand)) {
					storageSign.setMaterial(mat);
					storageSign.setDamage((short) 1);
				}
				else if (isHorseEgg(itemMainHand)){
					storageSign.setMaterial(Material.END_PORTAL);
					storageSign.setDamage((short) 1);
				}
				else if(mat == Material.STONE_SLAB){	
					storageSign.setMaterial(mat);
					storageSign.setDamage((short) 1);
				}
				else if (mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION)
				{
					storageSign.setMaterial(mat);
					PotionMeta potionMeta = (PotionMeta)itemMainHand.getItemMeta();
					PotionData potion = potionMeta.getBasePotionData();
					if(potion.isExtended()) storageSign.setDamage((short) 1);
					if(potion.isUpgraded()) storageSign.setDamage((short) 2);
					storageSign.setPotion(potion.getType());
				}
				else if (mat == Material.ENCHANTED_BOOK)
				{
					EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta)itemMainHand.getItemMeta();
					if(enchantMeta.getStoredEnchants().size() == 1) {
						Enchantment ench = enchantMeta.getStoredEnchants().keySet().toArray(new Enchantment[0])[0];
						storageSign.setMaterial(mat);
						storageSign.setDamage((short) enchantMeta.getStoredEnchantLevel(ench));
						storageSign.setEnchant(ench);
					}
				}else if(mat == Material.FIREWORK_ROCKET){
					storageSign.setMaterial(mat);
					FireworkMeta fireworkMeta = (FireworkMeta)itemMainHand.getItemMeta();
					storageSign.setDamage((short) fireworkMeta.getPower());
				}
				else if(mat == Material.WHITE_BANNER){
					storageSign.setMaterial(mat);
					BannerMeta bannerMeta = (BannerMeta)itemMainHand.getItemMeta();
					if(bannerMeta.getPatterns().size() == 8) {
						ominousBannerMeta = bannerMeta;
						storageSign.setDamage((short) 8);
					}
				}
				else
				{
					storageSign.setMaterial(mat);
					storageSign.setDamage(itemMainHand.getDurability());
				}

				for (int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
				sign.update();
				return;
			}

			if (isStorageSign(itemMainHand)) {
				//看板合成
				StorageSign itemSign = new StorageSign(itemMainHand);
				if (storageSign.getContents().isSimilar(itemSign.getContents()) && config.getBoolean("manual-import")) {
					storageSign.addAmount(itemSign.getAmount() * itemSign.getStackSize());
					itemSign.setAmount(0);
					player.getInventory().setItemInMainHand(itemSign.getStorageSign());
				}//空看板収納
				else if (itemSign.isEmpty() && storageSign.getMaterial() == itemSign.getSmat() && storageSign.getDamage() == 1 && config.getBoolean("manual-import")) {
					if (player.isSneaking() ) {
						storageSign.addAmount(itemMainHand.getAmount());
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					} else for (int i=0; i<player.getInventory().getSize(); i++) {
						ItemStack item = player.getInventory().getItem(i);
						if (storageSign.isSimilar(item)) {
							storageSign.addAmount(item.getAmount());
							player.getInventory().clear(i);
						}
					}
				}//中身分割機能
				else if (itemSign.isEmpty() && storageSign.getAmount() > itemMainHand.getAmount() && config.getBoolean("manual-export")) {
					itemSign.setMaterial(storageSign.getMaterial());
					itemSign.setDamage(storageSign.getDamage());
					itemSign.setEnchant(storageSign.getEnchant());
					itemSign.setPotion(storageSign.getPotion());

					int limit = config.getInt("divide-limit");

					if (limit > 0 && storageSign.getAmount() > limit * (itemSign.getStackSize() + 1)) itemSign.setAmount(limit);
					else itemSign.setAmount(storageSign.getAmount() / (itemSign.getStackSize() + 1));
					player.getInventory().setItemInMainHand(itemSign.getStorageSign());
					storageSign.setAmount(storageSign.getAmount() - (itemSign.getStackSize() * itemSign.getAmount()));//余りは看板に引き受けてもらう
				}
				for (int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
				sign.update();
				return;
			}

            //ここから搬入
            if (storageSign.isSimilar(itemMainHand)) {
                if (!config.getBoolean("manual-import")) return;
                if (player.isSneaking()) {
                    storageSign.addAmount(itemMainHand.getAmount());
                    player.getInventory().clear(player.getInventory().getHeldItemSlot());
                } else for (int i=0; i<player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (storageSign.isSimilar(item)) {
                        storageSign.addAmount(item.getAmount());
                        player.getInventory().clear(i);
                    }
                }

                player.updateInventory();
            } else if (config.getBoolean("manual-export"))/*放出*/ {
                if (storageSign.isEmpty()) return;
                ItemStack item = storageSign.getContents();

                int max = item.getMaxStackSize();

                if (player.isSneaking()) storageSign.addAmount(-1);
                else if (storageSign.getAmount() > max) {
                    item.setAmount(max);
                    storageSign.addAmount(-max);
                } else {
                    item.setAmount(storageSign.getAmount());
                    storageSign.setAmount(0);
                }

                Location loc = player.getLocation();
                loc.setY(loc.getY() + 0.5);
                player.getWorld().dropItem(loc, item);
            }

            for (int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
            sign.update();
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled())return;
        Sign sign = (Sign) event.getBlock().getState();

        if (sign.getLine(0).matches("StorageSign"))/*変更拒否*/ {
            event.setLine(0, sign.getLine(0));
            event.setLine(1, sign.getLine(1));
            event.setLine(2, sign.getLine(2));
            event.setLine(3, sign.getLine(3));
            sign.update();
        } else if (event.getLine(0).equalsIgnoreCase("storagesign"))/*書き込んで生成禁止*/ {
            if (event.getPlayer().hasPermission("storagesign.create")) {
                event.setLine(0, "StorageSign");
                sign.update();
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + config.getString("no-permisson"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Map<Location, StorageSign> breakSignMap = new HashMap<>();
        if (isStorageSign(block)) breakSignMap.put(block.getLocation(), new StorageSign((Sign)block.getState(),block.getType()));

        for (int i=0; i<5; i++) {//東西南北で判定
            BlockFace[] face = {BlockFace.UP,BlockFace.SOUTH,BlockFace.NORTH,BlockFace.EAST,BlockFace.WEST};
            block = event.getBlock().getRelative(face[i]);
            if (i==0 && isSignPost(block) && isStorageSign(block)) breakSignMap.put(block.getLocation(), new StorageSign((Sign)block.getState(),block.getType()));
            else if(isWallSign(block) && ((WallSign) block.getBlockData()).getFacing() == face[i] && isStorageSign(block)) breakSignMap.put(block.getLocation(), new StorageSign((Sign)block.getState(),block.getType()));
        }
        if (breakSignMap.isEmpty()) return;
        if (!event.getPlayer().hasPermission("storagesign.break")) {
            event.getPlayer().sendMessage(ChatColor.RED + config.getString("no-permisson"));
            event.setCancelled(true);
            return;
        }

        for (Location loc : breakSignMap.keySet()) {
            StorageSign sign = breakSignMap.get(loc);
            Location loc2 = loc;
            loc2.add(0.5, 0.5, 0.5);//中心にドロップさせる
            loc.getWorld().dropItem(loc2, sign.getStorageSign());
            loc.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled() || !isStorageSign(event.getItemInHand())) return;
		
		Player player = event.getPlayer();
		
        if (!player.hasPermission("storagesign.place")) {
			player.sendMessage(ChatColor.RED + config.getString("no-permisson"));
			
			event.setCancelled(true);
			
            return;
		}
		
		StorageSign storageSign = new StorageSign(event.getItemInHand());
		
		Sign sign = (Sign)event.getBlock().getState();
		
		for (int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
		
		// ダークオーク看板の文字色を白に
		if(sign.getType() == Material.DARK_OAK_SIGN || sign.getType() == Material.DARK_OAK_WALL_SIGN){
			sign.setColor(DyeColor.WHITE);
		}

		sign.update();
		
        player.closeInventory();//何故か仕事しない　<-　onBlockPlace関数が終了してから編集画面を開くらしい
	}
    
    
    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        if (event.isCancelled()) return;
        BlockState[] blockInventory =new BlockState[2];
        Boolean flag = false;
        Sign sign = null;
        StorageSign storageSign = null;
        ItemStack item = event.getItem();
        if (config.getBoolean("auto-import")) {
        	if (event.getDestination().getLocation() == null);//コンポスター用に生成された一時インベントリ
        	else if (event.getDestination().getHolder() instanceof Minecart);//何もしない
            else if (event.getDestination().getHolder() instanceof DoubleChest) {
                DoubleChest lc = (DoubleChest)event.getDestination().getHolder();
                blockInventory[0] = (BlockState) lc.getLeftSide();
                blockInventory[1] = (BlockState) lc.getRightSide();
            } else {
                blockInventory[0] = (BlockState) event.getDestination().getHolder();
            }

            importLoop:
                for (int j=0; j<2; j++) {
                    if (blockInventory[j] == null) break;
                    for (int i=0; i<5; i++) {
                        BlockFace[] face = {BlockFace.UP,BlockFace.SOUTH,BlockFace.NORTH,BlockFace.EAST,BlockFace.WEST};
                        Block block = blockInventory[j].getBlock().getRelative(face[i]);
                        if (i==0 && isSignPost(block) && isStorageSign(block)) {
                        	if(item.getType() == Material.WHITE_BANNER) {
                        		
                        	}
                            sign = (Sign) block.getState();
                            storageSign = new StorageSign(sign,block.getType());
                            if (storageSign.isSimilar(item)) {
                                flag = true;
                                break importLoop;
                            }
                        } else if (i != 0 && isWallSign(block) &&  ((WallSign) block.getBlockData()).getFacing() == face[i] && isStorageSign(block)) {
                            sign = (Sign) block.getState();
                            storageSign = new StorageSign(sign,block.getType());
                            if (storageSign.isSimilar(item)) {
                                flag = true;
                                break importLoop;
                            }
                        }
                    }
                }
            //搬入先が見つかった(搬入するとは言ってない)
            if (flag) importSign(sign, storageSign, item, event.getDestination());
        }

        //搬出用にリセット
        if (config.getBoolean("auto-export")) {
            blockInventory[0] = null;
            blockInventory[1] = null;
            flag = false;
        	if (event.getSource().getLocation() == null);//一時インベントリ
        	else if (event.getSource().getHolder() instanceof Minecart);
            else if (event.getSource().getHolder() instanceof DoubleChest) {
                DoubleChest lc = (DoubleChest)event.getSource().getHolder();
                blockInventory[0] = (BlockState) lc.getLeftSide();
                blockInventory[1] = (BlockState) lc.getRightSide();
            } else {
                blockInventory[0] = (BlockState) event.getSource().getHolder();
            }

            exportLoop:
                for (int j=0; j<2; j++) {
                    if (blockInventory[j] == null) break;
                    for (int i=0; i<5; i++) {
                        BlockFace[] face = {BlockFace.UP,BlockFace.SOUTH,BlockFace.NORTH,BlockFace.EAST,BlockFace.WEST};
                        Block block = blockInventory[j].getBlock().getRelative(face[i]);
                        if (i==0 && isSignPost(block) && isStorageSign(block)) {
                        	sign = (Sign) block.getState();
                        	storageSign = new StorageSign(sign,block.getType());
                        	if (storageSign.isSimilar(item)) {
                        		flag = true;
                        		break exportLoop;
                        	}
                        } else if (i != 0 && isWallSign(block) &&  ((WallSign) block.getBlockData()).getFacing() == face[i] && isStorageSign(block)) {
                        	sign = (Sign) block.getState();
                        	storageSign = new StorageSign(sign,block.getType());
                        	if (storageSign.isSimilar(item)) {
                        		flag = true;
                        		break exportLoop;
                        	}
                        }
                    }
                }
            if (flag) exportSign(sign, storageSign, item, event.getSource(), event.getDestination());
        }
    }

	private void importSign(Sign sign, StorageSign storageSign, ItemStack item, Inventory inv) {
        //搬入　条件　1スタック以上アイテムが入っている
        if (inv.containsAtLeast(item, item.getMaxStackSize())) {
            inv.removeItem(item);
            storageSign.addAmount(item.getAmount());
        }
        for (int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
        sign.update();
    }

    //搬出先ブロックに枠指定があると事故る
    private void exportSign(Sign sign, StorageSign storageSign, ItemStack item, Inventory inv, Inventory dest) {
        if (!inv.containsAtLeast(item, item.getMaxStackSize()) && storageSign.getAmount() >= item.getAmount()) {
        	int stacks = 0;
        	int amount = 0;
        	ItemStack[] contents = dest.getContents();

        	if(dest.getType() == InventoryType.BREWING){
        		switch(item.getType()){
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
                		if(inv.getLocation().getBlockY() > dest.getLocation().getBlockY()){//上から搬入
                			if(contents[3] != null && !item.isSimilar(contents[3])) return;//他のアイテムが詰まってる
                			else break;
                		}else return;


        			case BLAZE_POWDER://横or上
                		if(inv.getLocation().getBlockY() > dest.getLocation().getBlockY()){//上から搬入
                			if(contents[3] != null && !item.isSimilar(contents[3])) return;//他のアイテムが詰まってる
                			else break;
                		}else if(inv.getLocation().getBlockY() == dest.getLocation().getBlockY()){//横
                			if(contents[4] != null && contents[4].getAmount() == 64) return;//パウダー詰まり
                			else break;
                		}else return;

        			case POTION:
        			case SPLASH_POTION:
        			case LINGERING_POTION://横or下
                		if(inv.getLocation().getBlockY() <= dest.getLocation().getBlockY()){
                			if(contents[0] != null && contents[1] != null && contents[2] != null) return;
                			else break;
                		}else return;
        			default://ロスト回避
        				return;

        		}


        	}else if(dest.getType() == InventoryType.FURNACE || dest.getType() == InventoryType.BLAST_FURNACE || dest.getType() == InventoryType.SMOKER){
        		if(inv.getLocation().getBlockY() > dest.getLocation().getBlockY()){//上から搬入
        			if(contents[0] != null && !item.isSimilar(contents[0])) return;//他のアイテムが詰まってる
        		}else{//横から(下から)
        			if(!item.getType().isFuel() || contents[1] != null && !item.isSimilar(contents[1])) return;//燃料以外 or 他のアイテムが詰まってる
        		}
        	}
        	 for(int i=0; i< contents.length; i++){//PANPANによるロスト回避
        		if(item.isSimilar(contents[i])){
        			stacks++;
        			amount += contents[i].getAmount();
        		}
        	}
        	if(amount == stacks * item.getMaxStackSize() && dest.firstEmpty() == -1) return;


            inv.addItem(item);
            storageSign.addAmount(-item.getAmount());
        }
        for (int i=0; i<4; i++) sign.setLine(i, storageSign.getSigntext(i));
        sign.update();
    }

    @EventHandler
    public void onPlayerCraft(CraftItemEvent event) {
        if (isStorageSign(event.getCurrentItem()) && !event.getWhoClicked().hasPermission("storagesign.craft")) {
            ((CommandSender) event.getWhoClicked()).sendMessage(ChatColor.RED + config.getString("no-permisson"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent event) {//ホッパーに投げ込まれたとき
        if (event.isCancelled() || !config.getBoolean("auto-import")) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BlockState) {
            Sign sign = null;
            StorageSign storageSign = null;
            boolean flag = false;
            for (int i=0; i<5; i++) {
                BlockFace[] face = {BlockFace.UP,BlockFace.SOUTH,BlockFace.NORTH,BlockFace.EAST,BlockFace.WEST};
                Block block = ((BlockState)holder).getBlock().getRelative(face[i]);
                if (i==0 && isSignPost(block) && isStorageSign(block)) {
                    sign = (Sign) block.getState();
                    storageSign = new StorageSign(sign,block.getType());
                    if (storageSign.isSimilar(event.getItem().getItemStack())) {
                        flag = true;
                        break;
                    }
                } else if (i != 0 && isWallSign(block) && ((WallSign) block.getBlockData()).getFacing() == face[i] && isStorageSign(block)) {//BlockFaceに変更？(めんどい)
                    sign = (Sign) block.getState();
                    storageSign = new StorageSign(sign,block.getType());
                    if (storageSign.isSimilar(event.getItem().getItemStack())) {
                        flag = true;
                        break;
                    }
                }
            }
            if (flag) importSign(sign, storageSign, event.getItem().getItemStack(), event.getInventory());
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
    	if(event.isCancelled()) return;
    	if(event.getEntityType() == EntityType.PLAYER && config.getBoolean("autocollect")){
    		Player player = (Player) event.getEntity();
    		PlayerInventory playerInv = player.getInventory();
    		ItemStack item = event.getItem().getItemStack();
    		StorageSign storagesign = null;
    		//ここでは、エラーを出さずに無視する
    		if(!player.hasPermission("storagesign.autocollect")) return;
    		if(isStorageSign(playerInv.getItemInMainHand())){
    			storagesign = new StorageSign(playerInv.getItemInMainHand());
    			if(storagesign.getContents() != null){

    				if (storagesign.isSimilar(item) && playerInv.containsAtLeast(item, item.getMaxStackSize()) && storagesign.getStackSize() == 1) {
    					storagesign.addAmount(item.getAmount());

    					playerInv.removeItem(item);//1.9,10ではバグる？
    					playerInv.setItemInMainHand(storagesign.getStorageSign());
    					player.updateInventory();
    					//event.getItem().remove();
    					//player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.5f);
    					//event.setCancelled(true);
    					return;
    				}
    			}
    		}if(isStorageSign(playerInv.getItemInOffHand())){//メインハンドで回収されなかった時
    			storagesign = new StorageSign(playerInv.getItemInOffHand());
    			if(storagesign.getContents() != null){

    				if (storagesign.isSimilar(item) && playerInv.containsAtLeast(item, item.getMaxStackSize()) && storagesign.getStackSize() == 1) {
    					storagesign.addAmount(item.getAmount());
    					playerInv.removeItem(item);
    					playerInv.setItemInOffHand(storagesign.getStorageSign());
    					player.updateInventory();
    					//event.getItem().remove();
    					//player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.5f);
    					//event.setCancelled(true);
    					return;
    				}
    			}
    		}
    	}//SSをプレイヤー以外拾えなくする
    	if(event.getEntityType() != EntityType.PLAYER){
    		Item item = event.getItem();
    		if(isStorageSign(item.getItemStack())){
    			item.setPickupDelay(20);//毎tickキャンセルしてたら重そう
    			event.setCancelled(true);
    		}
    	}
    }
    
    private boolean isSignPost(Block block) {
    	Material mat = block.getType();
    	switch(mat) {
    	case OAK_SIGN:
    	case BIRCH_SIGN:
    	case SPRUCE_SIGN:
    	case JUNGLE_SIGN:
    	case ACACIA_SIGN:
    	case DARK_OAK_SIGN:
    		return true;
    	default:
    	}
    	return false;
    }
    
    private boolean isWallSign(Block block) {
    	Material mat = block.getType();
    	switch(mat) {
    	case OAK_WALL_SIGN:
    	case BIRCH_WALL_SIGN:
    	case SPRUCE_WALL_SIGN:
    	case JUNGLE_WALL_SIGN:
    	case ACACIA_WALL_SIGN:
    	case DARK_OAK_WALL_SIGN:
    		return true;
    	default:
    	}
    	return false;
    }

}