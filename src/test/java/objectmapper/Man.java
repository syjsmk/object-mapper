package objectmapper;
import objectmapper.annotations.Subclass;


//@Subclass("M")
public class Man extends Person {
	
	//mapToObject를 위해서 디폴트 생성자가 필요함. instantiationClazz.newInstance() 이 부분.
	public Man(){}

	//디폴트 생성자가 아닌 생성자가 있으면 디폴트 생성자 자동으로 안해줌.
	public Man(String id, String firstName, String lastName, int level) {
		super(id, firstName, lastName, level);
		// TODO Auto-generated constructor stub
	}

}
