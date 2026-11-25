package dev.rylune.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import org.slf4j.LoggerFactory;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("rylune-utils.json");
    public boolean zoom = false, sprint = false, crosshair = false, block = false;
    public int performanceRevision = 0;
    public boolean zoomToggle = false, zoomSmooth = false;
    public int zoomFactor = 4, zoomSpeed = 12;
    public boolean sprintHud = false, sprintReset = true;
    public int sprintColor = 0xFFFFFF, sprintX = 8, sprintY = 8;
    public int crossColor = 0xFFFFFF, crossOpacity = 100, crossLength = 5, crossGap = 3, crossThickness = 1, crossShape = 0;
    public String customPixels = "000000000000000".repeat(7)+"000000010000000"+"000000000000000".repeat(7);
    public boolean crossDot = true, crossOutline = true;
    public int blockColor = 0xB9C8FF, blockOpacity = 100, blockWidth = 1, blockFillOpacity = 0;
    public boolean enabled(int i) { return switch(i) {case 0 -> zoom; case 1 -> sprint; case 2 -> crosshair; default -> block;}; }
    public void toggle(int i) { switch(i) {case 0 -> zoom = !zoom; case 1 -> sprint = !sprint; case 2 -> crosshair = !crosshair; case 3 -> block = !block;} }
    public static int clamp(int n,int min,int max) { return Math.max(min,Math.min(max,n)); }
    private void validate() {
        zoomFactor=clamp(zoomFactor,2,12); zoomSpeed=clamp(zoomSpeed,3,24);
        crossOpacity=clamp(crossOpacity,10,100); crossLength=clamp(crossLength,1,16); crossGap=clamp(crossGap,0,12); crossThickness=clamp(crossThickness,1,5); crossShape=clamp(crossShape,0,4);
        if(customPixels==null || customPixels.length()!=225 || !customPixels.matches("[01]+")) customPixels=new Config().customPixels;
        blockOpacity=clamp(blockOpacity,10,100); blockWidth=clamp(blockWidth,1,6); blockFillOpacity=0;
        sprintX=clamp(sprintX,0,200); sprintY=clamp(sprintY,0,200);
        sprintColor &= 0xFFFFFF; crossColor &= 0xFFFFFF; blockColor &= 0xFFFFFF;
    }
    public static Config load() {
        try { if(Files.exists(FILE)) { Config c=GSON.fromJson(Files.readString(FILE),Config.class); if(c!=null) { c.validate(); if(c.performanceRevision<3){c.performanceRevision=3;c.zoomSmooth=false;c.sprintHud=false;c.blockWidth=1;c.save();} return c; } } }
        catch(Exception e) { LoggerFactory.getLogger("RyluneUtils").warn("Could not read Rylune Utils configuration",e); }
        return new Config();
    }
    public void save() {
        Crosshair.invalidate();
        try { validate(); Files.createDirectories(FILE.getParent()); Path temp=FILE.resolveSibling(FILE.getFileName()+".tmp"); Files.writeString(temp,GSON.toJson(this));
            try { Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING); }
        } catch(Exception e) { LoggerFactory.getLogger("RyluneUtils").warn("Could not save Rylune Utils configuration",e); }
    }
}
