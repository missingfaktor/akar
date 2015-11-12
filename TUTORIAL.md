# Akar Tutorial

## Background

### Pattern matching

Pattern matching is a standard feature found in typed functional languages, such as Haskell, Scala, and the ML family. It allows you to decompose data, inspect it for the desired structure or properties, and if affirmative, extract the relevant pieces. Patterns can also be arbitrarily nested, allowing for deep data deconstruction. You could think of pattern matching as `if`/`switch` on steroids.

Let's consider a very simple example from Phil Wadler's [critique of SICP](http://www.cs.kent.ac.uk/people/staff/dat/miranda/wadler87.pdf).

Here's how you sum a list of numbers in [Miranda](https://en.wikipedia.org/wiki/Miranda_programming_language):

```haskell
sum []     = 0
sum (x:xs) = x + sum xs
```

There are two clauses in this definition: one for an empty list (base case), and another for cons. `[]` is the Miranda notation for an empty list, and `(x:xs)` is a notation for a cons list with head `x` and tail `xs`.

Compare this with an equivalent Scheme definition:

```scheme
(define (sum a-list)
  (if (null? a-list)
    0
    (+ (car a-list) (sum (cdr a-list))))) 
```

As the author points out, the Scheme version is less readable than the Miranda one for following reasons:

0. The symmetry between the two cases is obscured. The empty case is tested for explicitly, and the cons case is assumed otherwise.
0. The extraction happens independently of the tests, even though there's a clear dependency of the former on the latter. 

To quote the author further:   

> A good choice of notation can greatly aid learning and thought, and a poor choice can hinder it. In particular, pattern-matching seems to aid thought about **case analysis**, making it easier to construct programs and to prove their properties by **structural induction**.

In their report ["Pattern Matching for an Object-oriented and Dynamically Typed Programming Language"](https://publishup.uni-potsdam.de/files/4204/tbhpi36.pdf), Geller et al also echo a similar sentiment:

> In contrast to regular accessors and conditional statements, it can be argued that deep pattern matching allows concise, readable deconstruction of complex data structures. More specifically, multiple nested conditional expressions quickly become difficult to read, while nested patterns allow destructuring of nested data containers in a single expression.

As it turns out, pattern matching can also be useful without a typing discipline, as illustrated by [Erlang](http://learnyousomeerlang.com/syntax-in-functions)'s example where it's an integral feature of the language. There exist implementations for [Newspeak](http://gbracha.blogspot.de/2010/06/patterns-as-objects-in-newspeak.html), [Common Lisp](https://github.com/m2ym/optima), [Racket](http://docs.racket-lang.org/reference/match.html), and even for [Clojure](https://github.com/clojure/core.match).

### Drawbacks of traditional implementations

In his paper ["First Class Patterns"](www.cs.yale.edu/~tullsen/patterns.ps), Mark Tullsen points out that patterns as typically implemented tend to be very complex. They bring in a lot of other special features, and even a small semantic change requires a large change in the compiler.

Tullsen ascribes most of these deficiencies to patterns not being first class values. In Gilad Bracha's words, they are a [shadow language](http://gbracha.blogspot.de/2014/09/a-domain-of-shadows.html). This hinders our ability to abstract over them, limiting expressivity greatly. More syntactic extensions, such as [view patterns](https://ghc.haskell.org/trac/ghc/wiki/ViewPatterns) and [pattern synonyms](https://ghc.haskell.org/trac/ghc/wiki/PatternSynonyms), are needed to support any abstraction. This complicates the implementation further. See section 1.2 of the paper for some examples.

### First class patterns

Let's put on our "functional goggles" for a bit, and try to see patterns as functions.

A pattern is something that **matches** the data against some structure or properties, and can potentially **extract** some values in case of a match. The following signature captures this contract precisely:
 
```
data -> (extracts | nil)
```

`extracts` is a sequence of extracted values. `nil` would mean that the match failed.

This is the formulation of patterns used by Akar. There's literally nothing more to it!

Treating patterns as regular functions opens up new possibilities. You can abstract over and compose them, like you do with any other functions. It's much simpler do build new features. In fact, this is exactly how various pattern operations, such as guards, at-patterns, alternation, are implemented in Akar. That should serve as a testament to the simplicity and power of this model.

This is not a novel idea, and there is quite a bit of "prior art" out there:

0. [first-class-patterns](http://hackage.haskell.org/package/first-class-patterns), a Haskell library by Brent Yorgey. (Akar is largely based on this library.) 
0. [Newspeak](http://gbracha.blogspot.de/2010/06/patterns-as-objects-in-newspeak.html) [patterns](https://publishup.uni-potsdam.de/files/4204/tbhpi36.pdf).
0. John De Goes' [presentation](http://www.slideshare.net/jdegoes/firstclass-patterns), where he also links to a Javascript fiddle illustrating the ideas.
0. ["First Class Patterns"](www.cs.yale.edu/~tullsen/patterns.ps), a paper by Mark Tullsen. (Previously referred in this article.)
0. [Active patterns](http://fsharpforfunandprofit.com/posts/convenience-active-patterns/) in F#.
0. Scala [extractors](http://lampwww.epfl.ch/~emir/written/MatchingObjectsWithPatterns-TR.pdf). (Kind of, but not quite).

*(This tutorial borrows many explanations and examples from the above mentioned resources.)*

## Akar

Without further ado, let's jump right in! 

Start a Clojure REPL with Akar on path. The easiest way to do so might be to clone this project, and firing `lein repl` from inside the directory. Alternatively, you could use [`lein-try`](https://github.com/rkneufeld/lein-try). 

We will be needing following `use`s:

```clojure
(use 'clojure.repl :reload-all)
(use 'akar.primitives :reload-all)
(use 'akar.patterns :reload-all)
(use 'akar.combinators :reload-all)
(use 'akar.syntax :reload-all)
(use 'n01se.syntax :reload-all)
```

Let's start defining some patterns.

All pattern matching implementations feature a way to **ignore** an argument being matched. The typical syntax is `_`. Consider the following Haskell example:
 
```haskell
case n of
     2 -> True       -- Give True if n == 2
     _ -> False      -- Otherwise give False
```

How will you formulate the "ignore" pattern as a function? Easy! It should always return an empty sequence (i.e. no extracts) regardless of its input. Akar ships with such a function, and it's called `!any`. Look up its source in your REPL.

```clojure
user=> (source !any)
(def !any
  (fn [_]
    []))
nil
```

*(If you are going "WTF! What are those bangs?" at this point, please read [this](stuff).)*

Test it out!

```clojure
user=> (!any 2)
[]

user=> (!any :banana)
[]
```

Sweet!

Let's try and define another common pattern matching feature: **Binding**. In pattern matching, you can bind values to names, that are available in a certain scope. 

This happens in two parts. First, you must define a function that emits its arguments as-is, indiscriminately. Akar defines such a function for you. It's called `!bind`.
 
```clojure
user=> (source !bind)
(def !bind
  (fn [arg]
    [arg]))
nil

user=> (!bind 2)
[2]

user=> (!bind :banana)
[:banana]
```

The other part allows us to consume the emitted values. At this point, we must learn about another Akar concept: **clauses**.

A clause is essentially a function that accepts a pattern and another function. If the former matches, the latter is invoked with the emitted values. `clause*` is a function we use to create a clause.

Let's see this in action.

```clojure
user=> (def c (clause* !any (fn [] :hey)))
#'user/c

user=> (c 2)
:hey

user=> (c :banana)
:hey

user=> (def c2 (clause* !bind (fn [x] [:result x])))
#'user/c2

user=> (c2 2)
[:result 2]

user=> (c2 :banana)
[:result :banana]
``` 

If the number of values emitted by a pattern do not match the number of arguments accepted by the action, there will be an error.

```clojure
user=> (def c3 (clause* !any (fn [x] [:result x])))
#'user/c3

user=> (c3 2)
ArityException Wrong number of args (0) passed to: user/fn--2607  clojure.lang.AFn.throwArity (AFn.java:429)
```



## Putting it all together

## Syntax

Don't feel bad about yourself for caring about syntax. You should totally care.

Syntax matters. UI of a language. 

The direct usage of functions gets unwieldy quickly, and should be avoided. We feature a syntactic layer which makes common use cases convenient but stays true to the first class spirit.
 
"Macro is a compiler." We created this with the seqex library. Better error messages (by Clojure standards) and auto-generated documentation.
  
(syndoc pattern')
(syndoc clause)
(syndoc clauses)
(syndoc match)

As we go, we will introduce various features, both the function versions and syntax versions. Some features only exist in functions land and have no syntactic equivalents.

<< 

The syntax is designed in a way that still stays true to the "first-class"/"value" spirit, at the same time making the common use cases convenient. The translation rules will be simple and easily tractable. 

The syntax is not here to shield users from the underlying model. Users are expected to know the underlying functions in order to be able to use this library effectively.

Uses brilliant library seqex. You can see the grammar with `syndoc`. 

>>

## clause

#### Simple patterns

!any 

Keep invoking `parse-forms`, and `source`.

!bind 

!constant

!some

#### "Arbitrary" patterns

!empty
!cons

Detour: !further 

!further

Explain that with syntactic patterns, some patterns with special syntaxes do it automatically. Arbitrary patterns do them in some cases.

#### Collection patterns

!seq

!further-many

#### Data type patterns

#### "Type" introspection patterns

#### Combinators / special operators

!view !and !or !at !guard !not 

Mention why !or special features and !not cannot make it to syntax.

## A full loaded example

???

### FAQs
Comparison with core.match
Interesting frontiers. Prisms? OTOH, Bondi.

is relevant in clojure? hickey. slingshot. and so on.

Trade offs 
what did we trade? exh. runs against grain.

bangvar