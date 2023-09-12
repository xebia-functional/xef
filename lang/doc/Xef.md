# Xef Language

## Xef Language Specification

### Type System

#### Introduction

In XEF, the type system is foundational and unique. At its core, the philosophy of the type system is that types and values are intertwined: every type is a value and every value is a type. This offers a heightened level of precision and flexibility in defining and working with data.

#### Primitive Types

The language includes a set of fundamental, predefined types. These are the building blocks of all other types in the language.

Examples of primitive types:
- **Int**: Represents integer numbers.
- **Float**: Represents floating-point numbers.
- **String**: Represents sequences of characters.
- **Bool**: Represents boolean values, `true` and `false`.

```yaml
# numbers
myInt @ Int : 1
myFloat @ Float : 3.14

# strings
myString @ String : "Hello, world!"

# booleans
myBool @ Bool : true
```

**Note:** In XEF, primitive types are also values, making them "singleton types".

#### Singleton Types

In most languages, primitive values (e.g., 1, true, "hello") are instances of a type (e.g., `Int`, `Bool`, `String`). In XEF, these values are also types in their own right, referred to as singleton types.

For instance, the value `1` is of type `Int`, but it's also a type `1` itself, of which the only instance is `1`.

**Example:**

```yaml
# Using the type and value `1`:
myValue @ 1: 1
```

In the above snippet, `myValue` is declared with the type `1`, and it can only ever be assigned the value `1`.

#### Type Inference

XEF is a statically typed language, but it also supports type inference. This means that the compiler can infer the type of a value based on its context.

```yaml
# The type of `myValue` is inferred to be the singleton type `1` which is also
# an Int and a Nat
myValue : 1
```

#### [], empty values

`[]` represents the neutral element for both scalars and vectors, and it's equivalent to
no value or empty tuple. This empty tuple covers both the empty list and the empty record.
empty has a single inhabitant `[]`.

Types can be denoted as potentially empty by using the `String?` or the `String | []` type.

```yaml
foo[x @ 1?] :
  x + 1
main:
  foo[1] # 2
  foo[] # []
```

A value of `value?` can be of any type `value` or the empty `[]` value and can be unpacked with automatic smart-casting
based on control flow:

```yaml
foo @ 1? : []

bar @ 1 : 
    if foo == [] : 1
    else: foo # foo is smart-casted to 1
```


#### Types as Values

Since every type in XEF is also a value, types can be assigned, passed to functions, and returned as results.

**Example:**

```yaml
# A function that accepts a type and returns a value of that type:
returnType[type @ Type, value @ type] @ value:
  value
```

In this function, `type` acts both as a type and a value, exemplifying the duality. It allows for high granularity when determining what kind of values a function can accept or return.

### Heterogeneous Types

A distinct feature of the XEF language is the confluence of lists, tuples, and function arguments. Rather than treating them as separate constructs, XEF understands all of them as tuples: ordered collections of elements that may each have a unique type. This simplifies the language's model while maximizing its expressiveness.

#### Tuples

At the foundational level, a tuple in XEF is an ordered sequence of values. Each value in the tuple has a distinct type, making it a typed tuple.

**Example:**

```yaml
# A tuple with three elements: an integer, a string, and a double.
myTuple @ [Int, String, Float]: [42, "hello", 3.14]
```

#### Lists

Traditionally, lists are ordered collections where every element has the same type. In XEF, lists are treated as a special case of tuples where every element just happens to have the same type.

**Example:**

```yaml
# A list of integers treated as a tuple.
integerList @ [*Int]: [1, 2, 3]
```

If you let Xef infer the type of the list it will infer the most precise type for the list.

```yaml 
integerList: [1, 2, 3]
main:
  typeOf[integerList] # [1, 2, 3]
```

The same list can be represented as a named record type.

```yaml  
integerList:
  first @ 1: 1
  second @ 2: 2
  third @ 3: 3
```

The same list can be represented as fully inferred record type

```yaml
integerList:
  first: 1
  second: 2
  third: 3
```

