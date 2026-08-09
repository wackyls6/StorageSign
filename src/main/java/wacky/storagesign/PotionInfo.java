package wacky.storagesign;


import static java.util.Map.entry;

import com.github.teruteru128.logger.Logger;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;
import org.bukkit.util.NumberConversions;
import wacky.storagesign.Exception.PotionException;

import static org.bukkit.potion.PotionType.*;

public class PotionInfo {

  private static final Set<PotionType> HEAL_POTIONS =
      Collections.unmodifiableSet(EnumSet.of(HEALING, STRONG_HEALING));
  private static final Set<PotionType> BREATH_POTIONS = Collections
      .unmodifiableSet(EnumSet.of(WATER_BREATHING, LONG_WATER_BREATHING));
  private static final Set<PotionType> DAMAGE_POTIONS =
      Collections.unmodifiableSet(EnumSet.of(HARMING, STRONG_HARMING));
  private static final Set<PotionType> JUMP_POTIONS = Collections.unmodifiableSet(
      EnumSet.of(LEAPING, LONG_LEAPING, STRONG_LEAPING));
  private static final Set<PotionType> SPEED_POTIONS = Collections.unmodifiableSet(
      EnumSet.of(SWIFTNESS, LONG_SWIFTNESS, STRONG_SWIFTNESS));
  private static final Set<PotionType> REGENERATION_POTIONS = Collections.unmodifiableSet(
      EnumSet.of(REGENERATION, LONG_REGENERATION, STRONG_REGENERATION));
  protected static String TYPE_SPLASH_PREF = "S";
  protected static String TYPE_LINGERING_PREF = "L";
  private static String ENHANCE_NORMAL_CODE = "0";
  private static String ENHANCE_EXTENSION_CODE = "1";
  private static String ENHANCE_STRONG_CODE = "2";
  private static String ENHANCE_EXTENSION_PREF = "LONG_";
  private static String ENHANCE_STRONG_PREF = "STRONG_";
  private static String SHORT_NAME_HEAL = "HEAL";
  private static String SHORT_NAME_BREAT = "BREAT";
  private static String SHORT_NAME_DAMAG = "DAMAG";
  private static String SHORT_NAME_JUMP = "JUMP";
  private static String SHORT_NAME_SPEED = "SPEED";
  private static String SHORT_NAME_REGEN = "REGEN";
  private static String OLD_HEAL_TYPE_NAME = "INSTANT_HEAL";
  private static String OLD_DAMAGE_TYPE_NAME = "INSTANT_DAMAGE";
  private static String OLD_JUMP_TYPE_NAME = "JUMP";
  private static String OLD_SPEED_TYPE_NAME = "SPEED";
  private static String OLD_REGEN_TYPE_NAME = "REGEN";
  private static final Map<String, String> NBT_NAME_SHORT_NAMES = Map.of(
          OLD_HEAL_TYPE_NAME, SHORT_NAME_HEAL,
          OLD_DAMAGE_TYPE_NAME, SHORT_NAME_DAMAG,
          OLD_JUMP_TYPE_NAME, SHORT_NAME_JUMP,
          OLD_SPEED_TYPE_NAME, SHORT_NAME_SPEED,
          OLD_REGEN_TYPE_NAME, SHORT_NAME_REGEN,
          WATER_BREATHING.toString(), SHORT_NAME_BREAT
  );

  protected Material mat;
  protected PotionType pot;
  protected short damage = 0;
  protected Logger logger;

  public PotionInfo(Material material, String type, String effName, String enhance, Logger logger) {

    this.logger = logger;
    logger.debug("PotionInfo:Start");
    logger.trace("material=" + material + ", type=" + type + ", effName=" + effName + ", enhance="
        + enhance + ", logger=" + logger);
    this.mat = material;

    // ポーション種別の設定
    logger.trace("type: " + type);
    if (type.startsWith(TYPE_SPLASH_PREF)) {
      logger.debug("This Potion Type is SplashPortion.");
      this.mat = Material.SPLASH_POTION;
    } else if (type.startsWith(TYPE_LINGERING_PREF)) {
      // 元のやつだと入ってないからいらない？
      logger.debug("This Potion Type is LingeringPortion.");
      this.mat = Material.LINGERING_POTION;
    }

    this.pot = getType(effName, enhance);
    this.damage = NumberConversions.toShort(enhance);

    // 一応前のロジックも入れておく
    logger.trace("damage: " + damage);
    logger.trace("pot.isExtendable(): " + pot.isExtendable());
    logger.trace("pot.isUpgradeable(): " + pot.isUpgradeable());
    if (damage % 8192 > 64 && pot.isExtendable()) {
      this.damage = 1;// 延長
    } else if (damage % 64 > 32 && pot.isUpgradeable()) {
      this.damage = 2;// 強化
    }

  }

