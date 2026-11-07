package cc.sighs.extremeevasion;

import cc.sighs.extremeevasion.handler.EchoHandler;
import cc.sighs.extremeevasion.handler.KeySet;
import net.minecraft.core.registries.Registries;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MINIMUM_EXTREME_EVASION_WINDOW_TICKS = BUILDER
            .comment(
                    "Minimum ticks that the extreme evasion detection window stays open after a roll starts.",
                    "翻滚开始后，极限闪避判定窗口至少保持开启的 tick 数。"
            )
            .defineInRange("minimumExtremeEvasionWindowTicks", 15, 0, 20 * 60);

    private static final ModConfigSpec.IntValue MAXIMUM_EXTREME_EVASION_WINDOW_TICKS = BUILDER
            .comment(
                    "Maximum ticks that the extreme evasion detection window can stay open after a roll starts.",
                    "翻滚开始后，极限闪避判定窗口最多保持开启的 tick 数。"
            )
            .defineInRange("maximumExtremeEvasionWindowTicks", 20, 0, 20 * 60);

    private static final ModConfigSpec.IntValue EXTREME_EVASION_COOLDOWN_TICKS = BUILDER
            .comment(
                    "Ticks after a successful extreme evasion before another one can trigger.",
                    "极限闪避成功后，下一次极限闪避触发前的冷却 tick 数。"
            )
            .defineInRange("extremeEvasionCooldownTicks", 40, 0, 20 * 60);

    private static final ModConfigSpec.DoubleValue EXTREME_EVASION_COOLDOWN_GUI_SCALE = BUILDER
            .comment(
                    "Scale of the extreme evasion cooldown HUD ring.",
                    "极限闪避冷却 HUD 圆环的缩放比例。"
            )
            .defineInRange("extremeEvasionCooldownGuiScale", 1.0D, 0.25D, 4.0D);

    private static final ModConfigSpec.DoubleValue EXTREME_EVASION_COOLDOWN_GUI_RATIO_X = BUILDER
            .comment(
                    "Horizontal HUD anchor ratio multiplied by the scaled GUI width.",
                    "HUD 水平位置锚点比例，会与 GUI 宽度相乘。"
            )
            .defineInRange("extremeEvasionCooldownGuiRatioX", 1.0D, 0.0D, 1.0D);

    private static final ModConfigSpec.DoubleValue EXTREME_EVASION_COOLDOWN_GUI_RATIO_Y = BUILDER
            .comment(
                    "Vertical HUD anchor ratio multiplied by the scaled GUI height.",
                    "HUD 垂直位置锚点比例，会与 GUI 高度相乘。"
            )
            .defineInRange("extremeEvasionCooldownGuiRatioY", 1.0D, 0.0D, 1.0D);

    private static final ModConfigSpec.IntValue EXTREME_EVASION_COOLDOWN_GUI_OFFSET_X = BUILDER
            .comment(
                    "Horizontal pixel offset from the HUD anchor; positive values move right.",
                    "相对于 HUD 锚点的水平像素偏移；正值向右移动。"
            )
            .defineInRange("extremeEvasionCooldownGuiOffsetX", 0, -4096, 4096);

    private static final ModConfigSpec.IntValue EXTREME_EVASION_COOLDOWN_GUI_OFFSET_Y = BUILDER
            .comment(
                    "Vertical pixel offset from the HUD anchor; positive values move down.",
                    "相对于 HUD 锚点的垂直像素偏移；正值向下移动。"
            )
            .defineInRange("extremeEvasionCooldownGuiOffsetY", 0, -4096, 4096);

    private static final ModConfigSpec.IntValue EXTREME_COUNTER_ATTACK_WINDOW_TICKS = BUILDER
            .comment(
                    "Ticks after a successful extreme evasion during which Extreme Counter is available.",
                    "极限闪避成功后，极限反击可用的持续 tick 数。"
            )
            .defineInRange("extremeCounterAttackWindowTicks", 30, 0, 20 * 60);

    private static final ModConfigSpec.IntValue EXTREME_COUNTER_ATTACK_CHARGES = BUILDER
            .comment(
                    "Number of attacks empowered by Extreme Counter after one successful extreme evasion.",
                    "每次极限闪避成功后，极限反击可强化的攻击次数。"
            )
            .defineInRange("extremeCounterAttackCharges", 1, 0, 100);

    private static final ModConfigSpec.BooleanValue ENABLE_EXTREME_COUNTER_ATTACK_CRITICAL = BUILDER
            .comment(
                    "Whether Extreme Counter forces empowered attacks to become critical hits.",
                    "极限反击是否让被强化的攻击必定暴击。"
            )
            .define("enableExtremeCounterAttackCritical", true);

    private static final ModConfigSpec.BooleanValue ENABLE_EXTREME_COUNTER_ATTACK_ARMOR_PIERCING = BUILDER
            .comment(
                    "Whether Extreme Counter makes empowered attacks ignore armor.",
                    "极限反击是否让被强化的攻击无视护甲。"
            )
            .define("enableExtremeCounterAttackArmorPiercing", true);

    private static final ModConfigSpec.BooleanValue ENABLE_EXTREME_COUNTER_GOLD_SHADER = BUILDER
            .comment(
                    "Whether to render the gold shader glow on the held item while Extreme Counter is active.",
                    "极限反击激活期间，是否给手持物品渲染金色发光着色器。"
            )
            .define("enableExtremeCounterGoldShader", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXTREME_COUNTER_ATTACK_DAMAGE_TYPE_WHITELIST = BUILDER
            .comment(
                    "Damage type ids or tags that can consume and trigger Extreme Counter. Prefix tags with #.",
                    "可以消耗并触发极限反击的伤害类型 ID 或标签；标签请以 # 开头。"
            )
            .defineList("extremeCounterAttackDamageTypeWhitelist", List.of("minecraft:arrow", "minecraft:trident"), () -> "", value -> value instanceof String);
    public static final KeySet<net.minecraft.world.damagesource.DamageType> EXTREME_COUNTER_ATTACK_DAMAGE_TYPES = new KeySet<>(Registries.DAMAGE_TYPE);

    private static final ModConfigSpec.BooleanValue ENABLE_BULLET_TIME = BUILDER
            .comment(
                    "Whether successful extreme evasion triggers bullet time.",
                    "极限闪避成功后是否触发子弹时间。"
            )
            .define("enableBulletTime", true);

    private static final ModConfigSpec.BooleanValue ENABLE_BULLET_TIME_IN_MULTIPLAYER = BUILDER
            .comment(
                    "Whether bullet time may trigger in multiplayer worlds and dedicated servers.",
                    "是否允许在多人世界和专用服务器中触发子弹时间。"
            )
            .define("enableBulletTimeInMultiplayer", false);

    private static final ModConfigSpec.BooleanValue BULLET_TIME_EXCEPT_PLAYER = BUILDER
            .comment(
                    "Whether to use TimeScaleLib's mode that slows everything except players. Requires TimeScaleLib; falls back to global time when unavailable.",
                    "是否使用 TimeScaleLib 的“除了玩家以外时间减缓”模式。需要安装 TimeScaleLib；未安装时回退为全局减速。"
            )
            .define("bulletTimeExceptPlayer", false);

    private static final ModConfigSpec.BooleanValue ENABLE_BULLET_TIME_INVULNERABILITY = BUILDER
            .comment(
                    "Whether the player cannot be attacked during bullet time.",
                    "子弹时间期间，玩家是否无法被攻击。"
            )
            .define("enableBulletTimeInvulnerability", true);

    private static final ModConfigSpec.DoubleValue BULLET_TIME_SPEED = BUILDER
            .comment(
                    "World time speed during bullet time. 0.2 means 20 percent speed.",
                    "子弹时间期间的世界时间流速。0.2 表示 20% 速度。"
            )
            .defineInRange("bulletTimeSpeed", 0.3D, 0.01D, 1.0D);

    private static final ModConfigSpec.IntValue BULLET_TIME_DURATION_MILLIS = BUILDER
            .comment(
                    "Real-time bullet time duration in milliseconds.",
                    "子弹时间持续的真实时间，单位为毫秒。"
            )
            .defineInRange("bulletTimeDurationMillis", 3000, 0, 60000);

    private static final ModConfigSpec.IntValue BULLET_TIME_VISUAL_FADE_MILLIS = BUILDER
            .comment(
                    "Real-time fade in/out duration in milliseconds for the bullet-time screen filter.",
                    "子弹时间屏幕滤镜淡入/淡出的真实时间，单位为毫秒。"
            )
            .defineInRange("bulletTimeVisualFadeMillis", 250, 0, 10000);

    private static final ModConfigSpec.BooleanValue ENABLE_BULLET_TIME_SCREEN_SHADER = BUILDER
            .comment(
                    "Whether to render the blue-gray bullet-time screen shader.",
                    "是否渲染子弹时间的蓝灰屏幕着色器。"
            )
            .define("enableBulletTimeScreenShader", true);

    private static final ModConfigSpec.BooleanValue ENABLE_BULLET_TIME_TRIGGER_SOUND = BUILDER
            .comment(
                    "Whether to play the bullet-time trigger sound.",
                    "是否播放子弹时间触发音效。"
            )
            .define("enableBulletTimeTriggerSound", true);

    private static final ModConfigSpec.BooleanValue ENABLE_TRAJECTORY_AABB_DEBUG_RENDERING = BUILDER
            .comment(
                    "Whether to render the extreme evasion trajectory AABBs for debugging.",
                    "是否绘制极限闪避的轨迹碰撞盒调试线框。"
            )
            .define("enableTrajectoryAabbDebugRendering", false);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXTREME_EVASION_ENTITY_BLACKLIST = BUILDER
            .comment(
                    "Entity type ids or tags excluded from extreme evasion trajectory collision checks. Prefix tags with #.",
                    "不参与极限闪避轨迹碰撞检测的实体类型 ID 或标签；标签请以 # 开头。"
            )
            .defineList("extremeEvasionEntityBlacklist", List.of("minecraft:player"), () -> "", value -> value instanceof String);

    private static final ModConfigSpec.DoubleValue BULLET_TIME_TRIGGER_SOUND_VOLUME = BUILDER
            .comment(
                    "Volume of the bullet-time trigger sound.",
                    "子弹时间触发音效的音量。"
            )
            .defineInRange("bulletTimeTriggerSoundVolume", 0.65D, 0.0D, 4.0D);

    private static final ModConfigSpec.DoubleValue BULLET_TIME_TRIGGER_SOUND_PITCH = BUILDER
            .comment(
                    "Pitch of the bullet-time trigger sound.",
                    "子弹时间触发音效的音高。"
            )
            .defineInRange("bulletTimeTriggerSoundPitch", 1.0D, 0.1D, 4.0D);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int minimumExtremeEvasionWindowTicks;
    public static int maximumExtremeEvasionWindowTicks;
    public static int extremeEvasionCooldownTicks;
    public static double extremeEvasionCooldownGuiScale;
    public static double extremeEvasionCooldownGuiRatioX;
    public static double extremeEvasionCooldownGuiRatioY;
    public static int extremeEvasionCooldownGuiOffsetX;
    public static int extremeEvasionCooldownGuiOffsetY;
    public static int extremeCounterAttackWindowTicks;
    public static int extremeCounterAttackCharges;
    public static boolean enableExtremeCounterAttackCritical;
    public static boolean enableExtremeCounterAttackArmorPiercing;
    public static boolean enableExtremeCounterGoldShader;
    public static boolean enableBulletTime;
    public static boolean enableBulletTimeInMultiplayer;
    public static boolean bulletTimeExceptPlayer;
    public static boolean enableBulletTimeInvulnerability;
    public static double bulletTimeSpeed;
    public static int bulletTimeDurationMillis;
    public static int bulletTimeVisualFadeMillis;
    public static boolean enableBulletTimeScreenShader;
    public static boolean enableBulletTimeTriggerSound;
    public static boolean enableTrajectoryAabbDebugRendering;
    public static List<? extends String> extremeEvasionEntityBlacklist;
    public static List<? extends String> extremeCounterAttackDamageTypeWhitelist;
    public static float bulletTimeTriggerSoundVolume;
    public static float bulletTimeTriggerSoundPitch;

    public static void onLoad(ModConfigEvent event) {
        minimumExtremeEvasionWindowTicks = MINIMUM_EXTREME_EVASION_WINDOW_TICKS.get();
        maximumExtremeEvasionWindowTicks = Math.max(
                MAXIMUM_EXTREME_EVASION_WINDOW_TICKS.get(),
                minimumExtremeEvasionWindowTicks
        );
        extremeEvasionCooldownTicks = EXTREME_EVASION_COOLDOWN_TICKS.get();
        extremeEvasionCooldownGuiScale = EXTREME_EVASION_COOLDOWN_GUI_SCALE.get();
        extremeEvasionCooldownGuiRatioX = EXTREME_EVASION_COOLDOWN_GUI_RATIO_X.get();
        extremeEvasionCooldownGuiRatioY = EXTREME_EVASION_COOLDOWN_GUI_RATIO_Y.get();
        extremeEvasionCooldownGuiOffsetX = EXTREME_EVASION_COOLDOWN_GUI_OFFSET_X.get();
        extremeEvasionCooldownGuiOffsetY = EXTREME_EVASION_COOLDOWN_GUI_OFFSET_Y.get();
        extremeCounterAttackWindowTicks = EXTREME_COUNTER_ATTACK_WINDOW_TICKS.get();
        extremeCounterAttackCharges = EXTREME_COUNTER_ATTACK_CHARGES.get();
        enableExtremeCounterAttackCritical = ENABLE_EXTREME_COUNTER_ATTACK_CRITICAL.get();
        enableExtremeCounterAttackArmorPiercing = ENABLE_EXTREME_COUNTER_ATTACK_ARMOR_PIERCING.get();
        enableExtremeCounterGoldShader = ENABLE_EXTREME_COUNTER_GOLD_SHADER.get();
        enableBulletTime = ENABLE_BULLET_TIME.get();
        enableBulletTimeInMultiplayer = ENABLE_BULLET_TIME_IN_MULTIPLAYER.get();
        bulletTimeExceptPlayer = BULLET_TIME_EXCEPT_PLAYER.get();
        enableBulletTimeInvulnerability = ENABLE_BULLET_TIME_INVULNERABILITY.get();
        bulletTimeSpeed = BULLET_TIME_SPEED.get();
        bulletTimeDurationMillis = BULLET_TIME_DURATION_MILLIS.get();
        bulletTimeVisualFadeMillis = BULLET_TIME_VISUAL_FADE_MILLIS.get();
        enableBulletTimeScreenShader = ENABLE_BULLET_TIME_SCREEN_SHADER.get();
        enableBulletTimeTriggerSound = ENABLE_BULLET_TIME_TRIGGER_SOUND.get();
        enableTrajectoryAabbDebugRendering = ENABLE_TRAJECTORY_AABB_DEBUG_RENDERING.get();
        extremeEvasionEntityBlacklist = EXTREME_EVASION_ENTITY_BLACKLIST.get();
        extremeCounterAttackDamageTypeWhitelist = EXTREME_COUNTER_ATTACK_DAMAGE_TYPE_WHITELIST.get();
        EXTREME_COUNTER_ATTACK_DAMAGE_TYPES.updateKeys(extremeCounterAttackDamageTypeWhitelist);
        bulletTimeTriggerSoundVolume = BULLET_TIME_TRIGGER_SOUND_VOLUME.get().floatValue();
        bulletTimeTriggerSoundPitch = BULLET_TIME_TRIGGER_SOUND_PITCH.get().floatValue();
        EchoHandler.reloadEntityTypeBlacklist(extremeEvasionEntityBlacklist);
    }
}