or without named fields

```yaml
integerList:
  - 1
  - 2
  - 3 
```

or without a name

```yaml
[1, 2, 3]
```

They all represent the same underliying structural type or a more precise subtype of it: `[Int, Int, Int]` or the structured
labelled type : `[first @ Int, second @ Int, third @ Int]`.

Labelled types are subtypes of unlabelled types and therefore assignable to them.

In this way, traditional lists, function arguments and tuples are unified under a single concept.
They are all multidimensional structures with different types in each dimension.

Users may still choose to type functions in a loose way or for cases where all elements of the tuple have the same type.

```yaml
# A function that accepts a list of integers of unbound size.
addOne[elements @ [*Int]] @ [*Int]:
  elements + 1

foo[unbound @ *Int] @ [*Int]:
  addOne[unbound]

main:
  addOne[1, 2, 3] # [1, 2, 3] -> [2, 3, 4]
  addOne[1, 2, 3, 4] # [1, 2, 3, 4] -> [2, 3, 4, 5]
  foo[1, 2, 3] # [1, 2, 3] -> [2, 3, 4]
  foo[dynamicList] # [*Int] -> [*Int]
```
In the example above we see that despite defining a homogenous type as the original function return if we pass a list of integers of different sizes
the type checker will infer the most precise type for each case at the call site.

In the event that our output is dynamic and there is no precise type for it, we can use the `*` keyword to denote
a more generic type that is not known at the definition site but that will be made specific at the call site based on the input provided.

#### Function Arguments

Function arguments in XEF are treated as tuples. This allows functions to accept multiple inputs without explicitly defining them as separate parameters.
That is a functions arguments is always represented in a heterogeneous tuple of 0 to n elements.

**Example:**

```yaml
# A function that accepts a named tuple consisting of an integer and a string.
id[count @ Int, message @ String] @ [Int, String]:
  [count, message]
```

When calling this function, you would provide a tuple with the appropriate types:

```yaml
id[3, "Hello!"] # [count @ 3, message @ "Hello!"] -> [3, "Hello!"]
```

Types can be partially inferred based on use sites

```yaml
id[count, message]:
  [count, message]
 
main:
  id[3, "Hello!"] # [count @ 3, message @ "Hello!"] -> [3, "Hello!"]
  id["Hello!", 3] # [count @ "Hello!", message @ 3] -> ["Hello!", 3]
```

or rather more generic as the universal identity for all typed values.

```yaml
id[value]: value

main:
    id[3] # [value @ Int] -> Int
    id["Hello!"] # [value @ String] -> String
    id[3, "Hello!"] # [value @ [Int, String]] -> [Int, String]
    id["Hello!", 3] # [value @ [String, Int]] -> [String, Int]

#In practice since these are constants and singleton types passed as arguments
#the types you would get are

main2:
    id[3] # [value @ 3] -> 3
    id["Hello!"] # [value @ "Hello!"] -> "Hello!"
    id[3, "Hello!"] # [value @ [3, "Hello!"]] -> [3, "Hello!"]
    id["Hello!", 3] # [value @ ["Hello!", 3]] -> ["Hello!", 3]
```

A function may be partially applied by providing only some of its arguments.

```yaml
id[count @ Int, message @ String] @ [Int, String]:
  [count, message]

main:
  id[3] # [message @ String] -> [3 @ 3, message @ String]
```
This means that functions may also be applied in groups of arguments.

```yaml
id[count @ Int, message @ String] @ [Int, String]:
  [count, message]
  
main:
  id[3, "Hello!"] # [count @ 3, message @ "Hello!"] -> [3, "Hello!"]
  id[3]["Hello!"] # [count @ 3] -> [message @ "Hello!"] -> [count @ 3, message @ "Hello!"]
```

### Scalar, Vectors, and Multidimensional Values

#### Introduction

One of the core principles of XEF is the uniform treatment of scalar, vector, and multidimensional values. Borrowing concepts from the APL programming paradigm, XEF provides a rich set of operators that can operate seamlessly across these varied data constructs. This uniformity allows for elegant and concise expressions, especially in transformation chains.

