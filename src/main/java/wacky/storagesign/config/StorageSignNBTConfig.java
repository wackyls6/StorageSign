package wacky.storagesign.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * key.config用セットアップクラス.
 */
public class StorageSignNBTConfig {

  private static final String PLUGIN_PATH = "plugins/StorageSign";
  private FileConfiguration config = null;
  private File configFile;
  private String file;
  private Plugin plugin;


  /**
   * Pluginだけのコンストラクタ.
   */
  public StorageSignNBTConfig(Plugin plugin) {
    this(plugin, "storageSignNBT.yml");
  }

  /**
   * コンストラクタ.
   */
  private StorageSignNBTConfig(Plugin plugin, String fileName) {
    this.plugin = plugin;
    this.file = fileName;

    // プラグインのパスを受け取って、コンフィグ作成
    FileConfiguration config = plugin.getConfig();
    String path = PLUGIN_PATH;

    configFile = new File(path, file);
  }

  /**
   * デフォルトのConfigファイルの保存.
   */
  public void saveDefaultConfig() {
    if (!configFile.exists()) {
      saveConfig();
    }
  }

  /**
   * Configファイルの再読み込み.
   */
  public void reloadConfig() {
    config = YamlConfiguration.loadConfiguration(configFile);

    final InputStream defConfigStream = plugin.getResource(file);
    if (defConfigStream == null) {
      return;
    }

    config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
  }

  /**
   * Configファイル取得.
   */
  public FileConfiguration getConfig() {
    if (config == null) {
      reloadConfig();
    }
    return config;
  }
  public void saveConfig() {
    if (config == null) {
      return;
    }
    try {
      getConfig().save(configFile);
    } catch (IOException ex) {
      plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
    }
  }

  public void init(){
    config = this.getConfig();
    config.options().copyDefaults(true);
    // 特性上、何度も上書き処理を走らせたくないため、上書き制御付きのdefaultConfigを呼ぶ
    // このファイル自体は子のプラグインでは利用せず、StorageSignでのみ使用
    this.saveDefaultConfig();
  }

  public String getOminousBannerNBT(String version){
    List<Map<?, ?>> l = (List<Map<?, ?>>) config.getMapList("ominusBunner");
    List<Map<String, String>> list = (List<Map<String, String>>) (List<?>) l;

    return extractNbtForVersion(l, version).orElse("");
  }

  private static Optional<String> extractNbtForVersion(List<Map<?, ?>> rawList, String version) {
    for (Map<?, ?> rawMap : rawList) {
      // 安全なキー・値のチェック
      if (rawMap.size() != 1) continue;

      Map.Entry<?, ?> entry = rawMap.entrySet().iterator().next();

      if (!(entry.getKey() instanceof String key) || !(entry.getValue() instanceof String value)) continue;

      if (version.equals(key)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

}