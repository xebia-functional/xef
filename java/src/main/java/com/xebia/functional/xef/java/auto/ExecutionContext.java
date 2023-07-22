package com.xebia.functional.xef.java.auto;

import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings;
import com.xebia.functional.xef.embeddings.Embeddings;
import com.xebia.functional.xef.vectorstores.LocalVectorStore;
import com.xebia.functional.xef.vectorstores.VectorStore;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.*;
import kotlinx.coroutines.future.FutureKt;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionContext implements AutoCloseable {

    private final ExecutorService executorService;
    private final CoroutineScope coroutineScope;
    private final CoreAIScope scope;

    public ExecutionContext(){
        this(Executors.newCachedThreadPool(new ExecutionContext.AIScopeThreadFactory()),  new OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING));
    }

    public ExecutionContext(ExecutorService executorService, Embeddings embeddings) {
        this.executorService = executorService;
        this.coroutineScope = () -> ExecutorsKt.from(executorService).plus(JobKt.Job(null));
        VectorStore vectorStore = new LocalVectorStore(embeddings);
        this.scope = new CoreAIScope(embeddings, vectorStore);
    }

    protected <A> CompletableFuture<A> future(Function1<? super Continuation<? super A>, ? extends Object> block) {
        return FutureKt.future(
                coroutineScope,
                coroutineScope.getCoroutineContext(),
                CoroutineStart.DEFAULT,
                (coroutineScope, continuation) -> block.invoke(continuation)
        );
    }

    @Override
    public void close() {
        CoroutineScopeKt.cancel(coroutineScope, null);
        executorService.shutdown();
    }

    public CoreAIScope getCoreScope() {
        return scope;
    }

    private static class AIScopeThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r);
            t.setName("xef-ai-scope-worker-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