#### Scalar, Vector, and Multidimensional Values

In XEF, a scalar is a singular value, a vector is a one-dimensional list of values, and a multidimensional value is a nested structure, typically visualized as matrices or higher-dimensional arrays which may have labelled values.
Since these arrays may have labelled dimensions, they can be also be represented as records.

**Examples:**

```yaml
# Scalar value
age @ Int: 25

# Vector value
names @ [*String]: ["Alice", "Bob", "Charlie"]

# Multidimensional value (2x3 matrix)
matrix @ [[Int, Int, Int], [Int, Int, Int]]: [[1, 2, 3], [4, 5, 6]]

# Record
record @ [[Int, Int, Int], [Int, Int, Int]]:
  row1:
    first: 1
    second: 2
    third: 3
  row2:
    first: 4
    second: 5
    third: 6
    
# Record with inferred types
record:
  row1:
    first: 1
    second: 2
    third: 3
  row2:
    first: 4
    second: 5
    third: 6
    
# inferred labelled type is: 
#  [
#    [first @ 1, second @ 2, third @ 3], 
#    [first @ 4, second @ 5, third @ 6]
#  ]

# Records that use the same tupled syntax
matrix: [
  [first @ 1, second @ 2, third @ 3],
  [first @ 4, second @ 5, third @ 6]
]
```

The type for `matrix` and `record` is the same in all cases: `[[Int, Int, Int], [Int, Int, Int]]` or a subtype of it which includes labelled types and values

#### Operators and Transformation Chains

Operators such as `+`, `-`, `*`, and `/`, traditionally associated with scalar operations, can be applied across scalars, vectors, and matrices. This allows for operations to be performed element-wise across structures without special syntax.

**Example:**

```yaml
# Adding a scalar to a vector
result1 @ [Int]: names + " Jr." # Results in ["Alice Jr.", "Bob Jr.", "Charlie Jr."]

# Element-wise addition of two matrices
matrix1 @ [[Int, Int, Int], [Int, Int, Int]]: [[1, 2, 3], [4, 5, 6]]
matrix2 @ [[Int, Int, Int], [Int, Int, Int]]: [[1, 1, 1], [1, 1, 1]]
result2 @ [[Int, Int, Int], [Int, Int, Int]]: matrix + matrix2 # Results in [[2, 3, 4], [5, 6, 7]]
```

You can define your own operators. In the following example we will redefine the `+` operator
as plus and we will use it to add different data types

```yaml
plus : +
([left] plus [right]) @ left + right :
    left + right

main:
  1 plus 1 # 2
  1 plus 1.0 # 2.0
  1 plus "1" # "11"
  1 plus [1, 2, 3] # [2, 3, 4]
```

In some use cases you may want to define your own operator that does not depend on the original operators provided
by Xef. In the following example we will define a function that works across scalars, vectors, and matrices of
any dimensions.

```yaml
neutral[element] :
    when element:
        [] : []
        Int: 0
        String: ""
        else : element

main:
  neutral[1] # 0
  neutral["Hello"] # ""
  neutral[[]] # []
  neutral[[1, 2, 3]] # [0, 0, 0]
  neutral[[[1, 2, 3], [4, 5, 6]]] # [[0, 0, 0], [0, 0, 0]]
  neutral[[[1, "a", 1.0], [2, "b", 2.0]]] # [[0, "", 0.0], [0, "", 0.0]]
```

Pattern matching can be simplied to not require the `when` keyword when the left hand side of a statement is a type
and there are as many statements as cases.

```yaml
neutral[element] :
    [] : []
    Int: 0
    String: ""
    else : element
```

Since records, tuples, lists and in general data structures in xef are all variants of a typed tuple,
operators apply to all structures that partially include the type.

