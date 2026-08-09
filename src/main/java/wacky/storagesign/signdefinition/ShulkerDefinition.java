package wacky.storagesign.signdefinition;

import static org.bukkit.Material.ACACIA_SIGN;
import static org.bukkit.Material.BAMBOO_SIGN;
import static org.bukkit.Material.BIRCH_SIGN;
import static org.bukkit.Material.BLACK_SHULKER_BOX;
import static org.bukkit.Material.BLUE_SHULKER_BOX;
import static org.bukkit.Material.BROWN_SHULKER_BOX;
import static org.bukkit.Material.CHERRY_SIGN;
import static org.bukkit.Material.CRIMSON_SIGN;
import static org.bukkit.Material.CYAN_SHULKER_BOX;
import static org.bukkit.Material.DARK_OAK_SIGN;
import static org.bukkit.Material.GRAY_SHULKER_BOX;
import static org.bukkit.Material.GREEN_SHULKER_BOX;
import static org.bukkit.Material.JUNGLE_SIGN;
import static org.bukkit.Material.LIGHT_BLUE_SHULKER_BOX;
import static org.bukkit.Material.LIGHT_GRAY_SHULKER_BOX;
import static org.bukkit.Material.LIME_SHULKER_BOX;
import static org.bukkit.Material.MAGENTA_SHULKER_BOX;
import static org.bukkit.Material.MANGROVE_SIGN;
import static org.bukkit.Material.OAK_SIGN;
import static org.bukkit.Material.ORANGE_SHULKER_BOX;
import static org.bukkit.Material.PALE_OAK_SIGN;
import static org.bukkit.Material.PINK_SHULKER_BOX;
import static org.bukkit.Material.PURPLE_SHULKER_BOX;
import static org.bukkit.Material.RED_SHULKER_BOX;
import static org.bukkit.Material.SHULKER_BOX;
import static org.bukkit.Material.SPRUCE_SIGN;
import static org.bukkit.Material.WARPED_SIGN;
import static org.bukkit.Material.WHITE_SHULKER_BOX;
import static org.bukkit.Material.YELLOW_SHULKER_BOX;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;

public class ShulkerDefinition {

  /**
   * 看板と扱われるアイテム一覧.
   * 看板種類が追加されたら追加する.
   */
  public static final Set<Material> shulker_materials = Collections.unmodifiableSet(
      EnumSet.of(SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX, LIME_SHULKER_BOX, PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX, CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX, RED_SHULKER_BOX, BLACK_SHULKER_BOX));

}