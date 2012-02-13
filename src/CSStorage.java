
public abstract class CSStorage {
	public abstract void savedata();
	public abstract void loaddata();
	
	protected Inventory makeInv(String[] loc) throws NumberFormatException, ArrayIndexOutOfBoundsException{
		Inventory inv = null;
		int w = Integer.valueOf(loc[0]);
		int x = Integer.valueOf(loc[1]);
		int y = Integer.valueOf(loc[2]);
		int z = Integer.valueOf(loc[3]);
		loadChunk(w, x, y, z);
		if(etc.getServer().getWorld(w).getBlockIdAt(x, y, z) == 54){
			inv = (Inventory) etc.getServer().getWorld(w).getComplexBlock(x, y, z);
			if(!(inv instanceof Chest)){
				inv = null;
			}
		}
		return inv;
	}
	
	protected void loadChunk(int w, int x, int y, int z){
		World world = etc.getServer().getWorld(w);
		world.loadChunk(x, y, z);
	}
}
