package wacky.storagesign.signdefinition;

import static java.util.Map.entry;
import static org.bukkit.Material.ACACIA_SIGN;
import static org.bukkit.Material.BAMBOO_SIGN;
import static org.bukkit.Material.BIRCH_SIGN;
import static org.bukkit.Material.CHERRY_SIGN;
import static org.bukkit.Material.CRIMSON_SIGN;
import static org.bukkit.Material.DARK_OAK_SIGN;
import static org.bukkit.Material.JUNGLE_SIGN;
import static org.bukkit.Material.MANGROVE_SIGN;
import static org.bukkit.Material.OAK_SIGN;
import static org.bukkit.Material.PALE_OAK_SIGN;
import static org.bukkit.Material.SPRUCE_SIGN;
import static org.bukkit.Material.WARPED_SIGN;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum SignMatStringDefinition {
  OAK_SS("OakStorageSign", OAK_SIGN),
  SPRUCE_SS("SpruceStorageSign", SPRUCE_SIGN),
  BIRCH_SS("BirchStorageSign", BIRCH_SIGN),
  JUNGLE_SS("JungleStorageSign", JUNGLE_SIGN),
  ACACIA_SS("AcaciaStorageSign", ACACIA_SIGN),
  DARKOAK_SS("DarkOakStorageSign", DARK_OAK_SIGN),
  CRIMSON_SS("CrimsonStorageSign", CRIMSON_SIGN),
  WARPED_SS("WarpedStorageSign", WARPED_SIGN),
  MANGROVE_SS("MangroveStorageSign", MANGROVE_SIGN),
  CHERRY_SS("CherryStorageSign", CHERRY_SIGN),
  BAMBOO_SS("BambooStorageSign", BAMBOO_SIGN),
  PALEOAK_SS("PaleOakStorageSign", PALE_OAK_SIGN);

  private String materialString;
  private Material material;

  public static Map<String, Material> asStringMaterialMap() {
    Map<String, Material> map = new LinkedHashMap<>();
    Arrays.stream(values()).forEach(e -> map.put(e.materialString, e.material));
    return map;
  }

  public static Map<Material, String> asMaterialStringMap() {
    Map<Material, String> map = new LinkedHashMap<>();
    Arrays.stream(values()).forEach(e -> map.put(e.material, e.materialString));
    return map;
  }
}