```yaml
# Adding two scalars
result1 @ 2: 1 + 1

# Adding a scalar to a vector
result2 @ [2, 3, 4]: [1, 2, 3] + 1

# Adding a scalar to a matrix
result3 @ [[2, 3, 4], [5, 6, 7]]: [[1, 2, 3], [4, 5, 6]] + 1

# Adding a scalar to a record
result4 @ [first @ 2, second @ 3, third @ 4]: [first @ 1, second @ 2, third @ 3] + 1

# Adding a scalar to a record with heterogeneous fields
result5 @ [first @ "Hello1", second @ 1, third @ 2.0]: [first @ "Hello", second @ 0, third @ 1.0] + 1

# Creating the superset of two records
person1:
  name @ "Alice": "Alice"
  age @ 25: 25
  location @ "London": "London"

person2:
  name @ "Bob": "Bob"
  age @ 30: 30
  # location is not defined

combinedPerson @ [name @ "AliceBob", age @ 55, location @ "London"]: person1 + person2

# combining only on certain fields by type, label or a combination of both
combinedPerson2 @ [name @ "Alice", age @ 55, location @ "London"]: person1 + person2's age
combinedPerson3 @ [name @ "AliceBob", age @ 55, location @ "London"]: person1 + person2's [age, name]

# removing a field from a record
person3 @ [name @ "Alice", age @ 25]: person1 - person1's location
```

Multidimensional operators in Xef support structures with different types as long as the operator
is defined for each one of the types that conform the product data structure.

**Example:**

```yaml
m1 : [[1, "a", 1.0], [2, "b", 2.0]]
m2 : [[1, "a", 1.0], [2, "b", 2.0]]
result : m1 + m2
# This results in value and type [[2, "aa", 2.0], [4, "bb", 4.0]]
```

#### Benefits:

1. **Uniformity:** The consistent treatment across scalars, vectors, and matrices simplifies mental models and reduces cognitive overhead.
2. **Expressiveness:** Chaining operations allows for compact representations of complex transformations.
3. **Flexibility:** The seamless operation across different data structures aids in tasks that involve varied data sizes and dimensions, from simple scalar operations to extensive matrix computations.

### Products and Unions

```yaml
portfolio:
 personal : "Personal portfolio"
 business : "Business portfolio"
 charity : "Charity portfolio"
 other : "Other portfolio"
 
# typed as portfolio @ [personal @ "Personal portfolio", business @ "Business portfolio", charity @ "Charity portfolio"]
 
# use as a regular product data type:
showPortfolios[portfolio @ portfolio] @ String:
    portfolio separatedBy ", "
    
# returns "personal, business, charity"    

# use as an algebraic coproduct data type with pattern matching with the `when` keyword:

evaluatePortfolio[element @ portfolio] @ String:
    when element:
        personal: "A personal portfolio"
        business: "A business portfolio"
        charity: "A charity portfolio"
        else: "An unknown portfolio"

main:
    evaluatePortfolio[portfolio's personal] # "A personal portfolio"
    evaluatePortfolio[portfolio's business] # "A business portfolio"
    evaluatePortfolio[portfolio's charity] # "A charity portfolio"
    evaluatePortfolio[portfolio's other] # "An unknown portfolio"
```

Data structures that are algebraic may have nested property constructors.
Constructors support default values

```yaml
portfolio:
  personal : "Personal portfolio"
  business : "Business portfolio"
  charity : "Charity portfolio"
  other:
    name : "Other portfolio"
    value : 1000
```

With these structures we can perform matches on the individual properties of the data structure.
In the following example we only evaluate the `other` branch if the portfolio `value` is 
of a certain amount.

```yaml
evaluatePortfolio[portfolio's element] @ String:
    when element:
        # no need to prefix with portfolio's since we are already in the portfolio context
        personal: "A personal portfolio"
        business: "A business portfolio"
        charity: "A charity portfolio"
        other:
            when element.value:
                1000: "A small other portfolio"
                10000: "A medium other portfolio"
                100000: "A large other portfolio"
                else: "An unknown other portfolio"
        else: "An unknown portfolio"
```

Non-exhaustive matches are not allowed and will result in a compile time error.

