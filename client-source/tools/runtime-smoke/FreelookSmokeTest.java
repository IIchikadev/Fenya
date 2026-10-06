package runtime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.render.Camera;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import socket.core.Socket;
public final class FreelookSmokeTest implements ClientModInitializer {
    private int ticks;
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.world == null || mc.player == null || ++ticks != 40) return;
            var freelook = Socket.getInstance().getProcessors().modules().freelook();
            try {
                mc.options.pauseOnLostFocus = false; mc.setScreen(null);
                Socket.getInstance().getProcessors().modules().animations().a(false);
                freelook.a(true);
                var toggle = freelook.getClass().getDeclaredMethod("toggle",boolean.class); toggle.setAccessible(true);
                var yaw = freelook.getClass().getDeclaredField("yaw"); yaw.setAccessible(true);
                var pitch = freelook.getClass().getDeclaredField("pitch"); pitch.setAccessible(true);
                var originalPerspective = mc.options.getPerspective();
                int x = mc.player.getBlockX(), y = mc.player.getBlockY(), z = mc.player.getBlockZ();
                for (int dx=-1; dx<=1; dx++) for (int dy=-1; dy<=3; dy++) for (int dz=-1; dz<=1; dz++) {
                    boolean boundary = Math.abs(dx)==1 || Math.abs(dz)==1 || dy==-1 || dy==3;
                    mc.world.setBlockState(new BlockPos(x+dx,y+dy,z+dz),boundary ? Blocks.STONE.getDefaultState() : Blocks.AIR.getDefaultState(),3);
                }
                mc.player.setPosition(x+.5,y,z+.5);
                mc.player.prevX=x+.5; mc.player.prevY=y; mc.player.prevZ=z+.5;
                toggle.invoke(freelook,true);
                Camera camera = new Camera();
                camera.update(mc.world,mc.player,true,false,1);
                for (int i=0;i<64;i++) camera.updateEyeHeight();
                int checks=0;
                for (float lookPitch : new float[]{-75,-30,0,30,75}) for (int lookYaw=0;lookYaw<360;lookYaw+=15) {
                    yaw.setFloat(freelook,lookYaw); pitch.setFloat(freelook,lookPitch);
                    camera.update(mc.world,mc.player,true,false,1);
                    Vec3d pos=camera.getPos();
                    var ray=mc.world.raycast(new RaycastContext(mc.player.getEyePos(),pos,
                        RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,mc.player));
                    if (ray.getType()!=HitResult.Type.MISS) throw new IllegalStateException("Camera crossed wall at "+lookYaw+" / "+lookPitch);
                    Box nearPlane=new Box(pos.x-.1,pos.y-.1,pos.z-.1,pos.x+.1,pos.y+.1,pos.z+.1);
                    if (mc.world.getBlockCollisions(mc.player,nearPlane).iterator().hasNext())
                        throw new IllegalStateException("Camera near plane inside wall at "+lookYaw+" / "+lookPitch+": "+pos);
                    checks++;
                }
                toggle.invoke(freelook,false);
                if(mc.options.getPerspective()!=originalPerspective) throw new IllegalStateException("Perspective not restored");
                System.out.println("PASS Freelook wall and near-plane collision: "+checks+" yaw/pitch combinations, perspective restored");
            } catch(Throwable error) {error.printStackTrace();System.out.println("FAIL Freelook runtime QA");}
            finally {freelook.a(false);mc.scheduleStop();}
        });
    }
}
