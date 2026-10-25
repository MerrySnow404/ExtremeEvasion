package cc.sighs.extremeevasion.client;

import cc.sighs.extremeevasion.BulletTimeClock;
import cc.sighs.extremeevasion.ExtremeEvasion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExtremeEvasion.MODID, value = Dist.CLIENT)
public final class BulletTimeRenderClockBypass {
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        BulletTimeClock.setBypassBulletTime(event.phase == TickEvent.Phase.START);
    }
}
