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

### Pattern functions

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

Let's try and define another common pattern matching feature: **Binding**. In pattern matching, you can bind values to names, that will be made available in a certain scope. 

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

Moving on, let's define a pattern that matches specifically for value `1`.
 
```clojure
user=> (def !one
  #_=>   (fn [arg] (if (= arg 1) [])))
#'user/!one

user=> (!one 1)
[]

user=> (!one 2)
nil
```

That works. But it would suck to have to define a new pattern for every new constant we want to test for. How could we generalize this further to work with any **constants**? Easy! Parametrize it.
 
```clojure
user=> (defn !cst [x]
  #_=>   (fn [arg] (if (= arg x) [])))
#'user/!cst

user=> ((!cst :woop) :woop)
[]

user=> ((!cst :woop) :not-so-woop)
nil
```

Akar already has this function, and it's called `!constant`.


We could generalize this even further to accept any **predicate**, and not be restricted to just equality tests. Sure enough, Akar has this as well.

```clojure
user=> (source !pred)
(defn !pred [pred]
  (fn [x]
    (if (pred x)
      [])))
nil

user=> ((!pred odd?) 3)
[]

user=> ((!pred odd?) 2)
nil
```
 
I hope this gives you an idea of how pattern functions work. I suggest you skim through [patterns.clj](src/akar/patterns.clj) before continuing further.

...

Hi, good to see you again!

Now that we have seen a bunch of pattern functions, let's try and put it all together in a pattern matching block.

You can compose clauses with a function named `or-else`. There's a utility function named `clauses*` which allows you define a group of clauses with slightly less noise. 

The function `match*` allows you to match a value against a bunch of clauses. 
    
Here is a full example.

```clojure
user=> (defn foo [n]
  #_=>   (match* n (clauses* (!constant 4) (fn [] :four)
  #_=>                       (!pred even?) (fn [] :even)
  #_=>                       !bind         (fn [x] x)
  #_=>                       !any          (fn [] :we-will-never-get-here))))
#'user/foo

user=> (foo 4)
:four

user=> (foo 2)
:even

user=> (foo 3)
3
```

### Nested patterns 

We mentioned in passing that pattern matching allows for deep deconstruction. Consider the following Haskell example to understand what we mean:
 
```haskell
foo xs = case xs of
              2:_ -> "starts with 2"
              x:_ -> "starts with " ++ show x
              _   -> "empty"
```

In the first clause, not only do we split up the list into its head and tail, but also further match that the head is equal to `2`. In the second clause, we bind the head to a value, but discard the tail. This is nested pattern matching. 
 
Akar has a pattern `!cons` that splits the head and tail of a list.

```clojure
user=> (!cons [2 3 4])
[2 (3 4)]
```

How do we further match on head and rest? 

Enter `!further`! 

`!further` is a combinator that takes a root pattern, and a sequence of further patterns, that will be applied with the values emitted by the root pattern, when it matches.

Let's see some examples.

```clojure
user=> ((!further !cons [(!constant 2) !bind]) [2 3 4])
((3 4))

user=> ((!further !cons [(!constant 2) !bind]) [5 3 4])
nil

user=> ((!further !cons [!bind !bind]) [5 3 4])
(5 (3 4))

user=> ((!further !cons [!any !any]) [5 3 4])
()
```

As you can see, all the bindings from the nested patterns are being correctly accumulated. 

The Haskell example we saw previously can be written with Akar as follows:

```clojure
user=> (defn foo [xs]
  #_=>   (match* xs (clauses* (!further !cons [(!constant 2) !any]) (fn [] "starts with 2")
  #_=>                        (!further !cons [!bind !any])         (fn [x] (str "starts with " x))
  #_=>                        !any                                  (fn [] "empty"))))
#'user/foo

user=> (foo [2 3 4])
"starts with 2"

user=> (foo [5 3 4])
"starts with 5"

user=> (foo [])
"empty"
```

### Pattern combinators

You can manipulate and combine patterns in a number of ways. 
 
- Negating a pattern.

```clojure
user=> ((!not (!constant 3)) 3)
nil

user=> ((!not (!constant 4)) 3)
[]
```

- Conjunction of multiple patterns.

```clojure
user=> ((!and (!key :name) (!key :age)) {:name "quentin" :age 25})
("quentin" 25)

user=> ((!and (!key :name) (!key :age)) {:name "quentin"})
nil
```

- Disjunction of multiple patterns. (Aka, alternation.)

```clojure
user=> ((!or (!constant 2) (!constant 3)) 2)
[]

user=> ((!or (!constant 2) (!constant 3)) 3)
[]

user=> ((!or (!constant 2) (!constant 3)) 4)
nil

user=> ((!or (!key :kr-number) (!key :tr-number)) {:kr-number "k 11" :tr-number "t 25"})
["k 11"]

user=> ((!or (!key :kr-number) (!key :tr-number)) {:tr-number "t 25"})
["t 25"]

```

