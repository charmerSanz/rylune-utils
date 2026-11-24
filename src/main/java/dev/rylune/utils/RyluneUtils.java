package dev.rylune.utils;

import dev.rylune.utils.ui.ModuleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class RyluneUtils implements ClientModInitializer {
    public static Config config = new Config();
    public static KeyBinding menuKey, zoomKey, sprintKey;
    public static boolean sprintActive, zoomLatched;
    private static float zoomAmount = 1f;
    private static long lastFrame;
    private static Object lastWorld;
    @Override public void onInitializeClient() {
        config=Config.load();
        var category=KeyBinding.Category.create(Identifier.of("ryluneutils","controls"));
        menuKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.ryluneutils.menu",GLFW.GLFW_KEY_RIGHT_SHIFT,category));
        zoomKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.ryluneutils.zoom",GLFW.GLFW_KEY_C,category));
        sprintKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.ryluneutils.sprint",GLFW.GLFW_KEY_R,category));
        ClientTickEvents.END_CLIENT_TICK.register(RyluneUtils::tick);
        BlockFill.register();
    }
    private static void tick(MinecraftClient client) {
        if(lastWorld!=client.world) { lastWorld=client.world; zoomLatched=false; if(config.sprintReset || client.world==null) sprintActive=false; }
        while(menuKey.wasPressed()) if(client.currentScreen==null) client.setScreen(new ModuleScreen());
        boolean playing=client.player!=null && client.world!=null && client.currentScreen==null;
        while(zoomKey.wasPressed()) if(playing && config.zoom && config.zoomToggle) zoomLatched=!zoomLatched;
        while(sprintKey.wasPressed()) if(playing && config.sprint) sprintActive=!sprintActive;
        if(!config.sprint) sprintActive=false;
        if(!config.zoom || !config.zoomToggle) zoomLatched=false;
        if(config.sprintReset && client.player!=null && !client.player.isAlive()) sprintActive=false;
    }
    public static float zoomFov(float vanilla) {
        if(!config.zoom){zoomAmount=1f;lastFrame=0;return vanilla;}
        var client=MinecraftClient.getInstance();
        boolean active=config.zoom && zoomKey!=null && client.player!=null && client.currentScreen==null && (config.zoomToggle?zoomLatched:zoomKey.isPressed());
        float target=active?config.zoomFactor:1f;
        if(target==zoomAmount){lastFrame=0;return vanilla/zoomAmount;}
        long now=System.nanoTime(); float dt=lastFrame==0?0.016f:Math.min(.1f,(now-lastFrame)/1_000_000_000f); lastFrame=now;
        if(config.zoomSmooth && config.zoom) zoomAmount+=(target-zoomAmount)*(1f-(float)Math.exp(-config.zoomSpeed*dt)); else zoomAmount=target;
        if(Math.abs(zoomAmount-target)<.002f) zoomAmount=target;
        return vanilla/Math.max(1f,zoomAmount);
    }
    public static int argb(int rgb,int opacity) { return (Math.round(opacity*2.55f)<<24)|(rgb&0xFFFFFF); }
}
