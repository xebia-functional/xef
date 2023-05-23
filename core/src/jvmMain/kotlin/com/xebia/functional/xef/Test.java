package com.xebia.functional.xef;

import kotlin.jvm.internal.IntCompanionObject;
import kotlinx.serialization.KSerializer;
import kotlinx.serialization.builtins.BuiltinSerializersKt;

public class Test {
    public void example() {
        //noinspection KotlinInternalInJava
        KSerializer<Integer> serializer = BuiltinSerializersKt.serializer(
                IntCompanionObject.INSTANCE
        );
    }
}
