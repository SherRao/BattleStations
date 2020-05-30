package tk.sherrao.bukkit.battlestations.station;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.massivecraft.factions.Factions;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.utils.plugin.SherPluginFeature;

public class DataManager extends SherPluginFeature {

	private final class Lock {}
	private final Object lock = new Lock();
	
	protected File dataFile;
	protected FileConfiguration data;
	
	public DataManager( BattleStations pl ) {
		super(pl);
		
		this.dataFile = pl.createFile( "data.yml" );
		this.data = YamlConfiguration.loadConfiguration( dataFile );
		
	}
	
	public void save( Station station ) 
			throws IOException {
		synchronized( lock ) {
			Location coreLocation = station.getCore().getLocation();
			String loc = coreLocation.getBlockX() + "-" + coreLocation.getBlockY() + "-" + coreLocation.getBlockZ() + ".";
			data.set( loc + "faction", station.getControllingFaction().getTag() );
			data.set( loc + "shield-shieldtime", station.getShieldTime() );
			data.set( loc + "shield-chargetime", station.getChargeTime() );
			data.set( loc + "core-health", station.getCore().getHealth() );
				
			for( int i = 0; i < station.getTurrets().size(); i++ ) 
				data.set( loc + ".turrets-health." + i, station.getTurrets().get(i).getHealth() );
				
			data.save( dataFile );
				
		}
	}
	
	public void load( Station station ) {
		synchronized( lock ) {
			Location coreLocation = station.getCore().getLocation();
			String loc = coreLocation.getBlockX() + "-" + coreLocation.getBlockY() +  "-" + coreLocation.getBlockZ() + ".";
			if( data.contains( loc ) ) {
				String faction = data.getString( loc + "faction" );
				int shieldTime = data.getInt( loc + "shield-shieldtime" );
				int chargeTime = data.getInt( loc + "shield-chargetime" );
				double coreHealth = data.getDouble( loc + "core-health" );

				station.claimed( Factions.getInstance().getByTag( faction ) );
				station.shieldTime = shieldTime;
				station.chargeTime = chargeTime;
				station.charge( null, chargeTime / 60 );
				station.getCore().initEntity( station, coreHealth );

				for( int i = 0; i < station.getTurrets().size(); i++ ) 
					station.getTurrets().get(i).initEntity( station, data.getInt( loc + ".turrets-health." + i ) );
				
			} else
				return;

		}		
	}
	
}
