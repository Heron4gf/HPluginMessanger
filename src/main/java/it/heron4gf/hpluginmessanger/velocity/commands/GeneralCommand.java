package it.heron4gf.hpluginmessanger.velocity.commands;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.heron4gf.hpluginmessanger.velocity.VelocityHPluginMessanger;

public abstract class GeneralCommand implements SimpleCommand {

    protected VelocityHPluginMessanger plugin = VelocityHPluginMessanger.getInstance();

    private CommandManager commandManager = plugin.getProxyServer().getCommandManager();

    public GeneralCommand(String command, String... aliases) {
        super();
        if(aliases.length == 0) commandManager.register(commandManager.metaBuilder(command).plugin(plugin).build(),this);
        else commandManager.register(commandManager.metaBuilder(command).aliases(aliases).plugin(plugin).build(),this);

        plugin.getLogger().info("Registered command "+command+" "+aliases);
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        handleCommand(source,args);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return true;
    }

    public abstract void handleCommand(CommandSource source, String[] args);

}
