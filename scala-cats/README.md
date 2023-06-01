# xef-scala-cats

This module aims to integrate xef-core with [cats-effect](https://typelevel.org/cats-effect/) and IO.

## Interoperability Coroutines & Cats-Effect's IO

The `CoroutineToIO` is a wrapper implementation that originated from a post by Alexandru Nedelcu. 
Therefore, any acknowledgments regarding this work should be directed toward him. The post can be 
found in [his blog post](https://alexn.org/blog/2023/04/24/kotlin-suspended-functions-to-cats-effect-io/).
