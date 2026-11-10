# ExtremeEvasion 使用说明

ExtremeEvasion 是一个围绕“翻滚闪避”和“极限反击”的战斗增强模组。玩家在翻滚窗口内躲过怪物攻击时，会触发极限闪避，取消这次危险，并获得短时间的极限反击机会。单人游玩时还可以进入子弹时间。

## 安装需求

- Minecraft `1.20.1`
- Forge `47.x`
- 需要至少安装一个受支持的翻滚模组

当前支持的闪避/翻滚来源：
- Moves Like Mafuyu 高速企鹅
- Combat Roll 战斗翻滚
- Epic Fight 史诗战斗
- ParCool 跑酷

把本模组 jar 放入 `mods` 文件夹即可。具体翻滚按键和动作设置由对应翻滚模组控制。

TimeScaleLib is optional. Install the Forge 1.20.1 version from the Modrinth page when using `bulletTimeExceptPlayer`.

## 核心机制

### 极限闪避

当玩家开始翻滚时，模组会打开一个极限闪避判定窗口。窗口内如果怪物对玩家发动近战攻击，或者残影被攻击命中，就会触发极限闪避。

触发成功后：

- 本次攻击会被取消。
- 本次翻滚不会重复触发极限闪避。
- 残影和 AI 诱导判定会立刻清理。
- 玩家获得极限反击。
- 单人游玩时可触发子弹时间。

极限闪避窗口有最短和最长时间限制。默认至少 `15tick`，最多 `20tick`。这用于兼容不同翻滚模组的动作长度，避免窗口太短漏判，也避免窗口异常延长。

### 残影与近战诱导

残影主要用于兜底弹射物或特殊伤害来源。近战怪物的关键判定在怪物真正准备执行近战攻击时触发，而不是等玩家实际受到伤害后再判断。

因此如果你看到怪物挥手、挥斧，并且此时仍处于极限闪避窗口内，就应该能触发极限闪避。

### 极限反击

极限闪避成功后，玩家会获得极限反击。

默认情况下，极限反击会强化接下来的 `1` 次攻击：

- 可配置为必定暴击。
- 可配置为无视护甲。
- 触发后会立刻完成武器攻击冷却。
- 手持物品会显示金色发光高亮。

极限反击按“攻击次数”消耗，而不是按“伤害次数”消耗。攻击命中目标后，金色高亮会淡出。

### 子弹时间

子弹时间只会在单人游玩中触发。

不会触发子弹时间的情况：

- 多人服务器
- 专用服务器
- 单人世界开放到局域网后

子弹时间默认持续 `3000ms`，时间流速默认是 `30%`。期间可以启用蓝灰屏幕滤镜、边缘模糊、触发音效，以及“子弹时间内无法被攻击”。

## 配置文件

配置文件由 Forge 自动生成，通常位于：

```text
config/extremeevasion-common.toml
```

如果你已经启动过游戏，修改配置后通常需要重启游戏或重新加载配置才会生效。

### 极限闪避窗口

```toml
minimumExtremeEvasionWindowTicks = 15
maximumExtremeEvasionWindowTicks = 20
```

`minimumExtremeEvasionWindowTicks` 控制翻滚开始后判定窗口至少保持多久。  
`maximumExtremeEvasionWindowTicks` 控制判定窗口最多保持多久。

如果最大值小于最小值，模组会按最小值处理，避免配置错误导致窗口无效。

### 极限闪避冷却

```toml
extremeEvasionCooldownTicks = 40
enableExtremeEvasionCooldownGui = false
```

`extremeEvasionCooldownTicks` 控制一次极限闪避成功后，下一次极限闪避能够触发前的冷却时间，单位为 tick；`20tick` 约等于 `1` 秒。设为 `0` 可关闭冷却。
`enableExtremeEvasionCooldownGui` 控制是否显示极限闪避冷却条 HUD。

冷却进度 HUD 的位置和大小也可配置：

```toml
extremeEvasionCooldownGuiScale = 1.0
extremeEvasionCooldownGuiRatioX = 1.0
extremeEvasionCooldownGuiRatioY = 1.0
extremeEvasionCooldownGuiOffsetX = -46
extremeEvasionCooldownGuiOffsetY = -16
```

