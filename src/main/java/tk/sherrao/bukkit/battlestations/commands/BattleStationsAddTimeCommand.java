package tk.sherrao.bukkit.battlestations.commands;

import org.bukkit.Sound;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.battlestations.station.Station;
import tk.sherrao.bukkit.battlestations.station.StationManager;
import tk.sherrao.bukkit.utils.command.CommandBundle;
import tk.sherrao.bukkit.utils.command.SherSubCommand;

public class BattleStationsAddTimeCommand extends SherSubCommand {

	protected StationManager stationMgr;
	
	protected String noStationSpecifiedMsg, noTimeSpecifiedMsg, invalidStationMsg, invalidTimeMsg, successMsg;
	protected Sound noStationSpecifiedSound, noTimeSpecifiedSound, invalidStationSound, invalidTimeSound, successSound;
	
	public BattleStationsAddTimeCommand( BattleStations pl ) {
		super( "addtime", pl );

		this.stationMgr = pl.getStationManager();
		
		this.noStationSpecifiedMsg = pl.getMessagesConfig().getString( "shield.add-time.no-station-specified" );
		this.noTimeSpecifiedMsg = pl.getMessagesConfig().getString( "shield.add-time.no-time-specified" );
		this.invalidStationMsg = pl.getMessagesConfig().getString( "shield.add-time.invalid-station-specified" );
		this.invalidTimeMsg = pl.getMessagesConfig().getString( "shield.add-time.invalid-time-specified" );
		this.successMsg = pl.getMessagesConfig().getString( "shield.add-time.success" );
		
		this.noStationSpecifiedSound = pl.getSoundsConfig().getSound( "shield.add-time.no-station-specified" );
		this.noTimeSpecifiedSound = pl.getSoundsConfig().getSound( "shield.add-time.no-time-specified" );
		this.invalidStationSound = pl.getSoundsConfig().getSound( "shield.add-time.invalid-station-specified" );
		this.invalidTimeSound = pl.getSoundsConfig().getSound( "shield.add-time.invalid-time-specified" );
		this.successSound = pl.getSoundsConfig().getSound( "shield.add-time.success" );
		
	}

	@Override
	public void onExecute( CommandBundle bundle ) {
		if( bundle.argsMoreThan(1) ) {
			if( bundle.argsMoreThan(2) ) {
				Station station = stationMgr.toStation( "station_" + bundle.argAt(1) );
				if( station != null ) { 
					try {
						long time = Long.parseLong( bundle.argAt(2) );
						station.addTime( time );
						bundle.messageSound( successMsg.replace( "[station]", bundle.argAt(1) )
								.replace( "[time]", String.valueOf( time ) ), successSound );
						
					} catch( NumberFormatException e ) { 
						bundle.messageSound( invalidTimeMsg, invalidTimeSound );
						
					}
					
				} else
					bundle.messageSound( invalidStationMsg, invalidStationSound );
				
			} else 
				bundle.messageSound( noTimeSpecifiedMsg.replace( "[alias]", bundle.alias ), noTimeSpecifiedSound );
			
		} else 
			bundle.messageSound( noStationSpecifiedMsg.replace( "[alias]", bundle.alias ), noStationSpecifiedSound );
		
	}

}