# Migrating from WebFlux to Virtual Threads:
## Enter on Virtual Threads
> Make all entry points enter the application on virtual threads: HTTP endpoints, Message listeners (Rabbit/Kafka), @Schedulers, main (@PostConstruct)
> Set on a ThreadLocal on that thread metadata extracted from Reactor Context
return Mono.deferContextual(context -> {
contextThreadLocal.set(context); // no need to remove ThreadLocal on a Virtual Thread
return methodReturningMono();
}).subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()));

## Call Reactive from Blocking
Mono<A> monoA = reactiveApi.call();
refactor to
A a = reactiveApi.call()
.contextWrite(contextThreadLocal.get()) // restore
.block(); // ok in a Virtual Thead

## Call Blocking from Reactive
Despite entering outside-in on Virtual Threads, we sometimes need to adjust existing reactive code to call blocking code (newly refactored).
a) Refactored flow: web1Blocking()->f2Blocking()->f3Blocking()
b) Legacy flow: web2Reactive()->f3Reactive();
To aboid breakign flow web2, don't CHANGE f3Reactive, but instead create f3Blocking() to do:
f3Blocking() {
return f3Reactive()
.contextWrite(contextThreadLocal.get()) // ***
.block(); // ok in a Virtual Thread
}
*** Raise a warning/error if contextThreadLocal is not set, to avoid missing context metadata.
Old code is moved to the blocking method outside-in - that is, always make sure you ride a virtual thread with threadlocal set

## Preserve Parallelization
Instead of Mono<A,B,C> = zip(monoA,monoB,monoC)

CompletableFuture<A> a = fetchA();
CompletableFuture<B> b = fetchB();
CompletableFuture<C> c = fetchC();
record Result(A a, B b, C c) {}
Result result = CompletableFuture
.allOf(a, b, c)
.thenApply(v -> new Result(a.join(), b.join(), c.join()))
.join();

## Flux to List or Stream
While Mono<A> becomes A, Flux <A> can become either a List<A> or a Stream<A> depending on the use case:
- if you need all elements in memory at once, use List<A>
- if you need to process elements one by one, use Stream<A>  <-- Expected to be exceptional/never

## Use blocking libraries side-by-side with reactive
Move from Redis-reactive to classic redis progressively 
