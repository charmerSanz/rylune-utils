package dev.rylune.utils;
import net.minecraft.client.gui.DrawContext;
public final class Crosshair {
    public static void draw(DrawContext ctx,int x,int y) {
        Config c=RyluneUtils.config;
        if(c.crossOutline) paint(ctx,x,y,1,RyluneUtils.argb(0,c.crossOpacity));
        paint(ctx,x,y,0,RyluneUtils.argb(c.crossColor,c.crossOpacity));
    }
    private static void rect(DrawContext d,int x1,int y1,int x2,int y2,int pad,int color) { d.fill(x1-pad,y1-pad,x2+pad,y2+pad,color); }
    private static void paint(DrawContext d,int x,int y,int pad,int color) {
        Config c=RyluneUtils.config; int t=c.crossThickness,g=c.crossGap,l=c.crossLength,a=t/2,b=t-a;
        if(c.crossShape==0 || c.crossShape==2) {
            rect(d,x-g-l,y-a,x-g,y+b,pad,color); rect(d,x+g+1,y-a,x+g+l+1,y+b,pad,color);
            if(c.crossShape==0) rect(d,x-a,y-g-l,x+b,y-g,pad,color);
            rect(d,x-a,y+g+1,x+b,y+g+l+1,pad,color);
        } else if(c.crossShape==3) {
            int r=g+l; rect(d,x-r,y-r,x+r+1,y-r+t,pad,color); rect(d,x-r,y+r-t+1,x+r+1,y+r+1,pad,color);
            rect(d,x-r,y-r+t,x-r+t,y+r-t+1,pad,color); rect(d,x+r-t+1,y-r+t,x+r+1,y+r-t+1,pad,color);
        }
        if(c.crossDot || c.crossShape==1) rect(d,x-a,y-a,x+b,y+b,pad,color);
    }
}
