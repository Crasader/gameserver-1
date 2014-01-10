package com.xinqihd.sns.gameserver.proto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtoTest {
	
	//The test order. From 0 to Max
	int order() default 0;
	
	//The test times
	int times() default 1;
}
