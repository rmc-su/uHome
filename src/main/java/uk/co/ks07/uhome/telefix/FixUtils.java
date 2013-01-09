package uk.co.ks07.uhome.telefix;

import org.bukkit.Location;
import org.bukkit.block.Block;

class FixUtils {
        protected static boolean isBlockSuitable(Block block) {
            return (!block.getType().isSolid());
        }
        
        protected static boolean isSpaceForPlayer(Block lower) {
            return isBlockSuitable(lower) && isBlockSuitable(lower.getRelative(0, 1, 0));
        }

        protected static Location findSpaceAbove(Location current, int maxSearchHeight) {
            int i;
            
            for (i = current.getBlockY(); (i < current.getWorld().getMaxHeight()) && (i - current.getBlockY() < maxSearchHeight); i++) {
                Block newBlock = current.add(0, i, 0).getBlock();
                
                if (isSpaceForPlayer(newBlock)) {
                    return newBlock.getLocation().add(0.5, 0, 0.5);
                }
            } 
            
            return null;
        }
}
