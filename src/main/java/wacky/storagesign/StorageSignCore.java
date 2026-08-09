package wacky.storagesign;

import com.github.teruteru128.logger.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
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
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import wacky.storagesign.client.paper.PaperSSEvent;
import wacky.storagesign.client.spigot.SpigotSSEvent;
import wacky.storagesign.config.StorageSignNBTConfig;
import wacky.storagesign.runnnable.ExportSign;
import wacky.storagesign.signdefinition.SignDefinition;

public class StorageSignCore extends JavaPlugin implements Listener {

  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(
      StorageSignCore.class);
  public static BannerMeta ominousBannerMeta;
  public Logger logger;
  private boolean _fallingBlockSS;

  @Override
  public void onEnable() {
    // ConfigLoader初期化
    ConfigLoader.setup(this);

    // ロガーの初期設定
    String logLevel = ConfigLoader.getLogLevel();
    // ログ取得
    Logger.register(this, logLevel);
    logger = Logger.getInstance(this);

    logger.debug("★onEnable:Start");
//		logger.fatal("serverLog");
//		logger.error("errorLog");
//		logger.warn("warnLog");
//		logger.info("infoLog");
//		logger.debug("debugLog");
//		logger.trace("traceLog");

    //鯖別レシピが実装されたら
    logger.trace("hardrecipe:" + ConfigLoader.getHardRecipe());
    Iterator<Material> it = SignDefinition.sign_materials.iterator();
    while(it.hasNext()){
      Material mat = it.next();
      logger.trace("signRecipi name:" + mat);

      ShapedRecipe storageSignRecipe = new ShapedRecipe(new NamespacedKey(this, "ssr" + mat.toString()),
          StorageSign.emptySign(mat));
      //ShapedRecipe storageSignRecipe = new ShapedRecipe(StorageSign.emptySign());
      storageSignRecipe.shape("CCC", "CSC", "CHC");
      storageSignRecipe.setIngredient('C', Material.CHEST);
      storageSignRecipe.setIngredient('S', mat);

      if (ConfigLoader.getHardRecipe()) {
        storageSignRecipe.setIngredient('H', Material.ENDER_CHEST);
      } else {
        storageSignRecipe.setIngredient('H', Material.CHEST);
      }

      // レシピのグループを設定する
      storageSignRecipe.setCategory(CraftingBookCategory.MISC);
      storageSignRecipe.setGroup("StorageSign");

      getServer().addRecipe(storageSignRecipe);
      logger.trace(mat + "StorageSign Recipe added.");
    }

    logger.trace("setEvent");
    boolean isPaper = false;
    try {
      Class.forName("io.papermc.paper.event.player.PlayerOpenSignEvent");
      isPaper = true;
      logger.info("ServerType:Paper");
    } catch (ClassNotFoundException e) {
      // Spigotの場合確定で起きるので、無視する
      logger.info("ServerType:Spigot");
    }
    getServer().getPluginManager().registerEvents(this, this);

    // Paper専用ハンドラの設定
    if(isPaper) {
      PaperSSEvent event = new PaperSSEvent();
      event.init(logger);
      getServer().getPluginManager().registerEvents(event, this);
    } else {
      SpigotSSEvent event = new SpigotSSEvent();
      event.init(logger);
      getServer().getPluginManager().registerEvents(event, this);
    }

    logger.trace("no-bud:" + ConfigLoader.getNoBud());
    if (ConfigLoader.getNoBud()) {
      logger.trace("no-bud is True.");
      new SignPhysicsEvent(this, logger);
    }
    _fallingBlockSS = ConfigLoader.getFallingBlockItemSs();

    // storageSignNBT.configのセットアップ
    StorageSignNBTConfig ssConfigClass = new StorageSignNBTConfig(this);
    ssConfigClass.init();

    String version = Bukkit.getBukkitVersion().split("-")[0];
    version = version.replaceFirst("\\.build\\.\\d+$", "");
    String nbt = ssConfigClass.getOminousBannerNBT(version);

    // バナーデバッグが入ってる場合は、アイテムスタックを設定しない
    if(!ConfigLoader.getBannerDebug()) {
      ItemStack banner = new ItemStack(Material.WHITE_BANNER);

      BannerMeta meta = (BannerMeta) banner.getItemMeta();

      meta.itemName(Component.translatable("block.minecraft.ominous_banner"));

      meta.setPatterns(List.of(
          new Pattern(DyeColor.CYAN, PatternType.RHOMBUS),
          new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM),
          new Pattern(DyeColor.GRAY, PatternType.STRIPE_CENTER),
          new Pattern(DyeColor.LIGHT_GRAY, PatternType.BORDER),
          new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE),
          new Pattern(DyeColor.LIGHT_GRAY, PatternType.HALF_HORIZONTAL),
          new Pattern(DyeColor.LIGHT_GRAY, PatternType.CIRCLE),
          new Pattern(DyeColor.BLACK, PatternType.BORDER)
      ));

      meta.setRarity(ItemRarity.UNCOMMON);

      try {
        byte[] data = Base64.getDecoder().decode(nbt);

        BukkitObjectInputStream dataInput =
            null;
        dataInput = new BukkitObjectInputStream(new ByteArrayInputStream(data));
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        ominousBannerMeta = meta;
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

    }