```yaml  
evaluatePortfolio[case @ portfolio's] @ String:
    when case:
        personal: "A personal portfolio"
        business: "A business portfolio"
        charity: "A charity portfolio"

# compile time error: `other` is not matched in `portfolio`
```

### Functions

#### Functions return types and errors

Functions may have multiple return values of different types which automatically type check to a union of possible exits
with biased in the order in which the exits take place following statement order.

```yaml
foo @ Int | String: 1
foo2 @ Int | String: "Hello"

bar @ Int:
    when foo:
        Int: foo
        String: 0
```

#### Functions are transparent 

Function's return types can be inferred from the functions body statements.

```yaml
foo @ Int | String: 1
foo2 @ Int | String: "Hello"

bar[input]:
    when input:
        Int: input
        else : "hello!"

main:
    bar[foo] # 1 
    bar[foo2] # "hello!"
    typeOf[bar[foo]] # Int | String
    typeOf[bar[foo2]] # Int | String
    typeOf[bar] # input @ (Int | String) -> Int | String
    typeOf[bar[1]] # 1
    typeOf[bar["hello"]] # "hello!"
```        

#### Recursion

Mutually recursive functions are allowed and stack safe

```yaml
even[n @ Int] @ Bool:
    when n:
        0: true
        else: odd[n - 1]

odd[n @ Int] @ Bool:
    when n:
        0: false
        else: even[n - 1]        
  
main:      
  even[1000000] # true
  odd[1000000] # false        
```

#### Refined types

```yaml
even[n @ n > 0]:
    when n:
        0: true
        else: odd[n - 1]

odd[n @ n > 0]:
    when n:
        0: false
        else: even[n - 1]

main:
    even[1000000] # true
    odd[1000000] # false
    typeOf[even] # n @ (n > 0) -> Boolean | odd[n - 1]
    typeOf[odd] # n @ (n > 0) -> Boolean | even[n - 1]
    typeOf[even[1]] # false
    typeOf[odd[1]] # true
```

#### Higher order functions

Higher order functions are functions that take functions as arguments or return functions as results.

```yaml
map[f @ f @ Int -> Int, elements @ [*Int]] @ [*Int]:
    when elements:
        []: []
        else: [f[elements.first]] + map[f, elements.rest]
```

We can applu higher order functions over heterogeneous arguments

```yaml
map[f @ f @ Int -> _, elements @ [Int, String, Double]]]:
    when elements' value:
      Int: f[value]
      String : value
      Double : f[value's intValue]

main:
    map[even, [1, 2, 3]] # [false, true, false]
    map[even, [1, "hello", 3.0]] # [false, "hello", false]
```

`_` in `f @ f @ Int -> _` is a type variable that represents a type that is not known at the definition site but that will be made specific at the call site based on the input provided.
`_` is the universal type for all types and represents a type hole the compiler needs to fill in at the call site.

### LLM integration

Xef is  language designed from the ground up to integrate with LLMs.
Both it's compiler and runtime have the ability to interface with an LLM and enrich programs
with inference, fine-tuning and in general optimizations and use cases that are common when building applications that
interface with a large language model.

#### Compile time typed holes

While compiling a xef program the user may specify type wholes that will be filled in by the compiler
and the LLM as suggestions on how to continue developing the program.

##### LLMs type holes

```yaml

# The user may specify a type hole with the `_` keyword

foo @ _ : 1
```
This program will compile because the user wants to fill the type missing in the function `foo` with the value `1`.
In this case the compiler does not need to call the LLM to fill in the type hole because it can infer the type of the function
and continue with its compilation.

There are cases where the compiler does not have enough information to infer hole.
Consider the following program:

```yaml
foo :
  name @ _ : 
  age @ _ : 1
```

In this case the compiler can guess the type of `age` as it did before, but it would not be possible to guess the type of `name` without
If we attempt to compile this program we get the following error:

