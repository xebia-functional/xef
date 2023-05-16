package com.xebia.functional.loom;

import java.util.function.BiFunction;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function10;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function5;
import kotlin.jvm.functions.Function6;
import kotlin.jvm.functions.Function7;
import kotlin.jvm.functions.Function8;
import kotlin.jvm.functions.Function9;

@SuppressWarnings("unused")
public class LoomAdapter {
    private LoomAdapter() {}

    public static <Output> Output apply(Function1<Continuation<? super Output>, Object> transform)
            throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) -> transform.invoke(cont));
    }

    public static <Input, Output> Output apply(
            Input input, BiFunction<Input, Continuation<? super Output>, Object> transform)
            throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) -> transform.apply(input, cont));
    }

    public static <Input, Output> Output apply(
            Input input, Function2<Input, Continuation<? super Output>, Object> transform) throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) -> transform.invoke(input, cont));
    }

    public static <Input1, Input2, Output> Output apply(
            Input1 input1, Input2 input2, Function3<Input1, Input2, Continuation<? super Output>, Object> transform)
            throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) -> transform.invoke(input1, input2, cont));
    }

    public static <Input1, Input2, Input3, Output> Output apply(
            Input1 input1,
            Input2 input2,
            Input3 input3,
            Function4<Input1, Input2, Input3, Continuation<? super Output>, Object> transform)
            throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) -> transform.invoke(input1, input2, input3, cont));
    }

    public static <Input1, Input2, Input3, Input4, Output> Output apply(
            Input1 input1,
            Input2 input2,
            Input3 input3,
            Input4 input4,
            Function5<Input1, Input2, Input3, Input4, Continuation<? super Output>, Object> transform)
            throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) -> transform.invoke(input1, input2, input3, input4, cont));
    }

    public static <Input1, Input2, Input3, Input4, Input5, Output> Output apply(
            Input1 input1,
            Input2 input2,
            Input3 input3,
            Input4 input4,
            Input5 input5,
            Function6<Input1, Input2, Input3, Input4, Input5, Continuation<? super Output>, Object> transform)
            throws InterruptedException {
        return LoomRunner.run(
                (scope, dispatcher, cont) -> transform.invoke(input1, input2, input3, input4, input5, cont));
    }

    public static <Input1, Input2, Input3, Input4, Input5, Input6, Output> Output apply(
            Input1 input1,
            Input2 input2,
            Input3 input3,
            Input4 input4,
            Input5 input5,
            Input6 input6,
            Function7<Input1, Input2, Input3, Input4, Input5, Input6, Continuation<? super Output>, Object> transform)
            throws InterruptedException {
        return LoomRunner.run(
                (scope, dispatcher, cont) -> transform.invoke(input1, input2, input3, input4, input5, input6, cont));
    }

    public static <Input1, Input2, Input3, Input4, Input5, Input6, Input7, Output> Output apply(
            Input1 input1,
            Input2 input2,
            Input3 input3,
            Input4 input4,
            Input5 input5,
            Input6 input6,
            Input7 input7,
            Function8<Input1, Input2, Input3, Input4, Input5, Input6, Input7, Continuation<? super Output>, Object>
                    transform)
            throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) ->
                transform.invoke(input1, input2, input3, input4, input5, input6, input7, cont));
    }

    public static <Input1, Input2, Input3, Input4, Input5, Input6, Input7, Input8, Output> Output apply(
            Input1 input1,
            Input2 input2,
            Input3 input3,
            Input4 input4,
            Input5 input5,
            Input6 input6,
            Input7 input7,
            Input8 input8,
            Function9<
                            Input1,
                            Input2,
                            Input3,
                            Input4,
                            Input5,
                            Input6,
                            Input7,
                            Input8,
                            Continuation<? super Output>,
                            Object>
                    transform)
            throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) ->
                transform.invoke(input1, input2, input3, input4, input5, input6, input7, input8, cont));
    }

    public static <Input1, Input2, Input3, Input4, Input5, Input6, Input7, Input8, Input9, Output> Output apply(
            Input1 input1,
            Input2 input2,
            Input3 input3,
            Input4 input4,
            Input5 input5,
            Input6 input6,
            Input7 input7,
            Input8 input8,
            Input9 input9,
            Function10<
                            Input1,
                            Input2,
                            Input3,
                            Input4,
                            Input5,
                            Input6,
                            Input7,
                            Input8,
                            Input9,
                            Continuation<? super Output>,
                            Object>
                    transform)
            throws InterruptedException {
        return LoomRunner.run((scope, dispatcher, cont) ->
                transform.invoke(input1, input2, input3, input4, input5, input6, input7, input8, input9, cont));
    }
}
