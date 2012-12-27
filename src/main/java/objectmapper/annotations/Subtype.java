package objectmapper.annotations;

public @interface Subtype {
	String[] value() default {};
	Class<?> clazz(); //default가 없으면 반드시 써줘야 함

}
