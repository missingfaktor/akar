# Akar Tutorial

## Pattern matching

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

As it turns out, pattern matching can also be useful without a typing discipline, as illustrated by Erlang's example where it's an integral feature of the language. There exist implementations for [Newspeak](http://gbracha.blogspot.de/2010/06/patterns-as-objects-in-newspeak.html), [Common Lisp](https://github.com/m2ym/optima), [Racket](http://docs.racket-lang.org/reference/match.html), and even for [Clojure](https://github.com/clojure/core.match).

## Drawbacks with traditional implementations

syntactic. Not values. Prevent abstraction. Necessitate extensions like pattern synonyms and view patterns.  

they are a shadow language.

## Prior art

Brent Yorgey
Newspeak Felix: Bracha's post, Felix' paper
John A De Goes' presentation, JS lib
patterns.ps paper
Active patterns in F#
Scala extractors

Mention that we use explanation from these resources.

## First class patterns

Magic. Hinders abstraction. 

Akar patterns are plain functions i.e. first class values. Simple contract. Opens up abstraction possibilities. You can manipulate them like any other values. Compose them. In fact, this is exactly how various pattern operations, such as guards, at-patterns, alternation, are implemented in Akar. This simple model makes Akar pattern matching extremely extensible. 

Let's look at these in more depth.

## Akar Concepts

Before we get into it: Fire up a lein repl. Best way is to clone the project, and start a lein repl from inside. 

Imports: clojure repl, akar patterns, akar syntax

### Patterns and clauses

Describe.
nil -> ?
[] -> ?
[..] -> ?

Mention clauses. or-else and so on.

Mention that starry functions are not "special" really. They just stick to macro-fn convention. Can be used directly still. Perfectly fine.
 
## Syntax

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