```yaml
Errors: 
  0 : 
    message: "Type hole not filled for `name @ _`"
    line: 2
    column: 3
    fix: 
      foo :
        name @ String : 
        age @ Int : 1
```

A compiler doesn't know `String` was a good type for just a placeholder label like `name`
but the LLM can look at the entire structure, infer you are trying to create the name for someone
and suggest `String` as a good type for it.

Type holes can be used in pretty much any position

```yaml
foo:
    _ : "Alice"
    _ : 25
    _ : "London"
    _ : _
```

In this case the compiler can infer the type of the first three type holes but it cannot infer their names,
It can't also infer any information about the last one property.
The LLM can infer a name for each value and a suggested property for the last one based on the context of the program.

```yaml
Errors: 
  0 : 
    message: "Type hole not filled for `_ : "Alice"`"
    line: 2
    column: 5
    fix: 
      name @ String : "Alice"
  1 : 
    message: "Type hole not filled for `_ : 25`"
    line: 3
    column: 5
    fix: 
      age @ Int : 25
  2 :
    message: "Type hole not filled for `_ : "London"`"
    line: 4
    column: 5
    fix: 
      location @ String : "London"
  3 :
    message: "Type hole not filled for `_ : _`"
    line: 5
    column: 5
    fix: 
      hobby @ String : "Traveling"
```

Since functions receive the same treatments type holes can be used in function arguments and body wherever
required:

```yaml
fib[n @ _] @ _: _
```
When attempting to compile this function compilation will fail and a suggestion like the one below
will be provided:

```yaml
Errors: 
  0 : 
    message: "Type hole not filled for `n @ _`"
    line: 1
    column: 4
    fix: 
      n @ Nat
  1 : 
    message: "Type hole not filled for `_ : _`"
    line: 1
    column: 11
    fix: 
      fib[n @ Nat] @ Nat: _
  2 : 
    message: "Type hole not filled for `_ : _`"
    line: 1
    column: 23
    fix: 
      fib[n @ Nat] @ Nat: 
        when n:
          0: 0
          1: 1
          else: fib[n - 1] + fib[n - 2]
```

The Xef compiler always returns a structured error that includes a fix with a compatible 
typed expression for the type hole.

#### Conversations

A Xef program can interface directly with an LLM and use it to infer values at runtime.
While performing inference the Xef runtime wraps AI programs in conversations with access to
the context of the conversation and the ability to perform actions.

Actions inside a conversation usually include:
- Adding a message prior to inference
- Search in the vector store
- interleave function and runtime control flow with inference calls.

##### Messages and Roles

The Xef language contains primitives to define messages and roles that are appended to a conversation
prior to a call to inference. consider the following available functions:

```yaml
Role :
  System
  Assistant
  User
  Function

Message:
  role @ Role :
  content @ String :
  functions @ [*] : []

system @ Message's :
  role : system

assistant @ Message's :
  role : assistant

user @ Message's :
  role : user

function @ Message's :
  role : function
  functions: [*] :

llm:infer :
  messages: [Message*] 
```

Provided these functions we can define conversations with an AI where the AI has access
to the messages and memory previous to inference calls

```yaml
Personality:
  Evil:
  Good:
  Neutral:

Character:
  name @ String:
  age @ Int:
  location @ String:
  skills @ [*String]:
  hobbies @ [*String]:
  personality @ Personality:

create character @ Character:
  config @ *:
  categories @ [String*]:
  infer: [
      system: "Roleplay as an expert roleplay character creator",
      assistant: "Hi, I'm an expert roleplay character creator",
      user: "Hello!, I'm looking for a sci-fi pirate that is a good cook",
      assistant: "I can help you with that",
      function:
        message: config
        functions: [ Character ]
    ]

alice:
  create character:
    name: "Alice"
    personality: Personality's good

# The AI may infer the following character
# Character:
#  name @ "Alice":
#  age @ 25:
#  location @ "London":
#  skills @ ["Cooking", "Sailing", "Fighting"]:
#  hobbies @ ["Traveling", "Reading"]:
#  personality @ Personality's good

```
