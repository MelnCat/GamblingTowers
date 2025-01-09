package dev.melncat.gamblingtowers

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MultiverseWorld
import dev.melncat.gamblingtowers.gambling.GamblingListener
import dev.melncat.gamblingtowers.gambling.GamblingManager
import dev.melncat.gamblingtowers.gambling.MinigameManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


class GamblingTowers : JavaPlugin() {
	lateinit var minigameWorld: MultiverseWorld
	lateinit var multiverse: MultiverseCore
	lateinit var manager: MinigameManager
	
	override fun onEnable() {
		multiverse = server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore
		minigameWorld = multiverse.mvWorldManager.getMVWorld("minigame") ?: run {
			multiverse.mvWorldManager.addWorld("minigame", World.Environment.NORMAL, "", WorldType.NORMAL, false, "VoidWorldGenerator")
			multiverse.mvWorldManager.getMVWorld("minigame")
		}
		minigameWorld.spawnLocation = Location(minigameWorld.cbWorld, 0.0, 64.5, 0.0)
		manager = MinigameManager(this)
		manager.clearWorld()
		GamblingManager.setupGambling(this)
		lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
			val commands: Commands = event.registrar()
			commands.register(Commands.literal("join")
				.executes { ctx: CommandContext<CommandSourceStack> ->
					val player = ctx.source.sender as? Player ?: return@executes 0
					if (manager.ongoing || manager.players.contains(player.uniqueId)) {
						ctx.source.sender.sendRichMessage("<red>You can't.")
						return@executes 0
					}
					manager.players.add(player.uniqueId)
					for (p in Bukkit.getOnlinePlayers()) p.sendMessage("${player.name} has joined (${manager.players.size})")
					ctx.source.sender.sendPlainMessage("You have joined.")
					Command.SINGLE_SUCCESS
				}.build(), "gambling")
			commands.register(Commands.literal("spectate")
				.executes { ctx: CommandContext<CommandSourceStack> ->
					val player = ctx.source.sender as? Player ?: return@executes 0
					if (!manager.ongoing) {
						ctx.source.sender.sendRichMessage("<red>You can't.")
						return@executes 0
					}
					player.gameMode = GameMode.SPECTATOR
					player.isFlying = true
					player.teleport(minigameWorld.spawnLocation)
					Command.SINGLE_SUCCESS
				}.build(), "gambling")
			commands.register(Commands.literal("start")
				.executes { ctx: CommandContext<CommandSourceStack> ->
					val player = ctx.source.sender as? Player ?: return@executes 0
					if (manager.ongoing) {
						ctx.source.sender.sendRichMessage("<red>You can't.")
						return@executes 0
					}
					manager.start()
					Command.SINGLE_SUCCESS
				}.build(), "gambling")
		}
		Bukkit.getPluginManager().registerEvents(GamblingListener(this), this)
	}
	
	override fun onDisable() {
		// Plugin shutdown logic
	}
}
