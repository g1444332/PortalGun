package me.gorgeousone.portalgun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the dragging of lifted blocks continuously.
 */
public class BlockDragHandler {

	private PortalMain main;
	private BlockHandler blockHandler;
	private BukkitRunnable blockDragTask;

	public BlockDragHandler(PortalMain main, BlockHandler blockHandler) {
		this.main = main;
		this.blockHandler = blockHandler;

		startBlockDragTask();
	}

	public void unload() {
		blockDragTask.cancel();
	}

	private void startBlockDragTask() {
		blockDragTask = new BukkitRunnable() {
			@Override
			public void run() {
				for(Map.Entry<UUID, FallingBlock> entry : blockHandler.getLiftedBlocks()) {
					Player player = Bukkit.getPlayer(entry.getKey());
					FallingBlock liftedBlock = entry.getValue();

					if (liftedBlock.isDead())
						blockHandler.respawnLiftedBlock(player);

					Vector newVelocity = calculateDragVelocity(player, liftedBlock);
					Location blockLoc = liftedBlock.getLocation();

					if (newVelocity.getY() < 0 && isAboveGround(liftedBlock))
						newVelocity.setY(0);

					liftedBlock.setVelocity(newVelocity);

					// Уменьшение скорости игрока
				}
			}
		};

		blockDragTask.runTaskTimer(main, 0, 1);
	}

	private Vector calculateDragVelocity(Player player, FallingBlock liftedBlock) {
		Location playerLoc = player.getLocation(); // Изменили на getEyeLocation()
		Location headLoc = playerLoc.clone().add(0, 1.4, 0);

		Location blockLoc = liftedBlock.getLocation();
		double dragSpeedFactor = 1.0;
		return headLoc.clone().subtract(blockLoc).toVector().multiply(dragSpeedFactor);
	}

	private boolean isAboveGround(FallingBlock fallingBlock) {

		Location blockLoc = fallingBlock.getLocation();

		if (blockLoc.getY() % 1 > 0.1)
			return false;

		List<Vector> relativeBlockEdges = new ArrayList<>(Arrays.asList(
				new Vector(-0.5, 0, -0.5),
				new Vector(0.5, 0, -0.5),
				new Vector(0.5, 0, 0.5),
				new Vector(-0.5, 0, 0.5)
		));

		for (Vector relativeEdge : relativeBlockEdges) {

			Location blockEdge = blockLoc.clone().add(relativeEdge);
			blockEdge.subtract(0, 0.1, 0);

			if (blockEdge.getBlock().getType().isSolid())
				return true;
		}

		return false;
	}
}

