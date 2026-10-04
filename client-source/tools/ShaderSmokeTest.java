import java.nio.file.*;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL33.*;

/** Runs in an invisible OpenGL window; no Minecraft installation or player input is touched. */
public class ShaderSmokeTest {
    static Path root;
    static int program(String fragment) throws Exception {
        int vertex = shader(GL_VERTEX_SHADER, Files.readString(root.resolve("fullscreen.vsh")));
        int frag = shader(GL_FRAGMENT_SHADER, Files.readString(root.resolve(fragment + ".fsh")));
        int p = glCreateProgram();
        glAttachShader(p, vertex); glAttachShader(p, frag); glBindAttribLocation(p, 0, "Position"); glLinkProgram(p);
        if (glGetProgrami(p, GL_LINK_STATUS) == 0) throw new AssertionError(glGetProgramInfoLog(p));
        glDeleteShader(vertex); glDeleteShader(frag); return p;
    }
    static int shader(int type, String source) {
        int shader = glCreateShader(type); glShaderSource(shader, source); glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) throw new AssertionError(glGetShaderInfoLog(shader));
        return shader;
    }
    static int texture(int unit, int r, int g, int b) {
        int texture = glGenTextures(); glActiveTexture(GL_TEXTURE0 + unit); glBindTexture(GL_TEXTURE_2D, texture);
        ByteBuffer pixels = BufferUtils.createByteBuffer(16*16*4);
        for (int i=0; i<256; i++) pixels.put((byte)r).put((byte)g).put((byte)b).put((byte)255);
        pixels.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 16, 16, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        return texture;
    }
    static void uniform(int p, String name, float value) { glUniform1f(glGetUniformLocation(p, name), value); }
    static int[] draw() {
        glDrawArrays(GL_TRIANGLES, 0, 6);
        ByteBuffer pixel=BufferUtils.createByteBuffer(4);
        glReadPixels(8,8,1,1,GL_RGBA,GL_UNSIGNED_BYTE,pixel);
        if (glGetError()!=GL_NO_ERROR) throw new AssertionError("OpenGL error");
        return new int[]{pixel.get(0)&255,pixel.get(1)&255,pixel.get(2)&255};
    }
    static void check(boolean value, String name) { if (!value) throw new AssertionError(name); System.out.println("PASS " + name); }
    public static void main(String[] args) throws Exception {
        root=Path.of(args[0]);
        if (!GLFW.glfwInit()) throw new AssertionError("GLFW initialization failed");
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        long window=GLFW.glfwCreateWindow(16,16,"Socket shader validation",0,0);
        if (window==0) throw new AssertionError("OpenGL context failed");
        try {
            GLFW.glfwMakeContextCurrent(window); GL.createCapabilities();
            System.out.println("GPU: " + glGetString(GL_RENDERER));
            int vao=glGenVertexArrays(); glBindVertexArray(vao);
            int vbo=glGenBuffers(); glBindBuffer(GL_ARRAY_BUFFER,vbo);
            glBufferData(GL_ARRAY_BUFFER,new float[]{-1,-1,0, 1,-1,0, 1,1,0, -1,-1,0, 1,1,0, -1,1,0},GL_STATIC_DRAW);
            glEnableVertexAttribArray(0); glVertexAttribPointer(0,3,GL_FLOAT,false,0,0L);
            glViewport(0,0,16,16);
            int p=program("world_effect"); glUseProgram(p);
            glUniform1i(glGetUniformLocation(p,"Sampler0"),0); glUniform1i(glGetUniformLocation(p,"Sampler1"),1);
            texture(0,200,40,20); texture(1,0,0,200);
            uniform(p,"Saturation",1); uniform(p,"Threshold",.9f); uniform(p,"Glow",0); uniform(p,"HistoryMix",0);
            glUniform2f(glGetUniformLocation(p,"Texel"),1f/16,1f/16);
            int[] normal=draw(); check(Math.abs(normal[0]-200)<=1 && Math.abs(normal[1]-40)<=1,"neutral saturation preserves image");
            uniform(p,"Saturation",0); int[] gray=draw(); check(Math.abs(gray[0]-gray[1])<=1 && Math.abs(gray[1]-gray[2])<=1,"zero saturation is grayscale");
            uniform(p,"Saturation",1); uniform(p,"HistoryMix",.5f); int[] mixed=draw(); check(Math.abs(mixed[0]-100)<=1 && Math.abs(mixed[2]-110)<=1,"motion blur mixes current and history");
            uniform(p,"HistoryMix",0); uniform(p,"Threshold",.1f); uniform(p,"Glow",1); int[] bloom=draw(); check(bloom[0]>normal[0],"bloom brightens pixels above threshold");
            uniform(p,"Threshold",1); int[] excluded=draw(); check(Math.abs(excluded[0]-normal[0])<=1,"bloom threshold excludes dark pixels");
            glDeleteProgram(p);
            p=program("sky_effect"); glUseProgram(p);
            uniform(p,"Time",1); uniform(p,"Scale",5); uniform(p,"Intensity",1); uniform(p,"Aspect",1); uniform(p,"Fov",1); uniform(p,"Opacity",1);
            glUniform2f(glGetUniformLocation(p,"Camera"),0,0);
            for (String name : new String[]{"Tint","Neon1","Neon2","Neon3"}) glUniform3f(glGetUniformLocation(p,name),.2f,.6f,1);
            glUniform3f(glGetUniformLocation(p,"Background"),.2f,.3f,.4f);
            for (int i=0;i<5;i++) { uniform(p,"Mode",i); draw(); check(true,"sky mode " + i + " renders"); }
            uniform(p,"Opacity",0); int[] background=draw(); check(Math.abs(background[0]-51)<=1 && Math.abs(background[2]-102)<=1,"sky opacity restores background color");
            glDeleteProgram(p); glDeleteBuffers(vbo); glDeleteVertexArrays(vao);
        } finally { GLFW.glfwDestroyWindow(window); GLFW.glfwTerminate(); }
    }
}
