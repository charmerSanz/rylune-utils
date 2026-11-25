package dev.rylune.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.memory.ObjectPool;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.slf4j.LoggerFactory;
import java.util.Set;

/** Camera-rotation directional blur. No temporal history or CPU pixel readback. */
public final class MotionBlur {
    private static final int[] RADII={2,4,6,8,12,16};
    private static final Identifier[][][] EFFECTS=new Identifier[3][4][6];
    private static final Set<Identifier> TARGETS=Set.of(PostEffectProcessor.MAIN);
    // Conservative: skip alternative render backends and Iris rather than touching their framebuffers.
    private static final boolean UNSUPPORTED=FabricLoader.getInstance().isModLoaded("vulkanmod")||FabricLoader.getInstance().isModLoaded("iris");
    static {
        for(int q=0;q<3;q++)for(int d=0;d<4;d++)for(int r=0;r<6;r++)
            EFFECTS[q][d][r]=Identifier.of("ryluneutils","motion/q"+q+"_d"+d+"_r"+r);
    }
    private static Object world, entity;
    private static boolean valid, failed;
    private static float prevYaw,prevPitch;
    private static long lastFrame, cooldown;
    private static int width,height;
    public static void reset(){valid=false;lastFrame=0;cooldown=0;world=null;entity=null;}
    public static void render(GameRenderer renderer,ObjectPool pool){
        Config c=RyluneUtils.config;var mc=renderer.getClient();
        if(failed||UNSUPPORTED||c.blurStrength==0||mc.world==null||mc.player==null||mc.currentScreen!=null||renderer.getPostProcessorId()!=null){
            reset();return;
        }
        var camera=renderer.getCamera();
        if(!camera.isReady()||(c.blurFirstPersonOnly&&camera.isThirdPerson())||(!c.blurWhileZoom&&c.zoom&&(RyluneUtils.zoomLatched||RyluneUtils.zoomKey.isPressed()))){reset();return;}
        int w=mc.getWindow().getFramebufferWidth(),h=mc.getWindow().getFramebufferHeight();
        float yaw=camera.getYaw(),pitch=camera.getPitch();
        long now=System.nanoTime();
        if(!valid||world!=mc.world||entity!=camera.getFocusedEntity()||width!=w||height!=h||now-lastFrame>250_000_000L){
            world=mc.world;entity=camera.getFocusedEntity();width=w;height=h;prevYaw=yaw;prevPitch=pitch;lastFrame=now;valid=true;return;
        }
        float dx=MathHelper.wrapDegrees(yaw-prevYaw),dy=pitch-prevPitch;
        long elapsed=now-lastFrame;lastFrame=now;prevYaw=yaw;prevPitch=pitch;
        if(Math.abs(dx)>80||Math.abs(dy)>65)return;
        float ax=Math.abs(dx),ay=Math.abs(dy),motion=Math.max(ax,ay);
        if(motion<=c.blurDeadzone*.015f)return;
        int fps=mc.getCurrentFps(),q=c.blurQuality;
        if(c.blurFpsGuard){
            if(fps>0&&fps<c.blurMinFps){cooldown=now+750_000_000L;return;}
            if(now<cooldown)return;
        }
        if(c.blurAdaptive){
            if(fps>0&&fps<c.blurMinFps+25||elapsed>20_000_000L)q=0;
            else if(fps>0&&fps<c.blurMinFps+60||elapsed>12_000_000L)q=Math.min(q,1);
        }
        float radius=Math.min(c.blurMaxRadius,motion*(c.blurSensitivity*.04f)*(c.blurStrength*.045f));
        if(radius<.7f)return;
        int r=0;for(int i=1;i<RADII.length;i++)if(RADII[i]<=c.blurMaxRadius&&radius>=(RADII[i]+RADII[i-1])*.5f)r=i;
        int dir;
        if(c.blurDirection!=0)dir=c.blurDirection-1;
        else if(ax>ay*2.4f)dir=0;
        else if(ay>ax*2.4f)dir=1;
        else dir=dx*dy>=0?3:2; // texture Y points up
        try {
            var effect=mc.getShaderLoader().loadPostEffect(EFFECTS[q][dir][r],TARGETS);
            if(effect==null){failed=true;LoggerFactory.getLogger("RyluneUtils").warn("Motion blur shader could not load; disabled for this session.");return;}
            effect.render(mc.getFramebuffer(),pool);
        }catch(RuntimeException e){failed=true;LoggerFactory.getLogger("RyluneUtils").warn("Motion blur rendering failed; disabled for this session.",e);}
    }
}
