package objectmapper;
import objectmapper.annotations.Polymorphism;
import objectmapper.annotations.Subtype;



@Polymorphism(value = "gender",
	subtype = {
			@Subtype(value = {"M", "Man"}, clazz = Man.class),
			@Subtype(value = "F", clazz = Woman.class)
		}
)
//@Polymorphism(value = "gender", subclass = {Man.class, Woman.class}) // 첫 번째 방법
//@Polymorphism("gender") //@class 대신. value = "gender"나 마찬가지임.
//@Polymorphism()
//@Polymorphism(temp = "gender") // 이런식이 됨.
//annotation에 들어갈 수 있는 값은 상수밖에 없음. 인스턴스를 넣을 수 없음.
public abstract class Person {

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
	 */
	String id;

	//@property // 외부에 공개는 안되어있지만 리플렉션으로는 가져올 수 있음.
	String firstName;
	String lastName;


	public int level;
	
	public Person(String id, String firstName, String lastName, int level){
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.level = level;
	}

	//Man, Woman에서 디폴트 생성자 실행 시 Person의 디폴트 생성자가 실행됨. 그래서 만들어줘야 함
	public Person() {
		// TODO Auto-generated constructor stub
	}

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