    logger.debug("★onEnable:End");
  }

  @Override
  public void onDisable() {
  }




  public boolean isHorseEgg(ItemStack item) {
    logger.debug("isHorseEgg: Start");
    if (item.getType() != Material.GHAST_SPAWN_EGG) {
      logger.debug("This item isn't HorseEgg.");
      return false;
    }
    if (item.getItemMeta().hasLore()) {
      logger.debug("This item is HorseEgg.");
      return true;
    }

    logger.debug("This item isn't HorseEgg.");
    return false;
  }


  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    logger.debug("★onPlayerInteract: Start");
    Player player = event.getPlayer();
    Block block;

    // デバッグオプション入ってる場合は、バナーのItemMetaを取得する
    if(ConfigLoader.getBannerDebug()){
      ItemStack banItem = event.getItem();

      try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput =
            new BukkitObjectOutputStream(outputStream);
        dataOutput.writeObject(banItem);
        dataOutput.close();
        String encoded = Base64.getEncoder()
            .encodeToString(outputStream.toByteArray());
        logger.trace("bannerMeta:" + encoded);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }

    logger.trace("event.useInteractedBlock() == Result.DENY :" + (event.useInteractedBlock()
        == Result.DENY));
    logger.trace("event.getAction() == Action.RIGHT_CLICK_AIR:" + (event.getAction()
        == Action.RIGHT_CLICK_AIR));
    //手持ちがブロックだと叩いた看板を取得できないことがあるとか
    if (event.useInteractedBlock() == Result.DENY && event.getAction() == Action.RIGHT_CLICK_AIR) {
      try {
        logger.debug("try getTargetBlock.");
        block = player.getTargetBlock((Set) null, 3);
      } catch (IllegalStateException ex) {
        java.util.logging.Logger log = logger.getLogger();
        log.log(Level.SEVERE, "★getTargetBlock is Error. Trace", ex);
        return;
      }
    } else {
      logger.debug("ClickedBlock :" + event.getClickedBlock());
      block = event.getClickedBlock();
    }
    logger.trace("block == null:" + (block == null));
    if (block == null) {
      logger.debug("★block is null.");
      return;
    }

    logger.trace("player.getGameMode() == GameMode.SPECTATOR:" + (player.getGameMode()
        == GameMode.SPECTATOR));
    if (player.getGameMode() == GameMode.SPECTATOR) {
      logger.debug("★UserGameMode is Spectator.");
      return;
    }

    logger.trace("event.getAction():" + event.getAction());
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK
        || event.getAction() == Action.RIGHT_CLICK_AIR) {
      logger.debug("UserAction is RIGHT_CLICK_BLOCK or RIGHT_CLICK_AIR");

      boolean clickBlockIsSS = StorageSign.isStorageSign(block, logger);
      logger.trace("clickBlockIsSS: " + clickBlockIsSS);
      if (!clickBlockIsSS) {
        logger.debug("★This Block not StorageSign.");
        return;
      }
      if (event.getHand() == EquipmentSlot.OFF_HAND) {
        logger.debug("★This hand is OFF_HAND.");
        //オフハンドでスニーク動作でSSを触り、かつ持ってるアイテムがSSのときは、看板が張り付かないようにイベントをキャンセルする
        boolean offItemIsSS = StorageSign.isStorageSign(event.getItem(), logger);
        logger.trace("player.isSneaking(): " + player.isSneaking());
        logger.trace("offItemIsSS: " + offItemIsSS);
        if (player.isSneaking() && offItemIsSS) {
          event.setUseItemInHand(Result.DENY);
          event.setUseInteractedBlock(Result.DENY);
        }
        return;
      }
      event.setUseItemInHand(Result.DENY);
      event.setUseInteractedBlock(Result.DENY);

      logger.trace("!player.hasPermission(\"storagesign.use\"):  " + !player.hasPermission(
          "storagesign.use"));
      if (!player.hasPermission("storagesign.use")) {
        logger.debug("★This User hasn't permission.storagesign.use");
        player.sendMessage(ChatColor.BLACK + ConfigLoader.getNoPermission());
        event.setCancelled(true);
        return;
      }
      Sign sign = (Sign) block.getState();
      StorageSign storageSign = new StorageSign(sign, block.getType(), logger);
      ItemStack itemMainHand = event.getItem();
      Material mat;

      logger.debug("Check Event Type.");
      logger.trace("sign: " + sign);
      logger.trace("storageSign: " + storageSign);
      logger.trace("itemMainHand: " + itemMainHand);
      logger.trace("storageSign.getMaterial() == null: " + (storageSign.getMaterial() == null));
      logger.trace("storageSign.getMaterial() == Material.AIR: " + (storageSign.getMaterial()
          == Material.AIR));
      //アイテム登録
      logger.debug("check Item Regist.");
      if (storageSign.getMaterial() == null || storageSign.getMaterial() == Material.AIR) {
        logger.debug("SS Material Regist.");
        logger.trace("itemMainHand:" + itemMainHand);
        logger.trace("itemMainHand == null: " + (itemMainHand == null));
        if (itemMainHand == null) {
          logger.debug("★User MainHand is Null.");
          //申し訳ないが素手はNG
          return;
        }
        mat = itemMainHand.getType();
        boolean mainHandIsSS = StorageSign.isStorageSign(itemMainHand, logger);
        boolean mainHandIsHE = isHorseEgg(itemMainHand);
        logger.trace("isStorageSign(itemMainHand): " + mainHandIsSS);
        logger.trace("isHorseEgg(itemMainHand): " + mainHandIsHE);
        logger.trace("mat: " + mat);
        if (mainHandIsSS) {
          logger.debug("main hand has StorageSign.");
          storageSign.setMaterial(mat);
          storageSign.setDamage((short) 1);
        } else if (mainHandIsHE) {
          logger.debug("main hand has HorseEgg.");
          storageSign.setMaterial(Material.END_PORTAL);
          storageSign.setDamage((short) 1);
        } else if (mat == Material.STONE_SLAB) {
          logger.debug("main hand has STONE_SLAB.");
          storageSign.setMaterial(mat);
          storageSign.setDamage((short) 1);
        } else if (mat == Material.POTION || mat == Material.SPLASH_POTION
            || mat == Material.LINGERING_POTION) {
          logger.debug("main hand has PotionSeries.");
          storageSign.setMaterial(mat);
          PotionMeta potionMeta = (PotionMeta) itemMainHand.getItemMeta();
          PotionType pot = potionMeta.getBasePotionType();
          storageSign.setDamage(Short.parseShort(PotionInfo.getPotionTypeCode(pot)));
          storageSign.setPotion(pot);
        } else if (mat == Material.OMINOUS_BOTTLE) {
          logger.debug("main hand has OMINOUS_BOTTLE.");
          storageSign.setMaterial(mat);
          storageSign.setDamage(OmniousBottleInfo.GetAmplifierWithMeta(itemMainHand.getItemMeta()));
        } else if (mat == Material.ENCHANTED_BOOK) {
          logger.debug("main hand has EnchantedBook.");
          EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) itemMainHand.getItemMeta();
          if (enchantMeta.getStoredEnchants().size() == 1) {
            Enchantment ench = enchantMeta.getStoredEnchants().keySet()
                .toArray(new Enchantment[0])[0];
            storageSign.setMaterial(mat);
            storageSign.setDamage((short) enchantMeta.getStoredEnchantLevel(ench));
            storageSign.setEnchant(ench);
          }
        } else if (mat == Material.FIREWORK_ROCKET) {
          logger.debug("main hand has FireRocket.");
          storageSign.setMaterial(mat);
          FireworkMeta fireworkMeta = (FireworkMeta) itemMainHand.getItemMeta();
          short power = (short) fireworkMeta.getPower();
          // 21.4より飛翔1はダメージ設定要らなくなった。
          // 過去のものだとdamage1と0が混在してるので、強制的にダメージを設定しないようにする。
          if(1 < power){
            storageSign.setDamage(power);
          }
        } else if (mat == Material.WHITE_BANNER) {
          logger.debug("main hand has WhiteBanner.");
          storageSign.setMaterial(mat);
          BannerMeta bannerMeta = (BannerMeta) itemMainHand.getItemMeta();
          if (bannerMeta.getPatterns().size() == 8) {
            ominousBannerMeta = bannerMeta;//襲撃バナー登録
            storageSign.setDamage((short) 8);
          }
        } else {
          logger.debug("main hand has " + mat);
          storageSign.setMaterial(mat);
          var meta = itemMainHand.getItemMeta();

          logger.trace("meta instanceof Damageable dam" + (meta instanceof Damageable dam));
          if (meta instanceof Damageable dam) {
            logger.debug("This Item has Damage.damage:" + dam.getDamage());
            storageSign.setDamage((short) dam.getDamage());
          }
        }

        logger.debug("SignTextUpdate.");
        for (int i = 0; i < 4; i++) {
          logger.trace(" [i]:" + i + ". signText:" + storageSign.getSigntext(i));
          sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
        }
        sign.update();

        logger.debug("★ItemRegist: End");
        return;
      }

      boolean isStorageSign = StorageSign.isStorageSign(itemMainHand, logger);
      logger.trace("isStorageSign:" + isStorageSign);
      if (isStorageSign) {
        //看板合成
        logger.debug("Item move to User StorageSign");
        StorageSign itemSign = new StorageSign(itemMainHand, logger);
        logger.trace("itemSign:" + itemSign);
        logger.trace("storageSign.getContents().isSimilar(itemSign.getContents()) && config.getBoolean(\n"
            + "            \"manual-import\"):" + (storageSign.getContents().isSimilar(itemSign.getContents()) && ConfigLoader.getManualImport()));
        logger.trace("itemSign.isEmpty() && storageSign.getAmount() > itemMainHand.getAmount()\n"
            + "            && config.getBoolean(\"manual-export\")" + (itemSign.isEmpty() && storageSign.getAmount() > itemMainHand.getAmount()
            && ConfigLoader.getManualExport()));
        if (storageSign.getContents().isSimilar(itemSign.getContents()) && ConfigLoader.getManualImport()) {
          logger.debug("Sign store Items.");
          storageSign.addAmount(itemSign.getAmount() * itemSign.getStackSize());
          itemSign.setAmount(0);
          player.getInventory().setItemInMainHand(itemSign.getStorageSign());
        } else if (itemSign.isEmpty() && storageSign.getMaterial() == itemSign.getSmat()
            && storageSign.getDamage() == 1 && ConfigLoader.getManualImport()) {
          //空看板収納
          logger.debug("Empty Sign store.");
          logger.trace("player.isSneaking()" + player.isSneaking());
          if (player.isSneaking()) {
            logger.debug("Player is Sneaking.");
            storageSign.addAmount(itemMainHand.getAmount());
            player.getInventory().clear(player.getInventory().getHeldItemSlot());
          } else {
            logger.debug("store all Empty Sign.SearchPlayer Inventory.");
            // アイテム内でサインを探す
            for (int i = 0; i < player.getInventory().getSize(); i++) {
              ItemStack item = player.getInventory().getItem(i);
              boolean isSimilar = storageSign.isSimilar(item);
              logger.trace("storageSign.isSimilar(item)" + isSimilar);
              if (isSimilar) {
                logger.debug("find Empty Sign.");
                storageSign.addAmount(item.getAmount());
                player.getInventory().clear(i);
              }
            }
          }
        } else if (itemSign.isEmpty() && storageSign.getAmount() > itemMainHand.getAmount()
            && ConfigLoader.getManualExport()) {
          //中身分割機能
          logger.debug("Export Item to Empty Sign.");
          itemSign.setMaterial(storageSign.getMaterial());
          itemSign.setDamage(storageSign.getDamage());
          itemSign.setEnchant(storageSign.getEnchant());
          itemSign.setPotion(storageSign.getPotion());

          int limit;
          if (player.isSneaking()){
            limit = ConfigLoader.getSneakDivideLimit();
          } else {
            limit = ConfigLoader.getDivideLimit();
          }

          logger.trace("limit > 0 && storageSign.getAmount() > limit * (itemSign.getStackSize() + 1)" + (limit > 0 && storageSign.getAmount() > limit * (itemSign.getStackSize() + 1)));
          if (limit > 0 && storageSign.getAmount() > limit * (itemSign.getStackSize() + 1)) {
            logger.debug("Item Export EmptySign divide-limit.");
            itemSign.setAmount(limit);
          } else {
            logger.debug("Item Export EmptySign Equality divide.");
            itemSign.setAmount(storageSign.getAmount() / (itemSign.getStackSize() + 1));
          }
          player.getInventory().setItemInMainHand(itemSign.getStorageSign());
          //余りは看板に引き受けてもらう
          storageSign.setAmount(storageSign.getAmount() - (itemSign.getStackSize()
              * itemSign.getAmount()));
        }
        logger.debug("Update StorageSign.");
        for (int i = 0; i < 4; i++) {
          logger.trace("set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
          sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
        }
        sign.update();
        logger.debug("★Item move to User StorageSign: End");
        return;
      }

      //ここから搬入
      boolean isMainSimiller = storageSign.isSimilar(itemMainHand);
      logger.debug("check manual Import.");
      logger.trace("storageSign.isSimilar(itemMainHand): " + isMainSimiller);
      logger.trace("config.getBoolean(\"manual-import\"): " + ConfigLoader.getManualImport());
      logger.trace("config.getBoolean(\"manual-export\"): " + ConfigLoader.getManualExport());
      if (isMainSimiller) {
        logger.debug("StorageSign Import.");

        logger.trace("!config.getBoolean(\"manual-import\"): " + !ConfigLoader.getManualImport());
        if (!ConfigLoader.getManualImport()) {
          logger.debug("★option:manual-import is False!");
          return;
        }
        logger.trace("player.isSneaking():" + player.isSneaking());
        if (player.isSneaking()) {
          logger.debug("player is Sneaking.Push 1 Item.");
          storageSign.addAmount(itemMainHand.getAmount());
          player.getInventory().clear(player.getInventory().getHeldItemSlot());

          logger.trace("isDye(itemMainHand):" + isDye(itemMainHand));
          logger.trace("isSac(itemMainHand):" + isSac(itemMainHand));
          if (isDye(itemMainHand)) {
            logger.debug("mainHandItem is Dye.");
            sign.getSide(Side.FRONT).setColor(getDyeColor(itemMainHand)); //同色用
          }
          if (isSac(itemMainHand)) {
            logger.debug("mainHandItem is Sac.");
            sign.getSide(Side.FRONT).setGlowingText(isGlowSac(itemMainHand)); //イカスミ用
          }
        } else {
          logger.debug("push Stack Item.");
          for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            boolean itemSimilar = storageSign.isSimilar(item);
            logger.trace(" item: " + item);
            logger.trace(" storageSign.isSimilar(item): " + itemSimilar);
            if (itemSimilar) {
              logger.debug(" same Item Found.Push Item to StorageSign.");
              storageSign.addAmount(item.getAmount());
              player.getInventory().clear(i);
            }
          }
          logger.debug("push Stack Item.End.");
        }

        player.updateInventory();
      } else if (ConfigLoader.getManualExport())/*放出*/ {
        logger.debug("Export StorageSign Item.");

        boolean isDye = false;
        boolean isSac = false;
        if (itemMainHand != null){
          isDye = isDye(itemMainHand);
          isSac = isSac(itemMainHand);
        }
        logger.trace("itemMainHand:" + itemMainHand);
        logger.trace("itemMainHand != null && isDye:" + (itemMainHand != null && isDye));
        logger.trace("itemMainHand != null && isSac:" + (itemMainHand != null && isSac));
        //染料の場合、放出せずに看板に色がつく
        if (itemMainHand != null && isDye) {
          logger.debug("★Set SignColor.");
          event.setUseItemInHand(Result.ALLOW);
          //最初にDENYにしてたので戻す、同色染料が使えない。
          event.setUseInteractedBlock(Result.ALLOW);
          return;
        } else if (itemMainHand != null && isSac) {
          logger.debug("★Set Sac.");
          event.setUseItemInHand(Result.ALLOW);
          event.setUseInteractedBlock(Result.ALLOW);
          return;
        } else if (storageSign.isEmpty()) {
          logger.debug("★StorageSign is Empty.");
          return;
        }
        ItemStack item = storageSign.getContents();
        int max = item.getMaxStackSize();

        logger.trace("player.isSneaking(): " + player.isSneaking());
        logger.trace("storageSign.getAmount() > max: " + (storageSign.getAmount() > max));
        if (player.isSneaking()) {
          logger.debug("Player is Sneaking.Get 1 Item.");
          storageSign.addAmount(-1);
        } else if (storageSign.getAmount() > max) {
          logger.debug("Sign Items bigger than 1Stack.Get 1Stack Items.");
          item.setAmount(max);
          storageSign.addAmount(-max);
        } else {
          logger.debug("Get 1Stack Items.");
          item.setAmount(storageSign.getAmount());
          storageSign.setAmount(0);
        }

        logger.debug("drop Item.");
        Location loc = player.getLocation();
        loc.setY(loc.getY() + 0.5);
        player.getWorld().dropItem(loc, item);
      }

      logger.debug("SetSignText.");
      for (int i = 0; i < 4; i++) {
        logger.trace(" set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
        sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
      }

      logger.debug("update Sign.");
      sign.update();
    }

    logger.debug("★onPlayerInteract: End");
  }

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    logger.debug("★onSignChangeEvent:Start");

    logger.trace("event.isCancelled(): " + event.isCancelled());
    if (event.isCancelled()) {
      logger.debug("★this Event is Cancelled!");
      return;
    }
    Sign sign = (Sign) event.getBlock().getState();

    logger.trace("sign.getSide(Side.FRONT).getLine(0):" + sign.getSide(Side.FRONT).getLine(0));
    if (sign.getSide(Side.FRONT).getLine(0).matches("StorageSign"))/*変更拒否*/ {
      event.setLine(0, sign.getSide(Side.FRONT).getLine(0));
      event.setLine(1, sign.getSide(Side.FRONT).getLine(1));
      event.setLine(2, sign.getSide(Side.FRONT).getLine(2));
      event.setLine(3, sign.getSide(Side.FRONT).getLine(3));
      sign.update();
    } else if (event.getLine(0).equalsIgnoreCase("storagesign"))/*書き込んで生成禁止*/ {
      logger.debug("Line 0 Str is storagesign");

      logger.trace("event.getPlayer().hasPermission(\"storagesign.create\")" + event.getPlayer().hasPermission("storagesign.create"));
      if (event.getPlayer().hasPermission("storagesign.create")) {
        logger.debug("This user has Permission.storagesign.create. make StorageSign.");
        event.setLine(0, "StorageSign");
        sign.update();
      } else {
        logger.debug("This user hasn't Permission.storagesign.create");
        event.getPlayer().sendMessage(ChatColor.RED + ConfigLoader.getNoPermission());
        event.setCancelled(true);
      }
    }
    logger.debug("★onSignChangeEvent:End");
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockBreak(BlockBreakEvent event) {
    logger.debug("★onBlockBreak:Start");

    logger.trace("event.isCancelled(): " + event.isCancelled());
    if (event.isCancelled()) {
      logger.debug("★this Event is Cancelled!");
      return;
    }
    logger.trace("!event.getPlayer().hasPermission(\"storagesign.break\"): " + !event.getPlayer()
        .hasPermission("storagesign.break"));
    if (!event.getPlayer().hasPermission("storagesign.break")) {
      logger.debug("★This user hasn't Permission. storagesign.break.");
      event.getPlayer().sendMessage(ChatColor.RED + ConfigLoader.getNoPermission());
      event.setCancelled(true);
      return;
    }

    dropRelativeSign(event.getBlock());

    logger.debug("★onBlockBreak:End.");
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    logger.debug("★onBlockPlace: Start");

    boolean handItemIsSS = StorageSign.isStorageSign(event.getItemInHand(), logger);
    logger.trace("event.isCancelled(): " + event.isCancelled());
    logger.trace("!handItemIsSS: " + !handItemIsSS);
    if (event.isCancelled() || !handItemIsSS) {
      logger.debug("★this Event is Cancelled!");
      return;
    }
    Player player = event.getPlayer();
    logger.trace("!player.hasPermission(\"storagesign.place\"): " + !player.hasPermission("storagesign.place"));
    if (!player.hasPermission("storagesign.place")) {
      logger.debug("★This user hasn't Permission. storagesign.place.");
      player.sendMessage(ChatColor.RED + ConfigLoader.getNoPermission());
      event.setCancelled(true);
      return;
    }

    logger.debug("StorageSign Placed in World.");
    StorageSign storageSign = new StorageSign(event.getItemInHand(), logger);
    Sign sign = (Sign) event.getBlock().getState();
    for (int i = 0; i < 4; i++) {
      logger.trace(" set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
      sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
    }

    logger.trace("storageSign.getSmat() == Material.DARK_OAK_SIGN: " + (storageSign.getSmat() == Material.DARK_OAK_SIGN));
    if (storageSign.getSmat() == Material.DARK_OAK_SIGN) {
      logger.debug("This Sign is DarkOak Sign.0");
      sign.getSide(Side.FRONT).setColor(DyeColor.WHITE);//文字色を白くする
    }

    logger.debug("UpdateSign.");
    sign.update();

    //時差発動が必要らしい
    player.closeInventory();
    logger.debug("★onBlockPlace: End");
  }

  @EventHandler
  public void onItemMove(InventoryMoveItemEvent event) {
    logger.debug("★onItemMove: Start");
    logger.trace("event.isCancelled(): " + event.isCancelled());
    if (event.isCancelled()) {
      logger.debug("★this Event is Cancelled!");
      return;
    }

    logger.debug("ItemMoveEvent check");
    BlockState[] blockInventory = new BlockState[2];
    Boolean moveFlg = false;
    Sign sign = null;
    StorageSign storageSign = null;
    ItemStack item = event.getItem();
    logger.trace("config.getBoolean(\"auto-import\"): " + ConfigLoader.getAutoImport());
    if (ConfigLoader.getAutoImport()) {
      logger.debug("auto-import Start");
      logger.trace("event.getDestination().getLocation(): " + event.getDestination().getLocation());
      logger.trace("event.getDestination().getHolder(): " + event.getDestination().getHolder());
      if (event.getDestination().getLocation() == null){
        logger.debug("This Event is Temp Inventory.");
        //コンポスター用に生成された一時インベントリ
      } else if (event.getDestination().getHolder() instanceof Minecart){
        //何もしない
        logger.debug("This Event is Minecart.");
      } else if (event.getDestination().getHolder() instanceof DoubleChest) {
        logger.debug("This Event is DoubleChest.");
        DoubleChest lc = (DoubleChest) event.getDestination().getHolder();
        blockInventory[0] = (BlockState) lc.getLeftSide();
        blockInventory[1] = (BlockState) lc.getRightSide();
      } else if (!(event.getDestination().getHolder() instanceof BlockState)){
        //ブロック情報が取得できない場合も何もしない
        logger.debug("This Event not get BlockState.");
      } else {
        logger.debug("BlockState Set.");
        blockInventory[0] = (BlockState) event.getDestination().getHolder();
        logger.trace("blockInventory[0]: " + blockInventory[0]);
      }

      logger.debug("importLoop Start.");
      importLoop:
      for (int j = 0; j < 2; j++) {
        logger.trace("j: " + j);
        logger.trace("blockInventory[j]: " + blockInventory[j]);
        if (blockInventory[j] == null) {
          logger.debug("This Inventory is NULL.");
          break;
        }
        logger.debug("BlockFaceCheck Start.");
        for (int i = 0; i < 5; i++) {
          BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST,
              BlockFace.WEST};
          Block block = blockInventory[j].getBlock().getRelative(face[i]);
          boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
          boolean relIsStorageSign = StorageSign.isStorageSign(block, logger);
          boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
          logger.trace("blockInventory[j].getBlock(): "+ blockInventory[j].getBlock());
          logger.trace("i: " + i);
          logger.trace("face[i]: " + face[i]);
          logger.trace("block: " + block);
          logger.trace("relIsSignPost: " + relIsSignPost);
          logger.trace("relIsStorageSign: " + relIsStorageSign);
          logger.trace("relIsWallSign: " + relIsWallSign);
          if (relIsWallSign) {
            logger.trace("((WallSign) block.getBlockData()).getFacing(): "
                + ((WallSign) block.getBlockData()).getFacing());
          }
          if (i == 0 && relIsSignPost && relIsStorageSign) {
            if (item.getType() == Material.WHITE_BANNER) {
              //
              //襲撃バナー用
              //
            }
            sign = (Sign) block.getState();
            storageSign = new StorageSign(sign, block.getType(), logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              moveFlg = true;
              break importLoop;
            }
          } else if (i != 0 && relIsWallSign
              && ((WallSign) block.getBlockData()).getFacing() == face[i] && relIsStorageSign) {
            sign = (Sign) block.getState();
            storageSign = new StorageSign(sign, block.getType(), logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              moveFlg = true;
              break importLoop;
            }
          }
        }
      }
      //搬入先が見つかった(搬入するとは言ってない)
      if (moveFlg) {
        logger.debug("Import Sign.");
        importSign(sign, storageSign, item, event.getDestination());
      }
    }

    //搬出用にリセット
    logger.debug("config.getBoolean(\"auto-export\"): " + ConfigLoader.getAutoExport());
    if (ConfigLoader.getAutoExport()) {
      blockInventory[0] = null;
      blockInventory[1] = null;
      moveFlg = false;
      logger.trace("event.getSource(): " + event.getSource());
      logger.trace("event.getSource().getLocation(): " + event.getSource().getLocation());
      logger.trace("event.getSource().getHolder(): " + event.getSource().getHolder());
      if (event.getSource().getLocation() == null){
        //一時インベントリ
        logger.debug("This Event is Temp Inventory.");
      } else if (event.getSource().getHolder() instanceof Minecart){
        logger.debug("This Event is Minecart.");
      } else if (event.getSource().getHolder() instanceof DoubleChest lc) {
        logger.debug("This Event is DoubleChest.");
        blockInventory[0] = (BlockState) lc.getLeftSide();
        blockInventory[1] = (BlockState) lc.getRightSide();
      } else if (!(event.getSource().getHolder() instanceof BlockState)){
        //ブロック情報が取得できない時も何もしない
        logger.debug("This Event not get BlockState.");
      } else {
        logger.debug("BlockState Set.");
        blockInventory[0] = (BlockState) event.getSource().getHolder();
        logger.trace("blockInventory[0]: " + blockInventory[0]);
      }

      Block block = null;
      exportLoop:
      for (int j = 0; j < 2; j++) {
        logger.trace("j: " + j);
        logger.trace("blockInventory[j]: " + blockInventory[j]);
        if (blockInventory[j] == null) {
          break;
        }
        for (int i = 0; i < 5; i++) {
          BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST,
              BlockFace.WEST};
          block = blockInventory[j].getBlock().getRelative(face[i]);
          boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
          boolean relIsStorageSign = StorageSign.isStorageSign(block, logger);
          boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
          logger.trace("blockInventory[j].getBlock(): "+ blockInventory[j].getBlock());
          logger.trace("i: " + i);
          logger.trace("face[i]: " + face[i]);
          logger.trace("block: " + block);
          logger.trace("relIsSignPost: " + relIsSignPost);
          logger.trace("relIsStorageSign: " + relIsStorageSign);
          logger.trace("relIsWallSign: " + relIsWallSign);
          if (relIsWallSign) {
            logger.trace("((WallSign) block.getBlockData()).getFacing(): "
                + ((WallSign) block.getBlockData()).getFacing());
          }
          if (i == 0 && relIsSignPost && relIsStorageSign) {
            sign = (Sign) block.getState();
            storageSign = new StorageSign(sign, block.getType(), logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              moveFlg = true;
              break exportLoop;
            }
          } else if (i != 0 && relIsWallSign
              && ((WallSign) block.getBlockData()).getFacing() == face[i] && relIsStorageSign) {
            sign = (Sign) block.getState();
            storageSign = new StorageSign(sign, block.getType(), logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              moveFlg = true;
              break exportLoop;
            }
          }
        }
      }
      if (moveFlg) {
        logger.debug("Export Sign.");
        new ExportSign(item.clone(), event.getSource(), event.getDestination(), event, block, logger).runTask(this);
      }
    }
    logger.debug("★onItemMove:End.");
  }

  private void importSign(Sign sign, StorageSign storageSign, ItemStack item, Inventory inv) {
    logger.debug("importSign:Start");
    //搬入　条件　1スタック以上アイテムが入っている
    logger.trace("inv.containsAtLeast(item, item.getMaxStackSize()): " + inv.containsAtLeast(item, item.getMaxStackSize()));
    if (inv.containsAtLeast(item, item.getMaxStackSize())) {
      logger.debug("Item is more 1s.Import Item.");
      inv.removeItem(item);
      storageSign.addAmount(item.getAmount());
    }
    for (int i = 0; i < 4; i++) {
      logger.trace(" set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
      sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
    }
    logger.debug("update Sign.");
    sign.update();
  }

  @EventHandler
  public void onPlayerCraft(CraftItemEvent event) {
    logger.debug("★onPlayerCraft:Start");
    logger.trace("isStorageSign(event.getCurrentItem()): " + StorageSign.isStorageSign(event.getCurrentItem(), logger));
    logger.trace("!event.getWhoClicked().hasPermission(\"storagesign.craft\")" + !event.getWhoClicked().hasPermission("storagesign.craft"));
    if (StorageSign.isStorageSign(event.getCurrentItem(), logger) && !event.getWhoClicked()
        .hasPermission("storagesign.craft")) {
      logger.debug("This user hasn't Permission. storagesign.craft.");
      ((CommandSender) event.getWhoClicked()).sendMessage(
          ChatColor.RED + ConfigLoader.getNoPermission());
      event.setCancelled(true);
    }
    logger.debug("★onPlayerCraft:End");
  }

  @EventHandler
  public void onInventoryPickup(InventoryPickupItemEvent event) {//ホッパーに投げ込まれたとき
    logger.debug("★onInventoryPickup:Start");
    logger.trace("event.isCancelled(): " + event.isCancelled());
    logger.trace("!config.getBoolean(\"auto-import\"): " + !ConfigLoader.getAutoImport());
    if (event.isCancelled() || !ConfigLoader.getAutoImport()) {
      logger.debug("★eventCancelled or not set permission.auto-import.");
      return;
    }

    InventoryHolder holder = event.getInventory().getHolder();
    logger.trace("holder: " + holder);
    if (holder instanceof BlockState) {
      logger.debug("holder has BlockState.");

      Sign sign = null;
      StorageSign storageSign = null;
      boolean flag = false;
      for (int i = 0; i < 5; i++) {
        BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST,
            BlockFace.WEST};
        Block block = ((BlockState) holder).getBlock().getRelative(face[i]);
        boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
        boolean relIsStorageSign = StorageSign.isStorageSign(block, logger);
        boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
        logger.trace(" i: " + i);
        logger.trace(" relIsSignPost: " + relIsSignPost);
        logger.trace(" relIsStorageSign: " + relIsStorageSign);
        logger.trace(" relIsWallSign: " + relIsWallSign);
        logger.trace(" face[i]: " + face[i]);
        if(relIsWallSign) {
          logger.trace(" ((WallSign) block.getBlockData()).getFacing() == face[i]: " + (
              ((WallSign) block.getBlockData()).getFacing() == face[i]));
        }
        if (i == 0 && relIsSignPost && relIsStorageSign) {
          logger.debug(" This block is StorageSign.");
          sign = (Sign) block.getState();
          storageSign = new StorageSign(sign, block.getType(), logger);
          logger.trace(" storageSign.isSimilar(event.getItem().getItemStack()): " + storageSign.isSimilar(event.getItem().getItemStack()));
          if (storageSign.isSimilar(event.getItem().getItemStack())) {
            logger.debug(" this Item is Import Item.");
            flag = true;
            break;
          }
        } else if (i != 0 && relIsWallSign
            && ((WallSign) block.getBlockData()).getFacing() == face[i] && relIsStorageSign) {
          //BlockFaceに変更？(めんどい)
          logger.debug(" This block is WallStorageSign.");
          sign = (Sign) block.getState();
          storageSign = new StorageSign(sign, block.getType(), logger);
          logger.trace(" storageSign.isSimilar(event.getItem().getItemStack()): " + storageSign.isSimilar(event.getItem().getItemStack()));
          if (storageSign.isSimilar(event.getItem().getItemStack())) {
            logger.debug(" this Item is Import Item.");
            flag = true;
            break;
          }
        }
      }
      if (flag) {
        logger.debug("Import Item.");
        importSign(sign, storageSign, event.getItem().getItemStack(), event.getInventory());
      }
    }
    logger.debug("★onInventoryPickup:End");
  }

  @EventHandler
  public void onPlayerPickupItem(EntityPickupItemEvent event) {
    logger.debug("★onPlayerPickupItem:Start");
    logger.trace("event.isCancelled(): " + event.isCancelled());
    if (event.isCancelled()) {
      logger.debug("★Event Cancelled.");
      return;
    }

    logger.trace("event.getEntityType(): " + event.getEntityType());
    logger.trace("config.getBoolean(\"autocollect\"): " + ConfigLoader.getAutoCollect());
    if (event.getEntityType() == EntityType.PLAYER && ConfigLoader.getAutoCollect()) {
      Player player = (Player) event.getEntity();
      PlayerInventory playerInv = player.getInventory();
      ItemStack item = event.getItem().getItemStack();
      StorageSign storagesign = null;

      logger.trace("!player.hasPermission(\"storagesign.autocollect\"): " + !player.hasPermission("storagesign.autocollect"));
      //ここでは、エラーを出さずに無視する
      if (!player.hasPermission("storagesign.autocollect")) {
        logger.debug("★This user hasn't Permission. storagesign.autocollect.");
        return;
      }

      boolean mainHandhasSS = StorageSign.isStorageSign(playerInv.getItemInMainHand(), logger);
      boolean offHandhasSS = StorageSign.isStorageSign(playerInv.getItemInOffHand(), logger);
      logger.trace("mainHandhasSS: " + mainHandhasSS);
      logger.trace("offHandisSS: " + offHandhasSS);
      logger.debug("check mainHand has SS.");
      if (mainHandhasSS) {
        logger.debug("MainHand has StorageSign.");
        storagesign = new StorageSign(playerInv.getItemInMainHand(), logger);

        logger.trace("storagesign.getContents(): " + storagesign.getContents());
        if (storagesign.getContents() != null) {
          logger.debug("This SS is not Empty");

          logger.trace("storagesign.isSimilar(item): " + storagesign.isSimilar(item));
          logger.trace("playerInv.containsAtLeast(item, item.getMaxStackSize()): " + playerInv.containsAtLeast(item, item.getMaxStackSize()));
          logger.trace("storagesign.getStackSize(): " + storagesign.getStackSize());
          if (storagesign.isSimilar(item) && playerInv.containsAtLeast(item, item.getMaxStackSize())
              && storagesign.getStackSize() == 1) {
            logger.debug("★Pickup Item to StorageSign.");
            storagesign.addAmount(item.getAmount());

            //1.9,10ではバグる？
            playerInv.removeItem(item);
            playerInv.setItemInMainHand(storagesign.getStorageSign());
            player.updateInventory();
            //event.getItem().remove();
            //player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.5f);
            //event.setCancelled(true);
            return;
          }
        }
      }
      logger.debug("check offHand has SS.");
      if (offHandhasSS) {//メインハンドで回収されなかった時
        logger.debug("OffHand has StorageSign.");
        storagesign = new StorageSign(playerInv.getItemInOffHand(), logger);
        if (storagesign.getContents() != null) {
          logger.debug("This SS is not Empty");

          logger.trace("storagesign.isSimilar(item): " + storagesign.isSimilar(item));
          logger.trace("playerInv.containsAtLeast(item, item.getMaxStackSize()): " + playerInv.containsAtLeast(item, item.getMaxStackSize()));
          logger.trace("storagesign.getStackSize(): " + storagesign.getStackSize());
          if (storagesign.isSimilar(item) && playerInv.containsAtLeast(item, item.getMaxStackSize())
              && storagesign.getStackSize() == 1) {
            logger.debug("★Pickup Item to StorageSign.");
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
    }
    //SSをプレイヤー以外拾えなくする
    if (event.getEntityType() != EntityType.PLAYER) {
      logger.debug("This Entity isn't Player.");
      Item item = event.getItem();

      boolean isStorageSign = StorageSign.isStorageSign(item.getItemStack(), logger);
      logger.trace("isStorageSign(item.getItemStack()): " + isStorageSign);
      if (isStorageSign) {
        logger.debug("Delay Picked Entity StorageSign.");
        item.setPickupDelay(20);//毎tickキャンセルしてたら重そう
        event.setCancelled(true);
      }
    }
    logger.debug("★onPlayerPickupItem:End.");
  }

  @EventHandler
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if(_fallingBlockSS){
      logger.debug("★onEntityChangeBlock:Start");
      logger.trace("event.getEntity(): " + event.getEntity());
      if (event.getEntity() instanceof FallingBlock fallingBlock) {
        logger.debug("check Falling relativeBlock.");
        dropRelativeSign(event.getBlock());
      }
      logger.debug("★onEntityChangeBlock:End");
    }
  }

  private void dropRelativeSign(Block block) {
    logger.debug(" dropRelativeSign:Start");
    Map<Location, StorageSign> breakSignMap = new HashMap<>();
    boolean isStorageSign = StorageSign.isStorageSign(block, logger);
    logger.trace(" isStorageSign: " + isStorageSign);
    if (isStorageSign) {
      logger.debug(" breakItem is StorageSign.");
      breakSignMap.put(block.getLocation(),
          new StorageSign((Sign) block.getState(), block.getType(), logger));
    }

    for (int i = 0; i < 5; i++) {//東西南北で判定
      logger.trace("  i: " + i);
      BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST,
          BlockFace.WEST};
      Block relBlock = block.getRelative(face[i]);

      boolean relIsSignPost = SignDefinition.sign_materials.contains(relBlock.getType());
      boolean relIsStorageSign = StorageSign.isStorageSign(relBlock, logger);
      boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(relBlock.getType());
      logger.trace("  relBlock: " + relBlock);
      logger.trace("  relIsSignPost: " + relIsSignPost);
      logger.trace("  relIsStorageSign: " + relIsStorageSign);
      logger.trace("  relIsWallSign: " + relIsWallSign);
      if (i == 0 && relIsSignPost && relIsStorageSign) {
        logger.debug("  This Block is StorageSign.");
        breakSignMap.put(relBlock.getLocation(),
            new StorageSign((Sign) relBlock.getState(), relBlock.getType(), logger));
      } else if (relIsWallSign && ((WallSign) relBlock.getBlockData()).getFacing() == face[i]
          && relIsStorageSign) {
        logger.debug("  This Block is WallStorageSign.");
        breakSignMap.put(relBlock.getLocation(),
            new StorageSign((Sign) relBlock.getState(), relBlock.getType(), logger));
      }
    }

    logger.trace(" breakSignMap.isEmpty(): " + breakSignMap.isEmpty());
    if (breakSignMap.isEmpty()) {
      logger.debug(" This Block isn't block StorageSign.");
      return;
    }

    logger.debug(" Break StorageSign Set Item in World.");
    for (Location loc : breakSignMap.keySet()) {
      StorageSign sign = breakSignMap.get(loc);
      logger.trace("  loc: " + loc);
      logger.trace("  sign:" + sign);

      Location loc2 = loc;
      loc2.add(0.5, 0.5, 0.5);//中心にドロップさせる
      loc.getWorld().dropItem(loc2, sign.getStorageSign());
      loc.getBlock().setType(Material.AIR);
      logger.debug("  Storage Sign drop in World.");
    }
  }

  private boolean isDye(ItemStack item) {
    logger.debug(" isDye: Start");
    Material mat = item.getType();

    logger.trace(" mat: " + mat);
    switch (mat) {
      case WHITE_DYE:
      case ORANGE_DYE:
      case MAGENTA_DYE:
      case LIGHT_BLUE_DYE:
      case YELLOW_DYE:
      case LIME_DYE:
      case PINK_DYE:
      case GRAY_DYE:
      case LIGHT_GRAY_DYE:
      case CYAN_DYE:
      case PURPLE_DYE:
      case BLUE_DYE:
      case BROWN_DYE:
      case GREEN_DYE:
      case RED_DYE:
      case BLACK_DYE:
        return true;
      default:
    }
    return false;
  }

  private DyeColor getDyeColor(ItemStack item) {
    logger.debug("getDyeColor: Start");
    Material mat = item.getType();

    logger.trace("mat: " + mat);
    switch (mat) {
      case WHITE_DYE:
        return DyeColor.WHITE;
      case ORANGE_DYE:
        return DyeColor.ORANGE;
      case MAGENTA_DYE:
        return DyeColor.MAGENTA;
      case LIGHT_BLUE_DYE:
        return DyeColor.LIGHT_BLUE;
      case YELLOW_DYE:
        return DyeColor.YELLOW;
      case LIME_DYE:
        return DyeColor.LIME;
      case PINK_DYE:
        return DyeColor.PINK;
      case GRAY_DYE:
        return DyeColor.GRAY;
      case LIGHT_GRAY_DYE:
        return DyeColor.LIGHT_GRAY;
      case CYAN_DYE:
        return DyeColor.CYAN;
      case PURPLE_DYE:
        return DyeColor.PURPLE;
      case BLUE_DYE:
        return DyeColor.BLUE;
      case BROWN_DYE:
        return DyeColor.BROWN;
      case GREEN_DYE:
        return DyeColor.GREEN;
      case RED_DYE:
        return DyeColor.RED;
      case BLACK_DYE:
        return DyeColor.BLACK;
      default:
    }
    return null;
  }

  private boolean isSac(ItemStack item) {
    logger.debug(" isSac: Start");
    Material mat = item.getType();

    logger.trace(" mat: " + mat);
    switch (mat) {
      case INK_SAC:
      case GLOW_INK_SAC:
        return true;
      default:
    }
    return false;
  }

  private boolean isGlowSac(ItemStack item) {
    logger.debug(" isSac: Start");
    Material mat = item.getType();

    logger.trace(" mat: " + mat);
    switch (mat) {
      case INK_SAC:
        return false;
      case GLOW_INK_SAC:
        return true;
      default:
    }
    return false;
  }
}