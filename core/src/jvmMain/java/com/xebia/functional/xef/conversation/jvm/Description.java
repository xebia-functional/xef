package com.xebia.functional.xef.conversation.jvm;

import kotlinx.serialization.SerialInfo;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SerialInfo
public @interface Description {
    String value();
}
