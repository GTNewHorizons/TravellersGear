package travellersgear.common.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import travellersgear.TravellersGear;
import travellersgear.api.IActiveAbility;
import travellersgear.client.ClientProxy;
import travellersgear.common.network.MessageOpenGui;

public class TGClientCommand extends CommandBase
{

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	@Override
	public String getCommandName() 
	{
		return "travellersgear";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if(sender instanceof EntityPlayer && args.length>=1 && args[0].equalsIgnoreCase("gui") && ((EntityPlayer)sender).worldObj.isRemote)
			TravellersGear.packetHandler.sendToServer(new MessageOpenGui((EntityPlayer) sender,2));
		if(sender instanceof EntityPlayer && args.length>=1 && args[0].equalsIgnoreCase("toolDisplay") && ((EntityPlayer)sender).worldObj.isRemote)
			TravellersGear.packetHandler.sendToServer(new MessageOpenGui((EntityPlayer) sender,3));
		if (sender instanceof EntityPlayer && args.length>=1 && args[0].equalsIgnoreCase("bind") && ((EntityPlayer)sender).worldObj.isRemote) {
			EntityPlayer player = (EntityPlayer) sender;
			if (args.length == 1) {
				player.addChatMessage(new ChatComponentText("Usage: travellersgear bind {1,2,3}"));
				return;
			}
			int key = MathHelper.parseIntWithDefault(args[1], 0);
			if (key < 1 || key > 3) {
				player.addChatMessage(new ChatComponentText("Usage: travellersgear bind {1,2,3}"));
				return;
			}
			ItemStack stack = player.getHeldItem();
			if (stack == null) {
				player.addChatMessage(new ChatComponentText("You should hold in hand item you want to bind"));
				return;
			}
			if (stack.getItem() instanceof IActiveAbility) {
				String code = Item.itemRegistry.getNameForObject(stack.getItem());
				player.addChatMessage(new ChatComponentText("Bind " + code + " to key " + key));
				ClientProxy.bindKey(key - 1, code);
			} else {
				player.addChatMessage(new ChatComponentText("Item has no active ability"));
			}
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		if(args==null || (args.length==1&&args[0].isEmpty()))
			return Arrays.asList("gui", "toolDisplay", "bind");
		return null;
	}

}