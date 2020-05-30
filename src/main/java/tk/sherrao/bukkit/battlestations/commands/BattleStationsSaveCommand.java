package tk.sherrao.bukkit.battlestations.commands;

import java.io.IOException;

import org.bukkit.Sound;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.DataManager;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.command.CommandBundle;
import tk.sherrao.bukkit.utils.command.SherSubCommand;

public class BattleStationsSaveCommand extends SherSubCommand {

	protected StationManager stationMgr;
	protected DataManager dataMgr;
	
	protected String successMsg, failureMsg;
	protected Sound successSound, failureSound;
	
	public BattleStationsSaveCommand( BattleStations pl ) {
		super( "save", pl );
		
		this.stationMgr = pl.getStationManager();
		this.dataMgr = pl.getDataManager();
		
		this.successMsg = pl.getMessagesConfig().getString( "command.save.success" );
		this.failureMsg = pl.getMessagesConfig().getString( "command.save.failure" );
		this.successSound = pl.getSoundsConfig().getSound( "command.save.success" );
		this.failureSound = pl.getSoundsConfig().getSound( "command.save.failure" );

	}

	@Override
	public void onExecute( CommandBundle bundle ) {
		for( Station station : stationMgr.getStations() ) {
			try {
				if( station.getControllingFaction().isWilderness() )
					continue;

				else 
					dataMgr.save( station );
			
			} catch ( IOException e ) { 
				log.severe( "Failed to save data for station at the coordinates of: " + station.location() + ", printing stacktrace...", e ); 
				bundle.messageSound( failureMsg, failureSound );
				return;
				
			}
		}
		
		bundle.messageSound( successMsg, successSound );
		
	}
	
}