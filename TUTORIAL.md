# Akar Tutorial

## Pattern matching

In their report ["Pattern Matching for an Object-oriented and Dynamically Typed Programming Language"](https://publishup.uni-potsdam.de/files/4204/tbhpi36.pdf), Geller et al introduce the concept of pattern matching so (paraphrased):

> Pattern matching facilities were [first developed](http://comjnl.oxfordjournals.org/content/12/1/41.full.pdf) for functional programming languages. Today it is a well established feature and is part of mature languages such as Haskell or members of [the ML family](http://www.amazon.com/exec/obidos/ASIN/0262631326/acmorg-20). The original work on pattern matching describes syntactic extensions to a functional programming language that facilitate the definition of programs by means of structural induction. Supporting such a technique coincides with the mathematical approach of functional programming languages and the aim to promote equational reasoning. 

Pattern matching can be thought of as `if` on steroids. It allows you to decompose the data, inspect it for the desired structure or properties, and if affirmative, extract the relevant pieces. Patterns can be arbitrarily nested, allowing for deep data deconstruction. Quoting Geller et al again:

> In contrast to regular accessors and conditional statements, it can be argued that deep pattern matching allows concise, read-able deconstruction of complex data structures. More specifically, multiple nested conditional expressions quickly become difficult to read, while nested patterns allow destructuring of nested data containers in a single expression.



sicp example: http://www.cs.kent.ac.uk/people/staff/dat/miranda/wadler87.pdf

establish pattern matching is awesome

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
