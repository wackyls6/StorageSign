package wacky.storagesign;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * config.ymlを読み取るローダー.
 */
public class ConfigLoader {

  /**
   * config.ymlを参照するFileConfigurationの実体.
   */
  public static FileConfiguration fileConfig;

  /**
   * ローダーの初期化処理. JavaPlugin.onEnableクラスでのみ呼ばれることを想定.
   */
  public static void setup(JavaPlugin plugin) {
    fileConfig = plugin.getConfig();
    fileConfig.options().copyDefaults(true);
    fileConfig.options().setHeader(List.of("StorageSign Configuration"));
    plugin.saveConfig();
  }

  /**
   * no-permissionの値を取得する.
   *
   * @return String
   */
  public static String getNoPermission() {
    return fileConfig.getString("no-permisson");
  }

  /**
   * log-levelの値を取得する.
   *
   * @return String
   */
  public static String getLogLevel() {
    return fileConfig.getString("log-level");
  }

  /**
   * manual-importの値を取得する.
   *
   * @return boolean
   */
  public static boolean getManualImport() {
    return fileConfig.getBoolean("manual-import");
  }

  /**
   * manual-exportの値を取得する.
   *
   * @return boolean
   */
  public static boolean getManualExport() {
    return fileConfig.getBoolean("manual-export");
  }

  /**
   * auto-importの値を取得する.
   *
   * @return boolean
   */
  public static boolean getAutoImport() {
    return fileConfig.getBoolean("auto-import");
  }

  /**
   * auto-exportの値を取得する.
   *
   * @return boolean
   */
  public static boolean getAutoExport() {
    return fileConfig.getBoolean("auto-export");
  }

  /**
   * autocollectの値を取得する.
   *
   * @return boolean
   */
  public static boolean getAutoCollect() {
    return fileConfig.getBoolean("autocollect");
  }

  /**
   * hardrecipeの値を取得する.
   *
   * @return boolean
   */
  public static boolean getHardRecipe() {
    return fileConfig.getBoolean("hardrecipe");
  }

  /**
   * divide-limitの値を取得する.
   *
   * @return boolean
   */
  public static int getDivideLimit() {
    return fileConfig.getInt("divide-limit");
  }

  /**
   * sneak-divide-limitの値を取得する.
   *
   * @return boolean
   */
  public static int getSneakDivideLimit() {
    return fileConfig.getInt("sneak-divide-limit");
  }

  /**
   * max-stack-sizeの値を取得する.
   *
   * @return boolean
   */
  public static int getMaxStackSize() {
    return fileConfig.getInt("max-stack-size");
  }

  /**
   * no-budの値を取得する.
   *
   * @return boolean
   */
  public static boolean getNoBud() {
    return fileConfig.getBoolean("no-bud");
  }

  /**
   * falling-block-itemSSの値を取得する.
   *
   * @return boolean
   */
  public static boolean getFallingBlockItemSs() {
    return fileConfig.getBoolean("falling-block-itemSS");
  }

  /**
   * banner-debugの値を取得する.
   *
   * @return boolean
   */
  public static boolean getBannerDebug() {
    return fileConfig.getBoolean("banner-debug");
  }
}