`extremeEvasionCooldownGuiScale` 控制圆环缩放比例，范围为 `0.25` 到 `4.0`。`extremeEvasionCooldownGuiRatioX/Y` 控制位置锚点，分别与 `getGuiScaledWidth()` 和 `getGuiScaledHeight()` 相乘；默认 `1.0/1.0` 表示右下角。X/Y 偏移以 GUI 像素为单位，相对于锚点调整；正 X 向右，正 Y 向下。

### 极限反击

```toml
extremeCounterAttackWindowTicks = 30
extremeCounterAttackCharges = 1
enableExtremeCounterAttackCritical = true
enableExtremeCounterAttackArmorPiercing = true
enableExtremeCounterGoldShader = true
extremeCounterAttackDamageTypeWhitelist = ["minecraft:arrow", "minecraft:trident"]
```

`extremeCounterAttackWindowTicks` 是极限反击可用时间，`20tick` 约等于 `1` 秒。  
`extremeCounterAttackCharges` 是极限反击可强化的攻击次数。  
`enableExtremeCounterAttackCritical` 控制是否必定暴击。  
`enableExtremeCounterAttackArmorPiercing` 控制是否无视护甲。  
`enableExtremeCounterGoldShader` 控制是否显示手持物品金色高亮。
`extremeCounterAttackDamageTypeWhitelist` 列出可以消耗极限反击次数的伤害类型 ID 或标签，默认允许箭矢和三叉戟；标签以 `#` 开头。

### 子弹时间

```toml
enableBulletTime = true
enableBulletTimeInMultiplayer = false
bulletTimeExceptPlayer = false
enableBulletTimeInvulnerability = true
bulletTimeSpeed = 0.3
bulletTimeDurationMillis = 3000
```

Set `enableBulletTimeInMultiplayer = true` to allow bullet time on multiplayer or dedicated servers. Set
`bulletTimeExceptPlayer = true` to use TimeScaleLib's player-exempt scaling mode when TimeScaleLib is installed;
without the optional dependency, the implementation falls back to the legacy global clock.

`enableBulletTime` 控制是否启用子弹时间。  
`enableBulletTimeInvulnerability` 控制子弹时间内玩家是否无法被攻击。  
`bulletTimeSpeed = 0.3` 表示子弹时间期间世界时间流速为 `30%`。  
`bulletTimeDurationMillis` 是持续时间，单位为毫秒。

### 视觉与音效

```toml
bulletTimeVisualFadeMillis = 250
enableBulletTimeScreenShader = true
enableBulletTimeTriggerSound = true
bulletTimeTriggerSoundVolume = 0.65
bulletTimeTriggerSoundPitch = 1.0
```

`enableBulletTimeScreenShader` 控制蓝灰子弹时间滤镜。  
`bulletTimeVisualFadeMillis` 控制滤镜淡入淡出时间。  
`enableBulletTimeTriggerSound` 控制子弹时间触发音效。  
音量和音高分别由 `bulletTimeTriggerSoundVolume`、`bulletTimeTriggerSoundPitch` 控制。

## 常见问题

### 为什么多人游戏没有子弹时间？

子弹时间会改变世界时间流速。为了避免影响其他玩家，它只允许在未开放局域网的单人世界中触发。

### 为什么同一次翻滚只触发一次？

这是设计行为。同一次翻滚成功触发极限闪避后，会立刻清理残影和 AI 诱导判定，并标记本次翻滚已消耗，防止重复触发。

### 为什么手持物品变金？

这表示你当前拥有极限反击。极限反击消耗完或持续时间结束后，金色高亮会淡出。

### 可以关闭视觉效果吗？

可以：

```toml
enableExtremeCounterGoldShader = false
enableBulletTimeScreenShader = false
```

### 可以只保留极限反击，不要子弹时间吗？

可以：

```toml
enableBulletTime = false
```

关闭子弹时间后，极限闪避和极限反击仍会正常工作。
