package objectmapper;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Handler;

import javax.naming.Context;
import javax.sql.DataSource;
import javax.swing.text.AbstractDocument.Content;

import objectmapper.ObjectMapper;
import objectmapper.data.CsvResource;
import objectmapper.data.Data;
import objectmapper.data.Resource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ResponseServer;
import org.junit.Before;
import org.junit.Test;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class ObjectMapperTest {

	ObjectMapper mapper;

	Map<String, Object> map;
	Person person;

	@Before
	public void setUp() throws Exception {
		mapper = new ObjectMapper();
		//map = new LinkedHashMap<String, Object>();
		map = new HashMap<String, Object>();
		
		//0817 이 부분을 csv로 뺄 것.
		map.put("name", "value");
		map.put("id", "temp");
		map.put("level", 17);
		
		//map.put("@class", "Man");
		//map.put("gender", "Man"); 이거 Man이 아니라 클래스 패키지까지 다 들어간거였음. org.zeropage.~~ 
		//map.put("gender", "M"); map.put("gender", "W"); // 이렇게 할 수 없을까
		
		// subclass annotation이 추가 후이거 두 부분이 다 돌아가야 함.
		map.put("gender", "M");
		//map.put("gender", "Man");

		//person = new Person();
		person = new Man("dd", "temp", "pmet", 77);
		/*
		person.firstName = "temp";
		person.lastName = "pmet";
		person.level = 77;
		person.id = "dd";
		*/

	}

	@Test
	public void test() {
		//mapper.print(map);
		mapper.print(person);
	}

	@Test
	public void mapToObject(){
		assertThat(person.level, is(not(17)));
		
		//object를 도는 것과 map을 도는 것의 두 가지 방법이 있음.
		//map에서 읽어서 object에 넣는 경우 object의 프로퍼티가 map이 가진 vqlue의
		//상위타입일 때 set이 안되는 문제점이 있을 수 있음.
		
		//map의 값이 person으로 들어가는 테스트
		mapper.mapToObject(map, person);
		//BeanUtils.populate(person, map); // setter가 있어야 동작함.
		
		
		assertThat(person.level, is(17));
	}

	@Test
	public void mapToObject2(){
		//Person타입 p를 새로 만드는 테스트.
		assertThat(person.level, is(77));
		assertThat(person.id, is("dd"));
		
		//Person p = mapper.mapToObject(map, Person.class);
		//여전히 에러가 나는 이유 -> Man에 디폴트 public 생성자가 없어서 그럼.
		//Man에 디폴트 생성자를 만들어줘도 내부에서 부모의 디폴트 생성자를 호출해줘야 하므로
		//Person에도 필요함
		//Person p = mapper.mapToObject(map, Man.class); // 컴파일 타임에 타입이 Man이라는걸 미리 알아야 함.
		//Person p = mapper.mapToObject(map); // 이렇게 하고 싶음.
		Person p = mapper.mapToObject(map, Person.class); //이게 현실적인거임.
		
		assertTrue(p instanceof Man);
		
		assertThat(person.level, is(77));
		assertThat(p.level, is(17));
		
	}
	
	@Test
	public void introspection() throws IntrospectionException{
		//java.lang.reflect 기존에 쓰던 리플렉션
		
		//setter, getter를 이용한 프로퍼티 관련으로 beans가 있음.
		//ui컴포넌트를 다루는 방법들.
		//java.beans.
		
		//introspect //내부를 본다는 소리.
		BeanInfo beanInfo = Introspector.getBeanInfo(Person.class);
		
		System.out.println("--------introspection-------------------");
		for(PropertyDescriptor desc : beanInfo.getPropertyDescriptors()){
			//desc에 getReadMethod, getWriteMethod, propertyEditor(문자열, 숫자를 넣어준 것에 대해서 맞는 특수타입으로 변환)
			
			System.out.println(desc.getDisplayName());
			//prefix를 이용해서 메소드를 찾았던걸 자동으로 처리해줌.
			System.out.println("readmethod : " + desc.getReadMethod());
			System.out.println("writemethod : " + desc.getWriteMethod());
			System.out.println();
		}
	}
	
	@Test
	public void commonsLang(){
		//체이닝 -> 제네릭스가 필요함. 스태틱 메소드로는 체이닝을 할 수 없음.
		//String[] value = Strings.spliter("a b c d").delimeter(" ").limit(3).getArray();
		//이런건 빌더 패턴의 한 종류임. this를 리턴하다가 마지막에 가서 실제 객체를 리턴하는 방식.
		//이런게 최적화에 좋음.
		
		//org.apache.commons.lang3.StringUtils.split("a b c", "[^0-9]", 3); // 이런건 매번 컴파일을 해줘야 함.
		//PropertyUtils.setProperty(obj, entry.getkey(), entry.getvalue()); // 구현부에서 bean에 값을 넣을 때
		
	}
	
	@Test
	public void objectToMap(){
		Map<String, ?> objectToMap = mapper.objectToMap(person);
		Object expect = "temppmet";
		
		assertThat(objectToMap.get("name"), is(expect));
	}
	
	@Test
	public void objectToGivenMap(){

		Object expect = "temppmet";
		
		//assertThat(map.get("name"), is(expect));
		mapper.objectToMap(person, map);
		
		assertThat(map.get("name"), is(expect));
	}

	/*
	public static class Person{
		/*
		//자바의 일반적인 세 가지 접근방법
		private String id;

		//@property // 외부에 공개는 안되어있지만 리플렉션으로는 가져올 수 있음.
		private String name;

		public int level;

		public String getId(){
			return id;
		}

		public String getName(String familyName){
			return name + familyName;
		}
		 
		public String id;

		//@property // 외부에 공개는 안되어있지만 리플렉션으로는 가져올 수 있음.
		private String firstName;
		private String lastName;


		public int level;

		public String getId(){
			return id;
		}

		public String getName(){
			return firstName + lastName;
		}
		
		public void setId(String id){
			this.id = id;
		}
	}
	*/
	
	Map<String, String> stringMap = new HashMap<String, String>();
	public static class StringPerson{
		public String id;
		public String name;
		public int level = 2;
	}
	
	@Test
	public void objectToStringMap(){
		StringPerson p = new StringPerson();
		p.id = "qwer";
		p.name = "asdf";
		
		assertThat(stringMap.get("name"), is(not("asdf")));
		
		//type eraser 때문에 타입을 위해 String.class를 추가
		mapper.objectToMap(p, stringMap, String.class);
		
		assertThat(stringMap.get("name"), is("asdf"));
		
	}
	
	@Test
	public void testCsvResource() throws IOException{
		/*
		Resource resource = new CsvResource("people.csv");

		//리더 상태가 공유됨. 라이브러리 가져다 쓸 때 파악을 잘 하는게 중요함.
		Iterator<Data> iterator = resource.iterator();
		Iterator<Data> iterator2 = resource.iterator();
		
		Object id = iterator.next().get("id");
		Object id2 = iterator2.next().get("id");
		
		assertThat(id, is(id2));
		*/
	}
	
	@Test
	public void testMaps() throws IOException {
		List<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("gender", "M");
		map.put("id", "syjsmk");
		maps.add(map);
		
		map = new HashMap<String, Object>();
		map.put("gender", "F");
		map.put("id", "smksyj");
		
		Resource fromMaps = Resource.Factory.fromMaps(maps);
		
		for(Person person : mapper.resourceToObjects(fromMaps, Person.class)){	
			assertThat(person.getId(), anyOf(is("syjsmk"), is("smksyj")));
			System.out.println("maps : " + person.getId());
		}
		
		Data d = Data.Factory.fromMap(map);
		Person p = mapper.dataToObject(d, Person.class);
	}
	
	@Test
	public void testProperty() throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		
		//props -> map이랑 비슷하게 생긴거임.
		properties.load(new FileReader("person.properties"));
		properties.loadFromXML(new FileInputStream("Person.xml"));
		
		String level = properties.getProperty("level", "1");
		Object name = properties.get("name"); 
		// 제네릭이 없을 때는 object로 꺼냈음. 문자열로 받는게 일반적이어서 getProperties는 String으로 받음.
		//getProperty 없었으면 사용자에게 형변환의 책임을 떠넘긴거임.
	
		for(Entry<Object, Object> entry : properties.entrySet()){
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}
	
	@Test
	public void testJdbc() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		/*
		//드라이버들을 관리.
		DriverManager.
		
		//컴파일 종속성을 런타임 종속성으로 바꾼것임
		Class<Driver> clazz = (Class<Driver>) Class.forName("com.mysql.jdbc.Driver");
		Driver driver = clazz.newInstance();
		//이 부분이 abstract factory
		//Driver driver = new com.mysql.jdbc.Driver();
		Connection conn = driver.connect("", null); // 구현체 중 com.mysql.jdbc.connection
		*/
		/*
		Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
		while(drivers.hasMoreElements()){
			System.out.println(drivers.nextElement());
		}
		*/
		//이걸 보고 com.mysql을 가져와야 한다는걸 알고 관리해줌.
		
		/*
		//driverManager보다 더 상위
		MysqlDataSource ds = new MysqlDataSource();
		//ds.set~~~ 가 많이 들어감. 
		ds.getConnection();
		*/
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://zeropage.org:3306/test", "test", "test");
		Statement statement = conn.createStatement();
		boolean execute = statement.execute("SELECT 1 + 1");
		assertThat(execute, is(true));
		
		ResultSet resultSet = statement.getResultSet();
		while(resultSet.next()){
			//resource의 next가 resultset.next가 하는 일.
			//resultset의 getXXX가 data가 하는 일.
			//각각에 나눠서 넣으면 될 것.
			assertThat(resultSet.getInt(1), is(2));
		}
	}
	
	//1020
	@Test
	public void testUrl() throws URISyntaxException, IOException, InterruptedException, ExecutionException {
		/*
		//java.net.URL URL에 관한 처리를 담당하는 클래스.
		URL url = new URL("http://zeropage.org/?name=test"); // 주소. 해당 주소에 존재하는 자료를 나타냄.
		//URI uri = new URI("http://zeropage.org"); // uri 도 사용 가능. 이쪽은 자료를 식별하기 위한 id의 의미
		// uniform resource identifier, locator. i가 더 큰 범위임.
		
		URLConnection conn = url.openConnection();
		InputStream inputStream = conn.getInputStream();
		*/
		
		final HttpClient client = new DefaultHttpClient();
		Executor executor = Executors.newFixedThreadPool(3); 
		// 다른 여러가지 기본 executor를 제공함. 이걸 쓰면 스레드를 관리 안해줘도 됨.
		
		//final java.util.concurrent.BlockingQueue<HttpResponse> responses = new ArrayBlockingQueue<HttpResponse>(5);
		
		/*
		new Thread(
				// runnable안만들면 template method (상속) run을 오버라이드 하게 되기 때문.
				//runnable 만들면 커맨드패턴 (구성 확장) run을 가진 runnable을 만들어서 넣어주니까.
				new Runnable() {
			
			public void run() {
				HttpGet get = new HttpGet("http://zeropage.org");
				HttpResponse response;
				try {
					response = client.execute(get);
					//HttpEntity entity = response.getEntity();
					//InputStream stream = entity.getContent();
					
					//여기에다가 handler사용으로 handler.onResponse(response); 이렇게.
					responses.offer(response);
					// 하고 이걸 메인스레드에서 꺼내쓰는 형태로 구현을 할 수 있음. 이거와 위의 차이는 어디 스레드에서 실행인가 여부.
					
					// 여기서 stream을 사용하는 커드는 지양해야함.
					// handler.onResponse(response); 같은 식의 구현이 되게 해야 함. 그냥 이렇게 하면 다른 스레드에서 동작.
					// handler는 외부에 있어야 함.
					// handler를 어떻게 할지를 고민을 해야 함. (메인스레드, 추가스레드 어디에서 실행하게 할지)
					// 메인 스레드에서 실제 동작을 하고 IO같은 부분만 추가스레드에서 처리하는 등. 스레드가 하나인 것처럼
					// 행동하기 위해서(node.js가 저렇게 함)
					
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}).start(); 
		// 여기서 직접 run을 하면 안되는거임.
		
		//위도 inputstream이고 이것도 결국 inputstream인데 차이는?
		// -> 중간에 쿠키나 헤더 관리 등이 가능해서 편리함. 여기도 또 abstract factory
		
		while(true) {
			HttpResponse response = responses.poll();
			handler.onResponse(response);
		}
		*/
		
		FutureTask<HttpResponse> future = new FutureTask<HttpResponse>(new Callable<HttpResponse>() {

			public HttpResponse call() throws Exception {
				// TODO Auto-generated method stub
				HttpGet get = new HttpGet("http://zeropage.org");
				HttpResponse response;
				
				try {
					response = client.execute(get);
					return response;
					
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		});
		
		executor.execute(future);
		
		// append code
		
		HttpResponse response = future.get();
		InputStream stream = response.getEntity().getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		
		String line;
		while((line = br.readLine()) != null){
			System.out.println(line);
		}
	}
}

//map에서 읽어오는거 말고 csv에서 person을 읽어올거임.