  public static String getShortName(PotionType pot) {
    String name = pot.toString();

    // 特殊命名ルールの取り出し
    if (HEAL_POTIONS.contains(pot)) {
      return SHORT_NAME_HEAL;
    } else if (BREATH_POTIONS.contains(pot)) {
      return SHORT_NAME_BREAT;
    } else if (DAMAGE_POTIONS.contains(pot)) {
      return SHORT_NAME_DAMAG;
    } else if (JUMP_POTIONS.contains(pot)) {
      return SHORT_NAME_JUMP;
    } else if (SPEED_POTIONS.contains(pot)) {
      return SHORT_NAME_SPEED;
    } else if (REGENERATION_POTIONS.contains(pot)) {
      return SHORT_NAME_REGEN;
    } else {
      // それ以外のもの
      // 延長・強化のプレフィックス除去
      if (name.startsWith(ENHANCE_EXTENSION_PREF)) {
        name = name.substring(ENHANCE_EXTENSION_PREF.length());
      } else if (name.startsWith(ENHANCE_STRONG_PREF)) {
        name = name.substring(ENHANCE_STRONG_PREF.length());
      }

      // 5文字以内の名前なら、そのまま使う。それ以外なら５文字に切り出す
      if (name.length() <= 5) {
        return name;
      }
    }

    return name.substring(0, 5);
  }

  public static String getSignData(Material mat, PotionType pot, short damage) {
    String prefix = "";
    if (mat == Material.SPLASH_POTION) {
      prefix = PotionInfo.TYPE_SPLASH_PREF;
    } else if (mat == Material.LINGERING_POTION) {
      prefix = PotionInfo.TYPE_LINGERING_PREF;
    }

    return prefix + "POTION:" + PotionInfo.getShortName(pot) + ":" + damage;
  }

  public static String getTagData(Material mat, PotionType pot, short damage, int amount) {
    return mat.toString() + ":" + getNormalType(pot) + ":" + damage + " " + amount;
  }

  public static String getPotionTypeCode(PotionType pot) {
    String name = pot.toString();
    if (name.startsWith(ENHANCE_EXTENSION_PREF)) {
      return ENHANCE_EXTENSION_CODE;
    } else if (name.startsWith(ENHANCE_STRONG_PREF)) {
      return ENHANCE_STRONG_CODE;
    }
    return ENHANCE_NORMAL_CODE;
  }

