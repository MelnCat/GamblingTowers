package dev.melncat.gamblingskyblock

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import dev.melncat.gamblingskyblock.gambling.GamblingListener
import dev.melncat.gamblingskyblock.gambling.GamblingManager
import dev.melncat.gamblingskyblock.gambling.GamblingManager.setupGambling
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin


class GamblingSkyblock : JavaPlugin() {
	
	override fun onEnable() {
		lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
			val commands: Commands = event.registrar()
			commands.register(Commands.literal("togglegambling")
				.requires { it.sender.hasPermission("gamblingskyblock.command.togglegambling") }
				.executes { ctx: CommandContext<CommandSourceStack> ->
					GamblingManager.enabled = !GamblingManager.enabled
					ctx.source.sender.sendPlainMessage("Gambling is now ${
						if (GamblingManager.enabled) "legal" else "illegal"
					}")
					Command.SINGLE_SUCCESS
				}.build(), "gambling", listOf<String>("gambling"))
			setupGambling(this)
		}
		Bukkit.getPluginManager().registerEvents(GamblingListener(this), this)
	}
	
	override fun onDisable() {
		// Plugin shutdown logic
	}
}
