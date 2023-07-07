package com.xebia.functional.xef.scala.auto;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({
		
				ElementType.FIELD,
						ElementType.METHOD,
						ElementType.PARAMETER,
						ElementType.LOCAL_VARIABLE,
						ElementType.ANNOTATION_TYPE,
						ElementType.CONSTRUCTOR,
						ElementType.PACKAGE,
						ElementType.TYPE,ElementType.TYPE_PARAMETER,
						ElementType.TYPE_USE,
						ElementType.MODULE,
						ElementType.RECORD_COMPONENT})
public @interface TestJavaAnnotation {
	public String name();
}