  private PotionType getType(String effName, String enhance) {
    logger.debug("getType: Start");

    // 表記上例外のケースたちの判定
    logger.trace("effName: " + effName + ".enhance: " + enhance);
    logger.trace("effName.equals(SHORT_NAME_HEAL): " + effName.equals(SHORT_NAME_HEAL));
    logger.trace("effName.equals(SHORT_NAME_BREAT): " + effName.equals(SHORT_NAME_BREAT));
    logger.trace("effName.equals(SHORT_NAME_DAMAG): " + effName.equals(SHORT_NAME_DAMAG));
    logger.trace("effName.equals(SHORT_NAME_JUMP): " + effName.equals(SHORT_NAME_JUMP));
    logger.trace("effName.equals(SHORT_NAME_SPEED): " + effName.equals(SHORT_NAME_SPEED));
    logger.trace("enhance.equals(ENHANCE_NORMAL_CODE)" + enhance.equals(ENHANCE_NORMAL_CODE));
    logger.trace("enhance.equals(ENHANCE_STRONG_CODE)" + enhance.equals(ENHANCE_STRONG_CODE));
    if (effName.equals(SHORT_NAME_HEAL)) {
      if (enhance.equals(ENHANCE_NORMAL_CODE)) {
        logger.debug("Portion:Heal.");
        return HEALING;
      } else if (enhance.equals(ENHANCE_STRONG_CODE)) {
        logger.debug("Portion:Str_Heal.");
        return STRONG_HEALING;
      } else {
        logger.error("Heal Enhance is not Exist!Enhance: " + enhance);
        throw new PotionException("治癒のポーションに対応する種類が存在しません");
      }
    } else if (effName.equals(SHORT_NAME_BREAT)) {
      if (enhance.equals(ENHANCE_NORMAL_CODE)) {
        logger.debug("Portion:Breat.");
        return WATER_BREATHING;
      } else if (enhance.equals(ENHANCE_EXTENSION_CODE)) {
        logger.debug("Portion:Ext_Breat.");
        return LONG_WATER_BREATHING;
      } else {
        logger.error("Breat Enhance is not Exist!Enhance: " + enhance);
        throw new PotionException("水中呼吸のポーションに対応する種類が存在しません");
      }
    } else if (effName.equals(SHORT_NAME_DAMAG)) {
      if (enhance.equals(ENHANCE_NORMAL_CODE)) {
        logger.debug("Portion:Damage.");
        return HARMING;
      } else if (enhance.equals(ENHANCE_STRONG_CODE)) {
        logger.debug("Portion:Str_Damage.");
        return STRONG_HARMING;
      } else {
        logger.error("Damage Enhance is not Exist!Enhance: " + enhance);
        throw new PotionException("ダメージのポーションに対応する種類が存在しません");
      }
    } else if (effName.equals(SHORT_NAME_JUMP)) {
      if (enhance.equals(ENHANCE_NORMAL_CODE)) {
        logger.debug("Portion:Jump.");
        return LEAPING;
      } else if (enhance.equals(ENHANCE_EXTENSION_CODE)) {
        logger.debug("Portion:Ext_Jump.");
        return LONG_LEAPING;
      } else if (enhance.equals(ENHANCE_STRONG_CODE)) {
        logger.debug("Portion:Str_Jump.");
        return STRONG_LEAPING;
      } else {
        logger.error("Jump Enhance is not Exist!Enhance: " + enhance);
        throw new PotionException("跳躍のポーションに対応する種類が存在しません");
      }
    } else if (effName.equals(SHORT_NAME_SPEED)) {
      if (enhance.equals(ENHANCE_NORMAL_CODE)) {
        logger.debug("Portion:Speed.");
        return SWIFTNESS;
      } else if (enhance.equals(ENHANCE_EXTENSION_CODE)) {
        logger.debug("Portion:Ext_Speed.");
        return LONG_SWIFTNESS;
      } else if (enhance.equals(ENHANCE_STRONG_CODE)) {
        logger.debug("Portion:Str_Speed.");
        return STRONG_SWIFTNESS;
      } else {
        logger.error("Speed Enhance is not Exist!Enhance: " + enhance);
        throw new PotionException("俊敏のポーションに対応する種類が存在しません");
      }
    } else if (effName.equals(SHORT_NAME_REGEN)) {
      if (enhance.equals(ENHANCE_NORMAL_CODE)) {
        logger.debug("Portion:Regen.");
        return REGENERATION;
      } else if (enhance.equals(ENHANCE_EXTENSION_CODE)) {
        logger.debug("Portion:Ext_Regen.");
        return LONG_REGENERATION;
      } else if (enhance.equals(ENHANCE_STRONG_CODE)) {
        logger.debug("Portion:Str_Regen.");
        return STRONG_REGENERATION;
      } else {
        logger.error("Speed Enhance is not Exist!Enhance: " + enhance);
        throw new PotionException("再生のポーションに対応する種類が存在しません");
      }
    } else {
      // それ以外は命名ルールに従って検索をかける
      // 延長、強化があったら先頭にプレフィックスをつけて検索
      logger.debug("SearchPotion");
      String name = effName;
      if (enhance.equals(ENHANCE_EXTENSION_CODE)) {
        name = ENHANCE_EXTENSION_PREF + name;
      } else if (enhance.equals(ENHANCE_STRONG_CODE)) {
        name = ENHANCE_STRONG_PREF + name;
      }
      logger.trace("name: " + name);
      for (PotionType p : values()) {
        logger.trace(" p: " + p);
        logger.trace(" p.toString().startsWith(name): " + p.toString().startsWith(name));
        if (p.toString().startsWith(name)) {
          logger.debug(" Potion Is " + p);
          return p;
        }
      }
      logger.error("Enhance is not Exist!");
      throw new PotionException("ポーションデータが存在しません");
    }
  }

  private static String getNormalType(PotionType pot) {
    if (HEAL_POTIONS.contains(pot)) {
      return OLD_HEAL_TYPE_NAME;
    } else if (BREATH_POTIONS.contains(pot)) {
      return WATER_BREATHING.toString();
    } else if (DAMAGE_POTIONS.contains(pot)) {
      return OLD_DAMAGE_TYPE_NAME;
    } else if (JUMP_POTIONS.contains(pot)) {
      return OLD_JUMP_TYPE_NAME;
    } else if (SPEED_POTIONS.contains(pot)) {
      return OLD_SPEED_TYPE_NAME;
    } else if (REGENERATION_POTIONS.contains(pot)) {
      return OLD_REGEN_TYPE_NAME;
    } else {
      // それ以外のもの
      // 延長・強化のプレフィックス除去
      String name = pot.toString();
      if (name.startsWith(ENHANCE_EXTENSION_PREF)) {
        name = name.substring(ENHANCE_EXTENSION_PREF.length());
      } else if (name.startsWith(ENHANCE_STRONG_PREF)) {
        name = name.substring(ENHANCE_STRONG_PREF.length());
      }
      for (PotionType p : values()) {
        String str = p.toString();
        if (str.startsWith(name)) {
          return str;
        }
      }
      throw new PotionException("ポーションデータが存在しません");
    }
  }

  public static String convertNBTNameToShortName(String nbtName) {
    if (NBT_NAME_SHORT_NAMES.containsKey(nbtName)) {
      return NBT_NAME_SHORT_NAMES.get(nbtName);
    }
    return nbtName;
  }

  public short getDamage() {
    return damage;
  }

  public Material getMaterial() {
    return mat;
  }

  public PotionType getPotionType() {
    return pot;
  }

}
