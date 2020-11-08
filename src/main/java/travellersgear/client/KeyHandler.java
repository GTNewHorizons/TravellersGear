package travellersgear.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import travellersgear.TravellersGear;
import travellersgear.client.handlers.ActiveAbilityHandler;
import travellersgear.client.handlers.CustomizeableGuiHandler;
import travellersgear.common.network.MessageActiveAbility;
import travellersgear.common.network.MessageOpenGui;
import travellersgear.common.network.MessageSlotSync;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import travellersgear.common.network.old.PacketActiveAbility;

public class KeyHandler
{
	public static KeyBinding openInventory = new KeyBinding("TG.keybind.openInv", 71, "key.categories.inventory");
	public static KeyBinding activeAbilitiesWheel = new KeyBinding("TG.keybind.activeaAbilities", 19, "key.categories.inventory");
	public static KeyBinding activeAbility1 = new KeyBinding("TG.keybind.activeaAbility1", Keyboard.KEY_NONE, "key.categories.inventory");
	public static KeyBinding activeAbility2 = new KeyBinding("TG.keybind.activeaAbility2", Keyboard.KEY_NONE, "key.categories.inventory");
	public static KeyBinding activeAbility3 = new KeyBinding("TG.keybind.activeaAbility3", Keyboard.KEY_NONE, "key.categories.inventory");
	public boolean[] keyDown = {false,false};
	public boolean[] abilityKeyDown = {false,false,false};
	public static float abilityRadial;
	public static boolean abilityLock = false;

	public KeyHandler()
	{
		ClientRegistry.registerKeyBinding(openInventory);
		ClientRegistry.registerKeyBinding(activeAbilitiesWheel);
		ClientRegistry.registerKeyBinding(activeAbility1);
		ClientRegistry.registerKeyBinding(activeAbility2);
		ClientRegistry.registerKeyBinding(activeAbility3);
	}

	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event)
	{
		if(Keyboard.isCreated() && event.side!=Side.SERVER && event.phase==TickEvent.Phase.START && FMLClientHandler.instance().getClient().inGameHasFocus)
		{
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if(player==null)
				return;

			if(openInventory.getIsKeyPressed() && !keyDown[0])
			{
				boolean[] hidden = new boolean[CustomizeableGuiHandler.moveableInvElements.size()];
				for(int bme=0;bme<hidden.length;bme++)
					hidden[bme] = CustomizeableGuiHandler.moveableInvElements.get(bme).hideElement;
				TravellersGear.packetHandler.sendToServer(new MessageSlotSync(player,hidden));
				//				PacketPipeline.INSTANCE.sendToServer(new PacketSlotSync(player,hidden));
				TravellersGear.packetHandler.sendToServer(new MessageOpenGui(player,0));
				//				PacketPipeline.INSTANCE.sendToServer(new PacketOpenGui(player,0));
				keyDown[0] = true;
			}
			else if(keyDown[0])
				keyDown[0] = false;

			Object[][] gear = ActiveAbilityHandler.instance.buildActiveAbilityList(player);
			checkAbilityKey(activeAbility1, 0, gear, player);
			checkAbilityKey(activeAbility2, 1, gear, player);
			checkAbilityKey(activeAbility3, 2, gear, player);
			if(activeAbilitiesWheel!=null && activeAbilitiesWheel.getIsKeyPressed() && !keyDown[1] && gear.length>0)
			{
				if(abilityLock)
				{
					abilityLock=false;
					keyDown[1] = true;
				}
				else if(FMLClientHandler.instance().getClient().inGameHasFocus)
				{
					if(abilityRadial<1)
						abilityRadial += ClientProxy.activeAbilityGuiSpeed;
					if(abilityRadial>1)
						abilityRadial=1f;
					if(abilityRadial>=1)	
					{
						abilityLock=true;
						keyDown[1] = true;
					}
				}
			}
			else
			{
				if(keyDown[1] && !activeAbilitiesWheel.getIsKeyPressed())
					keyDown[1]=false;
				if(!abilityLock)
				{
					if(abilityRadial>0)
						abilityRadial -= ClientProxy.activeAbilityGuiSpeed;
					if(abilityRadial<0)
						abilityRadial=0f;
				}
			}
		}
	}

	private void checkAbilityKey(KeyBinding activeAbility, int i, Object[][] gear, EntityPlayer player) {
		if(activeAbility != null && activeAbility.getIsKeyPressed() && !abilityKeyDown[i] && gear.length > i) {
			if(gear[i][0]!=null) {
				TravellersGear.packetHandler.sendToServer(new MessageActiveAbility(player, (Integer) gear[i][1]));
				PacketActiveAbility.performAbility(player, (Integer) gear[i][1]);
				abilityKeyDown[i] = true;
			}
		}
		if(abilityKeyDown[i] && !activeAbility.getIsKeyPressed()) {
			abilityKeyDown[i] = false;
		}
	}
}