- Applying a function to an argument, and then matching its result against a pattern. This is known in Haskell world as [view patterns](https://ghc.haskell.org/trac/ghc/wiki/ViewPatterns).

```clojure
user=> (def five-ish (!view #(Math/abs %) (!constant 5)))
#'user/five-ish

user=> (five-ish 5)
[]

user=> (five-ish -5)
[]

user=> (five-ish 6)
nil
```

- Guard a pattern with an additional predicate.

```
user=> ((!guard !cons vector?) [2 3])
(2 (3))

user=> ((!guard !cons vector?) '(2 3))
nil
```

- Apart from the emitted values, also bind the value being matched. These are called at-patterns or as-patterns.
 
```clojure
user=> ((!at (!constant 3)) 3)
(3)

user=> ((!at (!constant 3)) 4)
nil
```

Look up the definitions of `!view`, `!guard`, and `!at` in your REPL. Look how simple they are!

## Syntax

### Motivation

Let's consider a bigger, more realistic example to motivate this section.
 
```clojure
; We receive events from a remote Zookeeper cluster, and in response, we need to 
; update the local cache. On `:child-added` and `:child-updated` events, we must 
; put the entry in local cache. On `:child-removed` event, we must remove the 
; entry from local cache as well.

user=> (def cache (java.util.concurrent.ConcurrentHashMap.))
#'user/cache

user=> (defn act-on-event [evt]
  #_=>   (match* evt (clauses* (!and (!further (!key :evt-type) [(!or (!constant :child-added)
  #_=>                                                                (!constant :child-updated))])
  #_=>                               (!key :data)
  #_=>                               (!key :path)) (fn [data path] (.put cache path data))
  #_=>
  #_=>                         (!and (!further (!key :evt-type) [(!constant :child-removed)])
  #_=>                               (!key :path)) (fn [path] (.remove cache path))
  #_=>
  #_=>                         !any (fn [] nil))))
#'user/act-on-event

user=> (act-on-event {:evt-type :child-added :data "d" :path "p"})
nil

user=> cache
{"p" "d"}

user=> (act-on-event {:evt-type :child-updated :data "e" :path "q"})
nil

user=> cache
{"p" "d", "q" "e"}

user=> (act-on-event {:evt-type :child-updated :data "e" :path "r"})
nil

user=> cache
{"p" "d", "q" "e", "r" "e"}

user=> (act-on-event {:evt-type :child-removed :path "r"})
"e"

user=> cache
{"p" "d", "q" "e"}

user=> (act-on-event {:evt-type :child-transmogrified :path "r"})
nil

user=> cache
{"p" "d", "q" "e"}
```

Well... That escalated quickly!

As we marvel at the functional elegance of this piece, we can't help but notice that this is the kind of syntax only a mother would love. We could define helper pattern functions, but for such a simple logic, it would be nice not to have to do that.

The point I am trying to make is that [notation is a tool of thought](http://www.eecg.toronto.edu/~jzhu/csc326/readings/iverson.pdf), and therefore [syntax matters](http://tomasp.net/academic/papers/computation-zoo/talk-tfp.pdf).  

Akar acknowledges this, and features a syntactic layer that makes common use cases convenient, but at the same time stays true to the first-class spirit of the core model. 

### seqex

In ClojureConj 2013, [Jonathan Claggett](https://github.com/jclaggett) and [Chris Houser](https://github.com/Chouser) had a talk called ["Illuminated Projects"](https://www.youtube.com/watch?v=o75g9ZRoLaw), where they presented [seqex](https://github.com/jclaggett/seqex), a project they had been working on. (If you write Clojure (or, are simply enthusiastic about it), I cannot recommend you this talk enough. Queue it up!)  
 
seqex is pure brilliance. It allows you to define new syntax as a set of grammar rules. This makes it much simpler to create new syntax, gives you auto-generated documentation, and produces better error messages (by Clojure standards).
   
The syntax module in Akar was built using seqex.  

Run the following lines in your REPL, and marvel at the output. :smile: 
  
(syndoc match)
(syndoc pattern')

`match` is a syntax version of the function `match*`. We also have `clause`, `clauses`, and so on.

### akar.syntax

We will go over the important bits of syntax supported by Akar. We will use functions `syndoc`, `parse-forms` (from seqex) and `macroexpand-1` to study these. You have already seen `syndoc`. The latter two will help us see how various syntactic patterns translate to corresponding functions. 
     
Let's begin with `any-pattern'`. 



As we go, we will introduce various features, both the function versions and syntax versions. Some features only exist in functions land and have no syntactic equivalents.

<< 

The syntax is designed in a way that still stays true to the "first-class"/"value" spirit, at the same time making the common use cases convenient. The translation rules will be simple and easily tractable. 

The syntax is not here to shield users from the underlying model. Users are expected to know the underlying functions in order to be able to use this library effectively.

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