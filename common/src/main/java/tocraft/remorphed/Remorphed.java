package tocraft.remorphed;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.events.common.PlayerEvents;
import tocraft.craftedcore.platform.Platform;
import tocraft.craftedcore.platform.VersionChecker;
import tocraft.remorphed.config.RemorphedConfig;
import tocraft.remorphed.network.NetworkHandler;

public class Remorphed {

	public static final Logger LOGGER = LoggerFactory.getLogger(Remorphed.class);
	public static final String MODID = "remorphed";
	public static String versionURL = "https://raw.githubusercontent.com/ToCraft/remorphed/1.20.2/gradle.properties";
	public static final RemorphedConfig CONFIG = ConfigLoader.read(MODID, RemorphedConfig.class);
	public static List<String> devs = new ArrayList<>();
	static {
		devs.add("1f63e38e-4059-4a4f-b7c4-0fac4a48e744");
	}
	
	public void initialize() {
		PlayerEvents.PLAYER_JOIN.register(player -> {
			String newestVersion = VersionChecker.checkForNewVersion(versionURL);
			if (newestVersion != null && !Platform.getMod(MODID).getVersion().equals(newestVersion))
				player.sendSystemMessage(Component.translatable("ycdm.update", newestVersion));
		});			
		
		if (Platform.getDist().isClient())
			new RemorphedClient().initialize();
		
		NetworkHandler.registerPacketReceiver();
		
		//ShapeEvents.UNLOCK_SHAPE.register(new UnlockShapeCallback());
	}

	public static ResourceLocation id(String name) {
		return new ResourceLocation(MODID, name);
	}
}
