package cc.sighs.extremeevasion.compat.roll;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public final class RollCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<RollProvider> PROVIDERS = new ArrayList<>();
    private static boolean initialized;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        register(new MovesLikeMafuyuRollProvider());
        if (ModList.get().isLoaded("combatroll")) {
            register(new CombatRollProvider());
        }
        if (ModList.get().isLoaded("epicfight")) {
            register(new EpicFightRollProvider());
        }
        if (ModList.get().isLoaded("parcool")) {
            register(new ParCoolRollProvider());
        }

        for (RollProvider provider : PROVIDERS) {
            try {
                provider.init();
            } catch (RuntimeException exception) {
                LOGGER.warn("Failed to initialize roll compatibility provider {}", provider.id(), exception);
            }
        }
    }

    public static boolean isRolling(Player player) {
        for (RollProvider provider : PROVIDERS) {
            if (provider.isRolling(player)) {
                return true;
            }
        }
        return false;
    }

    private static void register(RollProvider provider) {
        PROVIDERS.add(provider);
    }
}
