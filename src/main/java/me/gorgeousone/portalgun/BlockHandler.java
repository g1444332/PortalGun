package me.gorgeousone.portalgun;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Stores players with the blocks they are lifting at the moment.
 */
public class BlockHandler {

	private PortalMain main;
	private Map<UUID, FallingBlock> liftedBlocks;
	private Map<UUID, Double> holdingDistances;
	private Map<UUID, Block> blockOrigins;

	private double maxBlockTossVelocity;

	public BlockHandler(PortalMain main) {

		this.main = main;

		liftedBlocks = new HashMap<>();
		holdingDistances = new HashMap<>();
		blockOrigins = new HashMap<>();

		maxBlockTossVelocity = 0.5;
	}

	public void unload() {

		for(UUID playerUUID : liftedBlocks.keySet()) {

			FallingBlock liftedBlock = liftedBlocks.get(playerUUID);
			blockOrigins.get(playerUUID).setBlockData(liftedBlock.getBlockData());
		}
	}

	public Set<Map.Entry<UUID, FallingBlock>> getLiftedBlocks() {
		return liftedBlocks.entrySet();
	}

	public boolean isLiftingBlock(Player player) {
		return liftedBlocks.containsKey(player.getUniqueId());
	}

	public FallingBlock getLiftedBlock(Player player) {
		return liftedBlocks.get(player.getUniqueId());
	}

	public Player getPlayerByLiftedBlock(FallingBlock liftedBlock) {

		for (Map.Entry<UUID, FallingBlock> entry : liftedBlocks.entrySet()) {
			if (entry.getValue().equals(liftedBlock))
				return Bukkit.getPlayer(entry.getKey());
		}
		return null;
	}

	public void liftBlock(Player player, Block block) {

		if(isLiftingBlock(player))
			dropLiftedBlock(player);

		new BukkitRunnable() {
			@Override
			public void run() {

				FallingBlock liftedBlock = spawnFloatingBlock(block);
				UUID playerUUID = player.getUniqueId();

				liftedBlocks.put(playerUUID, liftedBlock);
				holdingDistances.put(playerUUID, liftedBlock.getLocation().distance(player.getEyeLocation()));
				blockOrigins.put(playerUUID, block);

			}
		}.runTask(main);
	}

	public void dropLiftedBlock(Player player) {

		if(!isLiftingBlock(player))
			return;

		FallingBlock liftedBlock = getLiftedBlock(player);
		liftedBlock.setGravity(false);
		liftedBlock.setDropItem(false);
		liftedBlock.remove();

		Vector velocity = liftedBlock.getVelocity();

		if (velocity.length() > maxBlockTossVelocity)
			liftedBlock.setVelocity(velocity.normalize().multiply(maxBlockTossVelocity));

		UUID playerUUID = player.getUniqueId();
		liftedBlocks.remove(playerUUID);
		holdingDistances.remove(playerUUID);
		blockOrigins.remove(playerUUID);

	}

	public void respawnLiftedBlock(Player player) {

		if(!isLiftingBlock(player))
			return;

		FallingBlock deadBlock = getLiftedBlock(player);
		FallingBlock liftedBlock = spawnFloatingBlock(deadBlock.getLocation(), deadBlock.getBlockData());
		liftedBlocks.put(player.getUniqueId(), liftedBlock);

	}

	private FallingBlock spawnFloatingBlock(Block block) {

		Location blockMid = block.getLocation().add(0.5, 0, 0.5);
		FallingBlock floatingBlock = spawnFloatingBlock(blockMid, block.getBlockData());

		return floatingBlock;
	}

	private FallingBlock spawnFloatingBlock(Location location, BlockData blockData) {

		FallingBlock floatingBlock = location.getWorld().spawnFallingBlock(location, blockData);
		floatingBlock.setGravity(false);
		floatingBlock.setDropItem(false);

		return  floatingBlock;
	}
}