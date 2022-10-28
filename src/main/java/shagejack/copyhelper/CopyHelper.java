package shagejack.copyhelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CopyHelper.MOD_ID)
public class CopyHelper {

    public static final String MOD_ID = "copyhelper";
    public static final String MOD_NAME = "Copy Helper";
    public static final Logger LOGGER = LogManager.getLogger(CopyHelper.MOD_NAME);

    public CopyHelper() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        try {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LOGGER.info("Registering Events & Keys...");
                modEventBus.addListener(EventHandle::registerKey);
                forgeEventBus.addListener(EventHandle::onKeyInput);
            });
        } catch (Exception e) {
            LOGGER.error("Mod Loading Failed");
            throw e;
        }
    }

}
