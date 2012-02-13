import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;


public class CSMySQL extends CSStorage {
	CSData csd;
	ChestSync cs;
	
	public CSMySQL(CSData csd, ChestSync cs){
		this.csd = csd;
		this.cs = cs;
		CreateTable();
	}

	private void CreateTable(){
		String table1 = ("CREATE TABLE IF NOT EXISTS `ChestSync` (`LinkName` varchar(100) NOT NULL, `Owners` TEXT NOT NULL, `ChestALoc` TEXT NOT NULL, `ChestBLoc` TEXT NOT NULL, PRIMARY KEY (`LinkName`))");
		Connection conn = null;
		Statement st = null;
		try{
			conn = getSQLConn();
		}catch(SQLException SQLE){
			cs.log.log(Level.SEVERE, "[ChestSync] Failed to set MySQL Connection!", SQLE);
		}
		if(conn != null){
			try{
				st = conn.createStatement();
				st.execute(table1);
			}catch (SQLException SQLE) {
				cs.log.log(Level.SEVERE, "[ChestSync] Failed to Create MySQL Table!", SQLE);
			}finally{
				try{
					if(st != null && !st.isClosed()){ st.close(); }
					if(conn != null && !conn.isClosed()){ conn.close(); }
				}catch(SQLException SQLE){
					cs.log.log(Level.SEVERE, "[ChestSync] Failed to Close MySQL Connection!", SQLE);
				}
			}
		}
	}
	
	@Override
	public void savedata() {
		ArrayList<String> Keys = new ArrayList<String>();
		HashMap<String, String[]> LinkOwners = csd.getLink();
		HashMap<String, Inventory[]> Inventories = csd.getInvs();
		Connection conn = null;
		PreparedStatement Select = null;
		PreparedStatement Update = null;
		PreparedStatement Insert = null;
		PreparedStatement Delete = null;
		ResultSet rs = null;
		try{
			conn = getSQLConn();
		}
		catch(SQLException SQLE){
			cs.log.log(Level.SEVERE, "[ChestSync] Failed to set MySQL Connection!", SQLE);
		}
		if(conn != null){
			try{
				Select = conn.prepareStatement("SELECT * FROM ChestSync");
				rs = Select.executeQuery();
				while(rs.next()){
					Keys.add(rs.getString("LinkName"));
				}
				Update = conn.prepareStatement("UPDATE ChestSync SET Owners = ?, ChestALoc = ?, ChestBLoc = ? WHERE LinkName = ?");
				Insert = conn.prepareStatement("INSERT INTO ChestSync (LinkName,Owners,ChestALoc,ChestBLoc) VALUES(?,?,?,?)");
				Delete = conn.prepareStatement("DELETE FROM ChestSync WHERE LinkName = ?");
				for(String key : LinkOwners.keySet()){
					try{
						int w, x, y, z;
						StringBuffer Owned = new StringBuffer();
						String[] owners = LinkOwners.get(key);
						String owneds;
						String ChestALoc;
						String ChestBLoc;
						
						for(String owns : owners){
							Owned.append(owns+",");
						}
						
						owneds = Owned.toString();
						Inventory[] invs = Inventories.get(key);
						
						if(invs[0] != null){
							Chest chest = (Chest) invs[0];
							w = chest.getWorld().getType().getId();
							x = chest.getX();
							y = chest.getY();
							z = chest.getZ();
							ChestALoc = (w+","+x+","+y+","+z);
						}
						else{
							ChestALoc = ("null");
						}
						
						if(invs[1] != null){
							Chest chest = (Chest) invs[1];
							w = chest.getWorld().getType().getId();
							x = chest.getX();
							y = chest.getY();
							z = chest.getZ();
							ChestBLoc = (w+","+x+","+y+","+z);
						}
						else{
							ChestBLoc = ("null");
						}
						
						if(Keys.contains(key)){
							Keys.remove(key);
							Update.setString(1, owneds);
							Update.setString(2, ChestALoc);
							Update.setString(3, ChestBLoc);
							Update.setString(4, key);
							Update.addBatch();
						}
						else{
							Insert.setString(1, key);
							Insert.setString(2, owneds);
							Insert.setString(3, ChestALoc);
							Insert.setString(4, ChestBLoc);
							Insert.addBatch();
						}
						
					}catch(NullPointerException NPE){
						continue;
					}catch(ArrayIndexOutOfBoundsException AIOOBE){
						continue;
					}
				}
				
				Update.executeBatch();
				Insert.executeBatch();
				
				if(!Keys.isEmpty()){	
					for(String key : Keys){
						Delete.setString(1, key);
						Delete.addBatch();
					}
					Delete.executeBatch();
				}
			}
			catch(SQLException SQLE){
				cs.log.log(Level.SEVERE, "[ChestSync] Failed to save to ChestSync Table!", SQLE);
			}
			finally{
				try{
					if(Select != null && !Select.isClosed()){
						Select.close();
					}
					if(Update != null && !Update.isClosed()){
						Update.close();
					}
					if(Insert != null && !Insert.isClosed()){
						Insert.close();
					}
					if(Delete != null && !Delete.isClosed()){
						Delete.close();
					}
					if(rs != null && !rs.isClosed()){
						rs.close();
					}
					if(conn != null && !conn.isClosed()){
						conn.close();
					}
				}catch(SQLException SQLE){
					cs.log.log(Level.SEVERE, "[ChestSync] Failed to close MySQL Connection!", SQLE);
				}
			}
		}
	}

	@Override
	public void loaddata() {
		ArrayList<String> Lines = new ArrayList<String>();
		Inventory inva = null;
		Inventory invb = null;
		Inventory[] invs = new Inventory[]{null, null};
		String[] owners;
		String LinkName;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			conn = getSQLConn();
		}
		catch(SQLException SQLE){
			cs.log.log(Level.SEVERE, "[ChestSync] Failed to set MySQL Connection!", SQLE);
		}
		if(conn != null){
			try{
				ps = conn.prepareStatement("SELECT * FROM ChestSync");
				rs = ps.executeQuery();
				while(rs.next()){
					StringBuilder buildline = new StringBuilder();
					buildline.append(rs.getString("LinkName")+"=");
					buildline.append(rs.getString("Owners")+"~");
					buildline.append(rs.getString("ChestALoc")+"~");
					buildline.append(rs.getString("ChestBLoc"));
					Lines.add(buildline.toString());
				}
			}
			catch(SQLException SQLE){
				cs.log.info("?");
				cs.log.log(Level.SEVERE, "[ChestSync] Failed to load from ChestSync Table!", SQLE);
			}
			finally{
				try{
					if(ps != null && !ps.isClosed()){
						ps.close();
					}
					if(rs != null && !rs.isClosed()){
						rs.close();
					}
					if(conn != null && !conn.isClosed()){
						conn.close();
					}
				}catch(SQLException SQLE){
					cs.log.log(Level.SEVERE, "[ChestSync] Failed to close MySQL Connection!", SQLE);
				}
			}
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

			}
			catch(NumberFormatException NFE){
				continue;
			}
			catch(ArrayIndexOutOfBoundsException AIOOBE){
				continue;
			}
		}
	}
	
	private Connection getSQLConn() throws SQLException{
		Connection conn = null;
		conn = etc.getSQLConnection();
		return conn;
	}
}
