package objectmapper;
import objectmapper.annotations.Subclass;


//@Subclass("F") // 2번으로 가면서 삭제
public class Woman extends Person {
	
	public Woman(){
	}

	public Woman(String id, String firstName, String lastName, int level) {
		super(id, firstName, lastName, level);
		// TODO Auto-generated constructor stub
	}

}
