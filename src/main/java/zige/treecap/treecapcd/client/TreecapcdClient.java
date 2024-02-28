package zige.treecap.treecapcd.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

@Environment(EnvType.CLIENT)
public class TreecapcdClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("treecapcd");
    private boolean isCoolDown = false;
    private final AttackBlockCallback attackBlockCallback  = (player, world, hand, pos, direction) -> {
        if (isCoolDown) return ActionResult.PASS;

        NbtCompound handStackNbt = player.getMainHandStack().getNbt();
        if (handStackNbt == null) return ActionResult.PASS;
        String handStackName = handStackNbt.getCompound("ExtraAttributes").getString("id");
        if (handStackName == null) return ActionResult.PASS;

        if (handStackName.equals("TREECAPITATOR_AXE")) {
            new Thread(() -> {
                Instant start = Instant.now();
                while (!world.getBlockState(pos).isAir()) {
                    if (Duration.between(start, Instant.now()).toMillis() > 2000 ){
                        return;
                    }
                }
                isCoolDown = true;
                new Thread(() -> {
                    try {
                        Thread.sleep(2000 - Duration.between(start, Instant.now()).toMillis());
                        isCoolDown = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }).start();
        }
        return ActionResult.PASS;
    };
    private final HudRenderCallback hudRenderCallback  = (e, phases) -> {
        if (isCoolDown) {
            e.fillGradient(
                    0,
                    0,
                    e.getScaledWindowWidth(),
                    (int) (e.getScaledWindowHeight() * 0.045f),
                    Color.RED.getRGB(),
                    Color.TRANSLUCENT);
        }
    };

    @Override
    public void onInitializeClient() {
        LOGGER.info("Treecapcd client initialized");
        AttackBlockCallback.EVENT.register(attackBlockCallback);
        HudRenderCallback.EVENT.register(hudRenderCallback);
    }
}
