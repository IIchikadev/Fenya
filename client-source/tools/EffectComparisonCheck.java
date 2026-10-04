import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
public class EffectComparisonCheck {
 static BufferedImage read(Path root,String name)throws Exception{return ImageIO.read(root.resolve("compare-"+name+".png").toFile());}
 static double chroma(BufferedImage image){double sum=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getRGB(x,y),r=c>>16&255,g=c>>8&255,b=c&255;sum+=Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b));}return sum/(image.getWidth()*image.getHeight());}
 static double light(BufferedImage image){double sum=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getRGB(x,y);sum+=(c>>16&255)+(c>>8&255)+(c&255);}return sum/(3*image.getWidth()*image.getHeight());}
 static double difference(BufferedImage a,BufferedImage b){double sum=0;int h=a.getHeight()/2;for(int y=0;y<h;y++)for(int x=0;x<a.getWidth();x++){int c=a.getRGB(x,y),d=b.getRGB(x,y);for(int s:new int[]{0,8,16})sum+=Math.abs((c>>s&255)-(d>>s&255));}return sum/(3*a.getWidth()*h);}
 static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);System.out.println("PASS "+message);}
 public static void main(String[] args)throws Exception{
 Path root=Path.of(args[0]);var base=read(root,"baseline");var gray=read(root,"saturation-zero");var glow=read(root,"bloom");
 double c=chroma(gray),l=light(glow)-light(base),s=difference(base,read(root,"sky")),m=difference(read(root,"motion"),read(root,"motion-off"));
 System.out.printf("grayscale chroma=%.3f; bloom brightness delta=%.3f; sky delta=%.3f; motion delta=%.3f%n",c,l,s,m);
 check(chroma(base)>10&&c<1,"Saturation changes actual Minecraft framebuffer to grayscale");
 check(l>15,"Bloom visibly brightens actual Minecraft framebuffer");
 check(s>10,"SkyShader changes visible sky");
 check(m>3,"Motion Blur retains visible previous-camera history");
 }
}
