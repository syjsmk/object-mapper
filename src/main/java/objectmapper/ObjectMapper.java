package objectmapper;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import objectmapper.annotations.Polymorphism;
import objectmapper.annotations.Subclass;
import objectmapper.annotations.Subtype;
import objectmapper.data.Data;
import objectmapper.data.Resource;


public class ObjectMapper {

	public void print(Map<String, ?> map) {
		
		for(Entry<String, ?> entry : map.entrySet()){
			System.out.println("key : " + entry.getKey() + " / value : " + entry.getValue());
		}
	}

	public void print(Object obj) {
		print(objectToMap(obj));
	}

	private String getPropertyName(String name, String prefix) {

		if(name.equals(prefix) || !name.startsWith(prefix)){
			return null;
		}

		name = name.substring(prefix.length()); // Id -> id
		name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

		return name;
	}

	public Map<String, Object> objectToMap(Object obj) {
		return objectToMap(obj, new HashMap<String, Object>());
	}

	public Map<String, Object> objectToMap(Object obj, Map<String, Object> map) {
		return objectToMap(obj, map, Object.class);
	}

	@SuppressWarnings("unchecked")
	public <T> Map<String, T> objectToMap(Object obj, Map<String, T> map, Class<T> valueType) {
		Class<?> clazz = obj.getClass();
		for (Field field : clazz.getFields()){
			try {
				if(valueType.isAssignableFrom(field.getType())){

					Class<? extends T> returnType = field.getType().asSubclass(valueType);
					map.put(field.getName(), (T)field.get(obj));
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				System.err.println(e.getMessage());
			}
		}

		for(Method method : clazz.getMethods()){
			String name = getPropertyName(method.getName(), "get");

			if(name != null && method.getParameterTypes().length == 0){
				try {

					if(valueType.isAssignableFrom(method.getReturnType())){
						map.put(name, (T)method.invoke(obj));
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}

			}
		}
		return map;
	}
	
	//List<T>가 아니라 Iterable<T>인 점도 중요.
	//만약 List<T>면
	//for문을 돌면서 resoucre에서 data를 뽑아서 list.add해줘야 하는데
	//이런 경우는 list에 있는 값이 다 메모리에 올라가야 하기 때문에 문제가 있을 수 있음.
	public <T> Iterable<T> resourceToObjects(final Resource resource, final Class<T> clazz){
		return new Iterable<T>() {
			//Iterator<Data> data = resource.iterator(); 
			//여기 있으면 iterator를 공유하게 됨. 두 개의 상태가 하나로 됨.
			//-> data가 공유자원. 이러면 스레드 문제.
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					//그래서 여기 있어야 됨.
					//final 붙여야 됨.
					Iterator<Data> data = resource.iterator();
					//Resource의 iterator의 상태도 독립적으로 줘야 함. 생겨난 iter가 서로 독립적인 상태를 가짐.
					//iterator를 쓰면 각 iter가 독립적으로 순회가 가능하다. 스레드에서도.
					
					public boolean hasNext() {
						return data.hasNext();
					}

					public T next() {
						return dataToObject(data.next(), clazz);
					}

					public void remove() {
						data.remove();
					}
					
				};
			}
			
		};
	}
	
	public <T> T dataToObject(Data data, Class<T> clazz){

		Class<? extends T> instantiationClazz = clazz;

		Polymorphism polyAnno = clazz.getAnnotation(Polymorphism.class); // clazz.getAnnotations가능
		if(data.hasKey(polyAnno.value())){ //여기서 값이 "@class" 또는 "gender"로 되는거임.
			Object className = data.get(polyAnno.value());
			if(className instanceof String){
				try {
					Class<? extends T> mappingClazz = getMappingClass((String)className, clazz);

					if(mappingClazz == null){
						instantiationClazz = Class.forName((String)className).asSubclass(clazz);
					} 
					else{
						instantiationClazz = mappingClazz;
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		try{
			//return mapToObject(data, instantiationClazz.newInstance());
			return dataToObject(data, instantiationClazz.newInstance());
		}
		catch(InstantiationException ex){
			System.out.println(instantiationClazz.getName());
			ex.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public <T> T dataToObject(Data data, T obj){
		
		Class<?> clazz = obj.getClass();

		for(Field field : clazz.getFields()){
			if(data.hasKey(field.getName())){
				try {
					//여기서 프로퍼티 리스트를 만들어 둬야 2-way가 가능해짐.
					field.set(obj, data.get(field.getName()));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		for(Method method : clazz.getMethods()){
			String name = getPropertyName(method.getName(), "set");
			
			if(name != null && data.hasKey(name) && method.getParameterTypes().length == 1){
				try {
					method.invoke(obj, data.get(name));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return obj;

	}
	
	//mapToObject를 내부에서 dataToObject를 호출하는 방식으로 바꿈.
	//기존의 mapToObject의 호환성을 위해서 함수 모양만 남겨둔 것
	public <T> T mapToObject(Map<String, ?> map, Class<T> clazz){
		return dataToObject(Data.Factory.fromMap(map), clazz); // 맵을 받아서 Data를 만들게.
		// MapData가 코드에 드러나지 않게 됨. 생성 부분이 factory부분으로 감.
		//return dataToObject(new Data.MapData(map), clazz); 
	}
	
	public <T> T mapToObject(Map<String, ?> map, T obj){
		return dataToObject(Data.Factory.fromMap(map), obj);
	}
	

	//map -> object, csv -> object? csv를 map으로 바꿔야 하는건가?
	//ㄴㄴ. csv와 map을 한 단계 추상화. 임시로 record라고.
	
	volatile private Map<Class<?>, Map<String, Class<?>>> cache = new LinkedHashMap<Class<?>, Map<String, Class<?>>>(16, 0.75f, true){

		private static final int MAX_ENTRIES = 10;

		@Override
		protected boolean removeEldestEntry(Map.Entry<java.lang.Class<?>,java.util.Map<String,java.lang.Class<?>>> eldest) {
			return size() > MAX_ENTRIES;
		}
	};

	public <T> Class<? extends T> getMappingClass(String value, Class<T> clazz) {

		Map<String, Class<?>> tree = cache.get(clazz);
		if(tree == null){
			tree = generateCacheTree(clazz);
		}
		Class<?> temp = tree.get(value);
		return temp == null ? null : temp.asSubclass(clazz);
	}
	
	synchronized private Map<String, Class<?>> generateCacheTree(Class<?> clazz){

		Map<String, Class<?>> tree = cache.get(clazz);
		
		if(tree == null){
			tree = new HashMap<String, Class<?>>();

			Polymorphism polyAnno = clazz.getAnnotation(Polymorphism.class);

			for(Class<?> subclazz : polyAnno.subclass()){
				Subclass subAnno = subclazz.getAnnotation(Subclass.class);
				for(String sub : subAnno.value()){
					tree.put(sub, subclazz); //M -> 패키지.Man.class, F -> 패키지.Woman.class 이런식으로 맵에 집어넣음.
				}
			}
			for(Subtype subtype : polyAnno.subtype()){
				for(String sub : subtype.value()){
					tree.put(sub, subtype.clazz()); //M -> 패키지.Man.class, F -> 패키지.Woman.class 이런식으로 맵에 집어넣음.
				}
			}

			cache.put(clazz, tree); // 클래스 정보들이 들어있는 맵을 cache에 집어넣음.
		}
		return tree;

	}	

	private String getMethodName(String name, String prefix) {

		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

		name = prefix + name;

		return name;
	}
}