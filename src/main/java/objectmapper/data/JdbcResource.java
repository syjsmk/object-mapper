package objectmapper.data;

import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

//orm을 만들기 위함.
public class JdbcResource implements Resource {

	//private CSVReader reader;
	private String[] columns;
	private HashMap<String, Integer> keys;
	//private String[] values, nextValues;

	private Reader reader;
	private File file;

	private Connection conn;
	private String table;

	public JdbcResource(String url, String userName, String password) throws SQLException{
		conn = DriverManager.getConnection(url, userName, password);
	}

	public Resource setTable(String table) {
		this.table = table;
		return this;
	}
	

	public Iterator<Data> iterator() {
		
		// persistance layer는 외부 레이어를 많이 쓰기 때문에 exception이 많이 생김.
		// 이러한 exception이 비지니스 로직까지 올라가게 하면 안됨.
		// 올라가게 하려면 exception을 한 단계 추상화한 exception을 올려주거나 해야 함
		// csv든 sql이든 상관없는 exception만 올려주는게 좋은것임.
		// Resource 인터페이스로 추상화를 했으면 얘가 던지는 예외도 추상화된
		// 예외여야 함.
		try {
			Statement statement = conn.createStatement();
			//final ResultSet resultSet = statement.getResultSet();;
			final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + this.table);


			return new Iterator<Data>() {

				public void remove() {
					// TODO Auto-generated method stub

				}

				public Data next() {
					try {
						if(!resultSet.next()) {
							return null;
						}
					} catch (SQLException e) {
						return null;
					}
					return new Data() {

						public boolean hasKey(String key) {
							try{
								resultSet.findColumn(key);
								return true;
							} catch (SQLException e){
								return false;
							}
						}

						public Object get(String key) {
							try {
								return resultSet.getObject(key);
							} catch(SQLException e){
								return null;
							}
						}
					};
				}

				//csvResource의 hasNext처럼 먼저 체크를 하는 방식으로 만들어야함.
				public boolean hasNext() {
					try {
						return (!resultSet.isLast());
					} catch (SQLException e) {
						return false;
					}
				}
			};
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static class Builder {
		private String url;
		private String user;
		private String password;
		private String table;
		
		public Builder setUrl(String url) {
			this.url = url;
			return this;
		}
		
		public Builder setUser(String user) {
			this.user = user;
			return this;
		}
		
		public Builder setPassword(String password) {
			this.password = password;
			return this;
		}
		
		public Builder setTable(String table) {
			this.table = table;
			return this;
		}
		public Resource create() {
			try {
				return new JdbcResource(url, user, password).setTable(table);
			} catch (SQLException e) {
				//여기서 resourceCreateExceptio을 만들어주면 됨.
				e.printStackTrace();
				return null;
			}
		}
	}
}
