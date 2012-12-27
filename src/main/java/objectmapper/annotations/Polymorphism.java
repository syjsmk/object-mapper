package objectmapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


//다음에는 M과 W의 구분을 어떻게 할지.

//클라이언트의 코드(Person, Man)가 간단해짐. 이전의 방식은 클라이언트측에서 API를 호출하는 등의 방식으로 재사용
//값을 하나만 쓰면 기본이 value로 들어감.
//value = {ElementType.TYPE, ElementType.FIELD} 이런식으로 배열형태로도 가능
@Target(ElementType.TYPE) 
@Retention(RetentionPolicy.RUNTIME) // 어노테이션에서 나오는 정보가 언제 필요한가의 여부
public @interface Polymorphism {
	//인터페이스랑 거의 비슷한데 @붙임. 다른 어노테이션을 붙이는걸로 extends가능?
	// Polymorphism()이나 Polymorphism 같이 값을 안주면 기본 value = "@class"로 취급
	
	//String temp() default "@class"; 
	String value() default "@class";
	
	//0803 M과 W의 구분을 위해서. 클래스 로더마다 패키지별로 순회를 해서 스캔을 하는 방법도 있긴 함. 느림.
	//빈 배열을 넣은건 처리를 일괄적으로 할 수 있어서 좋다는거
	//실제로 서브클래스가 아닌 클래스를 넣을 수 있는 경우에 대해서는 처리가 불가능한가?
	//스캔을 빠르게 하기 위해서 직접 클래스를 넣어주는 변수를 만들어준것임. 이걸 빼고 위에 말한대로 스캔해도 됨.
	Class<?>[] subclass() default {}; 
	
	//두 번째. @Subtype의 @을 뺀 타입으로
	Subtype[] subtype() default{};
	
}