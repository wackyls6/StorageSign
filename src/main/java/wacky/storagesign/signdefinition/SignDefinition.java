package wacky.storagesign.signdefinition;

import static java.util.Map.entry;
import static org.bukkit.Material.ACACIA_SIGN;
import static org.bukkit.Material.ACACIA_WALL_SIGN;
import static org.bukkit.Material.BAMBOO_SIGN;
import static org.bukkit.Material.BAMBOO_WALL_SIGN;
import static org.bukkit.Material.BIRCH_SIGN;
import static org.bukkit.Material.BIRCH_WALL_SIGN;
import static org.bukkit.Material.CHERRY_SIGN;
import static org.bukkit.Material.CHERRY_WALL_SIGN;
import static org.bukkit.Material.CRIMSON_SIGN;
import static org.bukkit.Material.CRIMSON_WALL_SIGN;
import static org.bukkit.Material.DARK_OAK_SIGN;
import static org.bukkit.Material.DARK_OAK_WALL_SIGN;
import static org.bukkit.Material.JUNGLE_SIGN;
import static org.bukkit.Material.JUNGLE_WALL_SIGN;
import static org.bukkit.Material.MANGROVE_SIGN;
import static org.bukkit.Material.MANGROVE_WALL_SIGN;
import static org.bukkit.Material.OAK_SIGN;
import static org.bukkit.Material.OAK_WALL_SIGN;
import static org.bukkit.Material.PALE_OAK_SIGN;
import static org.bukkit.Material.PALE_OAK_WALL_SIGN;
import static org.bukkit.Material.SPRUCE_SIGN;
import static org.bukkit.Material.SPRUCE_WALL_SIGN;
import static org.bukkit.Material.WARPED_SIGN;
import static org.bukkit.Material.WARPED_WALL_SIGN;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;

public class SignDefinition {

  /**
   * 看板と扱われるアイテム一覧.
   * 看板種類が追加されたら追加する.
   */
  public static final Set<Material> sign_materials = Collections.unmodifiableSet(
      EnumSet.of(OAK_SIGN, SPRUCE_SIGN, BIRCH_SIGN, JUNGLE_SIGN, ACACIA_SIGN, DARK_OAK_SIGN, CRIMSON_SIGN, WARPED_SIGN, MANGROVE_SIGN, CHERRY_SIGN, BAMBOO_SIGN, PALE_OAK_SIGN));
  /**
   * 看板と扱われるアイテム一覧.
   * 看板種類が追加されたら追加する.
   */
  public static final Set<Material> wall_sign_materials = Collections.unmodifiableSet(EnumSet.of(OAK_WALL_SIGN, SPRUCE_WALL_SIGN, BIRCH_WALL_SIGN, JUNGLE_WALL_SIGN, ACACIA_WALL_SIGN, DARK_OAK_WALL_SIGN, CRIMSON_WALL_SIGN, WARPED_WALL_SIGN, MANGROVE_WALL_SIGN, CHERRY_WALL_SIGN, BAMBOO_WALL_SIGN, PALE_OAK_WALL_SIGN));
  /**
   * 壁掛け看板と通常看板の変換一覧.
   * 看板種類が追加されたら追加する.
   */
  public static final Map<Material, Material> wall_normal_sign_maps = Map.ofEntries(
      entry(OAK_WALL_SIGN, OAK_SIGN),
      entry(BIRCH_WALL_SIGN, BIRCH_SIGN),
      entry(SPRUCE_WALL_SIGN, SPRUCE_SIGN),
      entry(JUNGLE_WALL_SIGN, JUNGLE_SIGN),
      entry(ACACIA_WALL_SIGN, ACACIA_SIGN),
      entry(DARK_OAK_WALL_SIGN, DARK_OAK_SIGN),
      entry(CRIMSON_WALL_SIGN, CRIMSON_SIGN),
      entry(WARPED_WALL_SIGN, WARPED_SIGN),
      entry(MANGROVE_WALL_SIGN, MANGROVE_SIGN),
      entry(CHERRY_WALL_SIGN, CHERRY_SIGN),
      entry(BAMBOO_WALL_SIGN, BAMBOO_SIGN),
      entry(PALE_OAK_WALL_SIGN , PALE_OAK_SIGN)

  );
}
