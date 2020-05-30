package tk.sherrao.bukkit.battlestations.commands;

import org.bukkit.Sound;

import tk.sherrao.bukkit.battlestations.BattleStations;
import tk.sherrao.bukkit.utils.command.CommandBundle;
import tk.sherrao.bukkit.utils.command.SherCommand;
import tk.sherrao.bukkit.utils.command.SherSubCommand;
import tk.sherrao.bukkit.utils.config.SherConfiguration;

public class BattleStationsCommand extends SherCommand {

	protected SherSubCommand itemCmd, reloadCmd, saveCmd, addTimeCmd;
	protected SherConfiguration messagesConfig, soundsConfig;
	
	protected String noArgMsg, wrongUsageMsg, noPermsMsg;
	protected Sound noArgSound, wrongUsageSound, noPermsSound;
	
	public BattleStationsCommand( BattleStations pl ) {
		super( "battlestation", pl );
		
		this.itemCmd = new BattleStationsItemSubCommand(pl);
		this.reloadCmd = new BattleStationsReloadSubCommand(pl);
		this.saveCmd = new BattleStationsSaveCommand(pl);
		this.addTimeCmd = new BattleStationsAddTimeCommand(pl);
		this.messagesConfig = pl.getMessagesConfig();
		this.soundsConfig = pl.getSoundsConfig();
		
		this.noArgMsg = messagesConfig.getString( "command.no-args" );
		this.wrongUsageMsg = messagesConfig.getString( "command.wrong-usage" );
		this.noPermsMsg = messagesConfig.getString( "command.no-perms" );
		this.noArgSound = soundsConfig.getSound( "command.no-args" );
		this.wrongUsageSound = soundsConfig.getSound( "command.wrong-usage" );
		this.noPermsSound = soundsConfig.getSound( "command.no-perms" );
		
	}
	
	@Override
	public void onExecute( CommandBundle bundle ) {
		if( bundle.hasArgs() ) {
			String arg = bundle.argAt(0);
			if( arg.equalsIgnoreCase( "reload" ) ) {
				if( bundle.hasOpPermission( "battlestation.reload" ) ) 
					reloadCmd.onExecute( bundle );
				
				else
					bundle.messageSound( noPermsMsg, noPermsSound );
				
			} else if( arg.equalsIgnoreCase( "item" ) ) {
				if( bundle.hasOpPermission( "battlestation.item" ) )
					itemCmd.onExecute( bundle );
				
				else
					bundle.messageSound( noPermsMsg, noPermsSound );
			} else if( arg.equalsIgnoreCase( "save" ) ) {
				if( bundle.hasOpPermission( "battlestation.save" ) )
					saveCmd.onExecute( bundle );
				
				else
					bundle.messageSound( noPermsMsg, noPermsSound );
				
			} else if( arg.equalsIgnoreCase( "addtime" ) ) {
				if( bundle.hasOpPermission( "battlestations.addtime" ) )
					addTimeCmd.onExecute( bundle );
					
				else 
					bundle.messageSound( noPermsMsg, noPermsSound );
				
			} else 
				bundle.messageSound( wrongUsageMsg.replace( "[cmd]", bundle.alias ), wrongUsageSound );
			
		} else 
			bundle.messageSound( noArgMsg.replace( "[cmd]", bundle.alias ), noArgSound );
		
	}
	
}