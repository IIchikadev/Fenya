package platform;


import aethereal.core.Socket;
import net.fabricmc.api.ClientModInitializer;

public class Initializer implements ClientModInitializer {


    public void onInitializeClient() {
        new Socket();
    }
}
