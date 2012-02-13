import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;


public class CSFlat extends CSStorage{
	private ChestSync cs;
	private CSData csd;
	
	private final String ChestSyncFile = "plugins/config/ChestSync/Chests.locs";
	private final File Dir = new File("plugins/config/ChestSync/");
	private File CSFile = new File(ChestSyncFile);
	
	public CSFlat(CSData csd, ChestSync cs){
		this.csd = csd;
		if(!Dir.exists()){
			Dir.mkdirs();
		}
		if(!CSFile.exists()){
			try {
				CSFile.createNewFile();
			} catch (IOException IOE) {
				this.cs.log.log(Level.SEVERE, "[ChestSync] - Unable to create Chests.locs File!", IOE);
			}
		}
		if(CSFile.exists()){
			loaddata();
		}
	}
	
	@Override
	public void loaddata(){
		ArrayList<String> Lines = new ArrayList<String>();
		Inventory inva = null;
		Inventory invb = null;
		Inventory[] invs = new Inventory[]{null, null};
		String[] owners;
		String LinkName;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(ChestSyncFile));
			String str;
			while ((str = in.readLine()) != null) {
				if (!str.startsWith("#")){
					Lines.add(str);
				}
			}
			in.close();
		} catch (IOException IOE) {
			this.cs.log.log(Level.SEVERE, "[ChestSync] - Unable to Load Chests.locs File!", IOE);
		}
		for(String line : Lines){
			try{
				String[] link = line.split("=");
				LinkName = link[0];
				String[] info = link[1].split("~");
				owners = info[0].split(",");
			
				if(!info[1].equals("null")){
					String[] invaloc = info[1].split(",");
					inva = makeInv(invaloc);
				}
				
				if(!info[2].equals("null")){
					String[] invbloc = info[2].split(",");
					invb = makeInv(invbloc);
				}
				
				invs = new Inventory[]{inva, invb};
				csd.addLink(LinkName, owners);
				csd.addInvs(LinkName, invs);

			}catch(NumberFormatException NFE){
				continue;
			}catch(ArrayIndexOutOfBoundsException AIOOBE){
				continue;
			}
		}
	}

	@Override
	public void savedata() {
		HashMap<String, String[]> LinkOwners = csd.getLink();
		HashMap<String, Inventory[]> Inventories = csd.getInvs();
		PropertiesFile CSF = new PropertiesFile(ChestSyncFile);
		int w, x, y, z;
		ArrayList<String> Keys = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(ChestSyncFile));
			String str;
			  while ((str = in.readLine()) != null) {
				  if (!str.startsWith("#")){
					  String[] key = str.split("=");
					  Keys.add(key[0]);
				  }
			  }
			  in.close();
		} catch (IOException IOE) {
			this.cs.log.log(Level.SEVERE, "[ChestSync] - Unable to Load Chests.locs File!", IOE);
		}
		for(String key : LinkOwners.keySet()){
			try{
				StringBuffer Line = new StringBuffer();
				String[] owners = LinkOwners.get(key);
				for(String owns : owners){
					Line.append(owns+",");
				}
				Inventory[] invs = Inventories.get(key);
				for(Inventory inv : invs){
					if(inv != null){
						Chest chest = (Chest) inv;
						w = chest.getWorld().getType().getId();
						x = chest.getX();
						y = chest.getY();
						z = chest.getZ();
						Line.append("~"+w+","+x+","+y+","+z);
					}
					else{
						Line.append("~null");
					}
				}
				if(Keys.contains(key)){
					Keys.remove(key);
				}
				CSF.setString(key, Line.toString());	
			}catch(NullPointerException NPE){
				continue;
			}catch(ArrayIndexOutOfBoundsException AIOOBE){
				continue;
			}
		}
		for(String key : Keys){
			CSF.removeKey(key);
		}
	}
}
