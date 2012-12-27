package objectmapper.data;
import java.util.Map;

//하나의 obj가 매핑됨.
public interface Data {

	//list의 데이터
	
	//이런게 있어야 Data내부에 여러개의 값이 있을 수 있음.
	//data를 순회하는 것과 값을 가져오는 것이 같은 책임인가? //여러 data엘리멘트에 대한 것.
	//boolean next(); 

	//data엘리멘트 하나에 대한 것.
	boolean hasKey(String key);
	Object get(String key);

	//factory를 만들어서 MapData를 외부에 숨길 수 있음.
	//factory method 패턴. new를 숨기는 패턴. -> 구체 클래스를 숨긴다는 소리.
	public static class Factory {
		//map이 final이 아니면 안됨.
		public static Data fromMap(final Map<String, ?> map){
			//꼭 익명내부클래스가 아니라도 상관은 없음.
			return new Data(){

				public boolean hasKey(String key) {
					return map.containsKey(key);
				}

				public Object get(String key) {
					return map.get(key);
				}

			};
		}
	}

	//MapData를 공개할 필요가 없이 Data로만 접근하게 해야 하는데 public으로 해서
	//공개하면 문제가 있을 수 있음.
	/*
	private static class MapData implements Data {
		public boolean hasKey(String key) {
			return map.containsKey(key);
		}

		public Object get(String key) {
			return map.get(key);
		}


	}
	 */


}


