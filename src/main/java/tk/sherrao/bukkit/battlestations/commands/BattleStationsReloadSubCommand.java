package tk.sherrao.bukkit.battlestations.commands;

import org.bukkit.Sound;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.utils.command.CommandBundle;
import tk.sherrao.bukkit.utils.command.SherSubCommand;

public class BattleStationsReloadSubCommand extends SherSubCommand {

	protected String successMsg, failureMsg;
	protected Sound successSound, failureSound;
	
	public BattleStationsReloadSubCommand( BattleStations pl ) {
		super( "reload", pl );
		
		this.successMsg = pl.getMessagesConfig().getString( "command.reload.success" );
		this.failureMsg = pl.getMessagesConfig().getString( "command.reload.failure" );
		this.successSound = pl.getSoundsConfig().getSound( "command.reload.success" );
		this.failureSound = pl.getSoundsConfig().getSound( "command.reload.failure" );
		
	}

	@Override
	public void onExecute( CommandBundle bundle ) {
		try {
			((BattleStations) pl).getStationManager().reloadMaps();
			bundle.messageSound( successMsg, successSound );
			
		} catch( Exception e ) { 
			log.severe( "Failed to load maps for BattleStations! Printing stacktrace...", e );
			bundle.messageSound( failureMsg, failureSound );
			
		}
	}

}
