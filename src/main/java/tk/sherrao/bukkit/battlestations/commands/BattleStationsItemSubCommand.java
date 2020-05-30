package tk.sherrao.bukkit.battlestations.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.utils.ItemBuilder;
import tk.sherrao.bukkit.utils.command.CommandBundle;
import tk.sherrao.bukkit.utils.command.SherSubCommand;
import tk.sherrao.bukkit.utils.config.SherConfiguration;

public class BattleStationsItemSubCommand extends SherSubCommand {

	protected SherConfiguration messagesConfig, soundsConfig, stationsConfig;
	protected ItemStack item;
	
	protected String noPlayerMsg, invalidPlayerMsg, giveMsg, receieveMsg;
	protected Sound noPlayerSound, invalidPlayerSound, giveSound, receieveSound;
	
	public BattleStationsItemSubCommand( BattleStations pl ) {
		super( "item", pl );
		
		this.messagesConfig = pl.getMessagesConfig();
		this.soundsConfig = pl.getSoundsConfig();
		this.stationsConfig = pl.getStationsConfig();
		this.item = new ItemBuilder( stationsConfig.getMaterial( "claim-item.item" ) )
				.setName( stationsConfig.getString( "claim-item.title" ) )
				.setLore( stationsConfig.getStringList( "claim-item.lore" ) )
				.toItemStack();
		
		this.noPlayerMsg = messagesConfig.getString( "command.item.no-player-specified" );
		this.invalidPlayerMsg = messagesConfig.getString( "command.item.invalid-player-specified" );
		this.giveMsg = messagesConfig.getString( "command.item.give" );
		this.receieveMsg = messagesConfig.getString( "command.item.receive" );
		this.noPlayerSound = soundsConfig.getSound( "command.item.no-player-specified" );
		this.invalidPlayerSound = soundsConfig.getSound( "command.item.invalid-player-specified" );
		this.giveSound = soundsConfig.getSound( "command.item.give" );
		this.receieveSound = soundsConfig.getSound( "command.item.receive" );
		
	}
	
	@Override
	public void onExecute( CommandBundle bundle ) {
		if( bundle.argsMoreThan(1) ) {
			try {
				Player to = Bukkit.matchPlayer( bundle.argAt(1) ).get(0);
				if( to != null ) {
					bundle.messageSound( giveMsg.replace( "[player]", to.getName() ), giveSound );
					to.sendMessage( receieveMsg );
					pl.playSound( to, receieveSound );
					
					if( bundle.argsMoreThan(2) ) {
						ItemStack is = item.clone();
						is.setAmount( Integer.valueOf( bundle.argAt(2) ) );
						to.getInventory().addItem( is );

					} else
						to.getInventory().addItem( item );
					
				} else
					bundle.messageSound( invalidPlayerMsg.replace( "[input]", bundle.argAt(1) ), invalidPlayerSound );
			
			} catch( IndexOutOfBoundsException e ) { bundle.messageSound( invalidPlayerMsg.replace( "[input]", bundle.argAt(1) ), invalidPlayerSound ); }
			
		} else 
			bundle.messageSound( noPlayerMsg, noPlayerSound );
		
	}

}