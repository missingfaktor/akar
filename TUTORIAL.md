# Akar Tutorial

## Background

### Pattern matching

Pattern matching is a standard feature found in many programming languages, such as Haskell, Scala, Erlang, and the ML family. There also exist implementations for [Newspeak](http://gbracha.blogspot.de/2010/06/patterns-as-objects-in-newspeak.html), [Common Lisp](https://github.com/m2ym/optima), [Racket](http://docs.racket-lang.org/reference/match.html), and [Clojure](https://github.com/clojure/core.match).

Pattern matching allows you to decompose data, inspect it for desired structure or properties, and if affirmative, extract the relevant pieces. Patterns can also be arbitrarily nested, allowing for deep data deconstruction. You could think of pattern matching as `if`/`switch` on steroids.

Let's consider a very simple example from Phil Wadler's [critique of SICP](http://www.cs.kent.ac.uk/people/staff/dat/miranda/wadler87.pdf).

Here's how you sum a list of numbers in [Miranda](https://en.wikipedia.org/wiki/Miranda_programming_language) (or Haskell; same syntax here):

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

### Traditional implementations

Traditionally, pattern matches are compiled to [highly efficient matching automata](http://pauillac.inria.fr/~maranget/papers/ml05e-maranget.pdf), typically decision trees, realized using low level  tests, jump tables, and so on. This efficiency however comes at a cost.

The first section of Mark Tullsen's ["First Class Patterns"](http://citeseerx.ist.psu.edu/viewdoc/download;jsessionid=7D0C1723046D2DCD47099E7B2557381C?doi=10.1.1.37.7006&rep=rep1&type=pdf) paper enumerates a number of drawbacks associated with traditional implementations. Here is a brief summary:

0. **Patterns are too complex.** Pattern matching usually comes with dozens of special purpose features, each of which occupies a place of its own in compilers and language specifications. (Currently ten pages in the Haskell 98 Report are dediated to pattern mathing.) This makes them both harder to implement and use.
0. **Patterns have inelegant semantics.** Patterns impose a left-to-right, top-to-bottom evaluation order. If we want a different evaluation order, we must either do without patterns or write far less elegant looking ode.
0. **You cannot abstract over patterns.** Patterns being a syntactic construct makes it almost impossible to abstract over them. This limits expressivity greatly. Languages end up adding more syntactic extensions, such as [view patterns](https://ghc.haskell.org/trac/ghc/wiki/ViewPatterns) and [pattern synonyms](https://ghc.haskell.org/trac/ghc/wiki/PatternSynonyms) to support any abstraction, complicating the implementation and semantics even further.

Tullsen ascribes these deficiencies to patterns not being first-class values. In Gilad Bracha's words, they are a [shadow language](http://gbracha.blogspot.de/2014/09/a-domain-of-shadows.html).

Akar patterns are first-class values, and alleviate the problems described here. This also means that we do not compile down to efficient decision trees, as is the case with traditional implementations. As stated earlier, Akar focuses on simplicity and abstraction, and as such, trades off some performance for it. [TANSTAAFL](https://en.wikipedia.org/wiki/There_ain%27t_no_such_thing_as_a_free_lunch)! You can read more about performance in the [FAQs](FAQs.md).


### First-class patterns

We will start with some terminology. Look at the diagram below:

![terminology](graphics/terminology.jpg)

This is the same example as before, with a slightly different syntax.

We refer to the structure or property we are matching against as **pattern**.

Each case of pattern match, along with the code to be excuted on a successful match, is referred to as a **clause**.

On some successful pattern matches, we can extract parts of the structure, and bind them to fresh variables scoped under the clause that the pattern is a part of. We refer to these as **extractions**. The term "extract" carries an implication that these values are constituents of the original structure, which is something we cannot guarantee when patterns are arbitrary functions. So we sometimes also use a more unassuming term **emissions** to refer to these values.

Let's now put on our "functional goggles", and try to see these constructs as functions.

![functionalgoggles](graphics/functional.goggles.jpg)

A **pattern** is something that **matches** the given data against some structure or properties, and can potentially **emit** some values in case of a match. The following signature captures this contract precisely:
 
```
data -> (emissions | nil)
```

`emissions` is a sequence of emitted values. 

`nil` would mean that the match failed.

A **clause** is a function that combines a **pattern** and a **function** to be invoked on a successful match.

```
((data -> emissions | nil), (emissions -> result)) -> (data -> result | nil)
```

These are the formulations used by Akar. There's literally nothing more to it! Amazed yet?

Treating patterns as regular functions opens up new possibilities. You can abstract over them and compose them, like you do with any other functions. It's much simpler to build new features too. In fact, this is exactly how various pattern operations, such as guards, at-patterns, alternation etc are implemented in Akar. That should serve as a testament to the simplicity and power of this model.

This is not a novel idea, and there is quite a bit of "prior art" out there:

0. [first-class-patterns](http://hackage.haskell.org/package/first-class-patterns), a Haskell library by Brent Yorgey. (Akar borrows many ideas from this library.) 
0. [Newspeak](http://gbracha.blogspot.de/2010/06/patterns-as-objects-in-newspeak.html) [patterns](https://publishup.uni-potsdam.de/files/4204/tbhpi36.pdf).
0. John De Goes' [presentation](http://www.slideshare.net/jdegoes/firstclass-patterns), where he also links to a Javascript fiddle illustrating the ideas.
0. ["First Class Patterns"](www.cs.yale.edu/~tullsen/patterns.ps), a paper by Mark Tullsen. (Previously referred in this article.)
0. [Active patterns](http://fsharpforfunandprofit.com/posts/convenience-active-patterns/) in F#.
0. Scala [extractors](http://lampwww.epfl.ch/~emir/written/MatchingObjectsWithPatterns-TR.pdf). (Sort of, but not quite).

*(This tutorial borrows many explanations and examples from the above mentioned resources.)*

## Diving in

Without further ado, let's dive right in! 

Start a Clojure REPL with Akar on path. The easiest way to do so might be cloning this project, and firing `lein repl` from inside the directory. Alternatively, you could use [`lein-try`](https://github.com/rkneufeld/lein-try).

You should find yourself in the `akar.try-out` namespace by default. If not, switch to it manually. This namespace brings in all the modules that we will be needing for this tutorial.

This tutorial is structured in a bottom up fashion. We introduce you to the underlying primitives, pattern functions, and combinators first. The discussion on syntax is deferred for later.

### Pattern functions

#### 'Ignore' patterns

Let's start defining some patterns.

All pattern matching implementations feature a way to **ignore** the value being matched. The typical syntax is `_`. Consider the following Haskell example:
 
```haskell
case n of
     2 -> True       -- Give True if n == 2
     _ -> False      -- Otherwise give False
```

How will you formulate the "ignore" pattern as a function? Easy! It should always return an empty sequence (i.e. no emissions) regardless of its input. 

Akar ships with such a function, and it's called `!any`. Inspect its source in your REPL.

```clojure
akar.try-out=> (source !any)
(def !any
  (fn [_]
    []))
nil
```

(If you are going "Hmm, what are those bangs?" at this point, the [FAQs](FAQs.md) might help you out.)

Test it out!

```clojure
akar.try-out=> (!any 2)
[]

akar.try-out=> (!any :banana)
[]
```

Sweet!

#### Bindings

Let's define another common pattern matching feature: **Binding**. In patterns, you can bind the values being matched to fresh variables, which will be available in scope of the clause they are a part of.

This happens in two parts. First, we must define a function that emits its arguments as-is, indiscriminately. Akar defines such a function for you. It's called `!bind`.
 
```clojure
akar.try-out=> (source !bind)
(def !bind
  (fn [arg]
    [arg]))
nil

akar.try-out=> (!bind 2)
[2]

akar.try-out=> (!bind :banana)
[:banana]
```

Thie second part is about consuming the emitted values. This is where the function `clause*` comes in picture.

Let's see this in action.

```clojure
akar.try-out=> (def c (clause* !any (fn [] :hey)))
#'user/c

akar.try-out=> (c 2)
:hey

akar.try-out=> (c :banana)
:hey

akar.try-out=> (def c2 (clause* !bind (fn [x] [:result x])))
#'user/c2

akar.try-out=> (c2 2)
[:result 2]

akar.try-out=> (c2 :banana)
[:result :banana]
``` 

If the number of values emitted by a pattern do not match the number of arguments accepted by the action, there will be an error.

```clojure
akar.try-out=> (def c3 (clause* !any (fn [x] [:result x])))
#'user/c3

akar.try-out=> (c3 2)
ArityException Wrong number of args (0) passed to: user/fn--2607  clojure.lang.AFn.throwArity (AFn.java:429)
```

#### Constant patterns

Moving on, let's define a pattern that matches specifically for value `1`.
 
```clojure
akar.try-out=> (def !one
                 (fn [arg] (if (= arg 1) [])))
#'user/!one

akar.try-out=> (!one 1)
[]

akar.try-out=> (!one 2)
nil
```

That works. But it would be maddening to have to define a new pattern for every new constant we want to test for. How could we generalize this further to work with any **constants**? Easy! Parametrize it.
 
```clojure
akar.try-out=> (defn !cst [x]
                 (fn [arg] (if (= arg x) [])))
#'user/!cst

akar.try-out=> ((!cst :woop) :woop)
[]

akar.try-out=> ((!cst :woop) :not-so-woop)
nil
```

Akar already has this function, and it's called `!constant`.

#### Predicate patterns

We could generalize this even further to accept any **predicate**, and not be restricted to just equality tests. Sure enough, Akar has this covered as well.

```clojure
akar.try-out=> (source !pred)
(defn !pred [pred]
  (fn [x]
    (if (pred x)
      [])))
nil

akar.try-out=> ((!pred odd?) 3)
[]

akar.try-out=> ((!pred odd?) 2)
nil
```

#### Data-type patterns

Jeanine Adkisson gave a talk at ClojureConj about [variants in Clojure](https://www.youtube.com/watch?v=ZQkIWWTygio). This is a very simple representation wherein you use a vector whose first argument is a tag or a label, and the rest are fields. The pattern `!variant` allows you to pattern match on such structures.

```clojure
akar.try-out=> ((!variant :b-node) [:b-node 3 4])
[3 4]

akar.try-out=> ((!variant :b-node) [:z-node 3 4])
nil
```

There is a record counterpart of this pattern, named `!record`.

```clojure
akar.try-out=> (defrecord BNode [lvalue rvalue])
akar.try_out.BNode

akar.try-out=> ((!record BNode) (->BNode 3 4))
[3 4]
```

#### Type-casing patterns

There are cases when it's necessary to type-case on data. There are several ways in Clojure of tagging data with a "type".  The following ways are supported out of the box.

```clojure
akar.try-out=> (def data ^{:type :cid} {:value 9})
#'akar.try-out/data

akar.try-out=> ((!type :cid) data)
[]

akar.try-out=> (def data (java.util.Date.))
#'akar.try-out/data

akar.try-out=> ((!type java.util.Date) data)
[]

akar.try-out=> (def data {:tag :cid :value 9})
#'akar.try-out/data

akar.try-out=> ((!tag :cid) data)
[]
```
 
I hope this gives you an idea of how pattern functions work. There are a few more stock patterns. I suggest you skim through [patterns.clj](src/akar/patterns.clj) before continuing further.

...

Hi, good to see you again!

Now that we have seen a bunch of pattern functions, let's try and put it all together in a pattern matching block.

You can compose clauses with a function named `or-else`. There's a utility function named `clauses*` which allows you define a group of clauses with slightly less noise. 

The function `match*` allows you to match a value against a clause. 
    
Here is a complete example.

```clojure
akar.try-out=> (defn foo [n]
                 (match* n (clauses* (!constant 4) (fn [] :four)
                                     (!pred even?) (fn [] :even)
                                     !bind         (fn [x] x)
                                     !any          (fn [] :we-will-never-get-here))))
#'user/foo

akar.try-out=> (foo 4)
:four

akar.try-out=> (foo 2)
:even

akar.try-out=> (foo 3)
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

In the first clause, not only do we split up the list into its head and tail, but also further require that the head is equal to `2`. In the second clause, we bind the head to a value, but discard the tail. This is nested pattern matching. 
 
Akar has a pattern `!cons` that splits the head and tail of a list.

```clojure
akar.try-out=> (!cons [2 3 4])
[2 (3 4)]
```

How do we further match on head and rest? 

Enter `!further`! 

`!further` is a combinator that takes a root pattern, and a sequence of further patterns, that will be applied with the values emitted by the root pattern, when it matches.

Let's see some examples.

```clojure
akar.try-out=> ((!further !cons [(!constant 2) !bind]) [2 3 4])
((3 4))

akar.try-out=> ((!further !cons [(!constant 2) !bind]) [5 3 4])
nil

akar.try-out=> ((!further !cons [!bind !bind]) [5 3 4])
(5 (3 4))

akar.try-out=> ((!further !cons [!any !any]) [5 3 4])
()
```

As you can see, all the bindings from the nested patterns are being correctly accumulated. 

The Haskell example we saw previously can be written with Akar as follows:

```clojure
akar.try-out=> (defn foo [xs]
                 (match* xs (clauses* (!further !cons [(!constant 2) !any]) (fn [] "starts with 2")
                                      (!further !cons [!bind !any])         (fn [x] (str "starts with " x))
                                      !any                                  (fn [] "empty"))))
#'user/foo

akar.try-out=> (foo [2 3 4])
"starts with 2"

akar.try-out=> (foo [5 3 4])
"starts with 5"

akar.try-out=> (foo [])
"empty"
```

There is a variation of `!further` named `!further-many` that allows a variadic list of arguments to be further matched by other patterns. This is needed to work with patterns like `!seq` and `!regex`. 

Due to variadicity, `!further-many` comes with a support for what's known as rest patterns, i.e. an ability to capture "all the remaining values" and match them further.

Some examples:

```clojure
akar.try-out=> ((!further-many !seq [!bind !bind]) [3 4])
(3 4)

akar.try-out=> ((!further-many !seq [!bind !bind]) [3 4 5 6])
nil

akar.try-out=> ((!further-many !seq [!bind !bind] !bind) [3 4 5 6])
(3 4 [5 6])
```

### Pattern combinators

You can manipulate and combine patterns in a number of ways.
 
#### Negating a pattern

```clojure
akar.try-out=> ((!not (!constant 3)) 3)
nil

akar.try-out=> ((!not (!constant 4)) 3)
[]
```

#### Conjunction of multiple patterns

```clojure
akar.try-out=> ((!and (!key :name) (!key :age)) {:name "quentin" :age 25})
("quentin" 25)

akar.try-out=> ((!and (!key :name) (!key :age)) {:name "quentin"})
nil
```

#### Disjunction/alternation of multiple patterns

```clojure
akar.try-out=> ((!or (!constant 2) (!constant 3)) 2)
[]

akar.try-out=> ((!or (!constant 2) (!constant 3)) 3)
[]

akar.try-out=> ((!or (!constant 2) (!constant 3)) 4)
nil

akar.try-out=> ((!or (!key :kr-number) (!key :tr-number)) {:kr-number "k 11" :tr-number "t 25"})
["k 11"]

akar.try-out=> ((!or (!key :kr-number) (!key :tr-number)) {:tr-number "t 25"})
["t 25"]

```

#### View patterns

This involves applying a function to an argument, and then matching its result against a pattern. This is known in Haskell world as [view patterns](https://ghc.haskell.org/trac/ghc/wiki/ViewPatterns).

```clojure
akar.try-out=> (def five-ish (!view #(Math/abs %) (!constant 5)))
#'user/five-ish

akar.try-out=> (five-ish 5)
[]

akar.try-out=> (five-ish -5)
[]

akar.try-out=> (five-ish 6)
nil
```

#### Pattern guards

Sometimes you may wish to guard existing patterns with additional predicates.

```clojure
akar.try-out=> ((!guard !cons vector?) [2 3])
(2 (3))

akar.try-out=> ((!guard !cons vector?) '(2 3))
nil
```

#### At-patterns / As-patterns

Apart from the emitted values, we may also want to bind the value being matched. These are especially useful when dealing with nested pattern matches.
 
```clojure
akar.try-out=> ((!at (!constant 3)) 3)
(3)

akar.try-out=> ((!at (!constant 3)) 4)
nil
```

Look up the definitions of `!view`, `!guard`, and `!at` in your REPL. 

## Syntax

### Motivation

Let's consider a bigger, more realistic example to motivate this section.
 
```clojure
; We receive events from a remote Zookeeper cluster, and in response, we need to 
; update the local cache. On `:child-added` and `:child-updated` events, we must 
; put the entry in local cache. On `:child-removed` event, we must remove the 
; entry from local cache as well.

akar.try-out=> (def cache (java.util.concurrent.ConcurrentHashMap.))
#'user/cache

akar.try-out=> (defn act-on-event [evt]
                 (match* evt (clauses* (!and (!further (!key :evt-type) [(!or (!constant :child-added)
                                                                              (!constant :child-updated))])
                                             (!key :data)
                                             (!key :path)) (fn [data path] (.put cache path data))
      
                                       (!and (!further (!key :evt-type) [(!constant :child-removed)])
                                             (!key :path)) (fn [path] (.remove cache path))
      
                                       !any (fn [] nil))))
#'user/act-on-event

akar.try-out=> (act-on-event {:evt-type :child-added :data "d" :path "p"})
nil

akar.try-out=> cache
{"p" "d"}

akar.try-out=> (act-on-event {:evt-type :child-updated :data "e" :path "q"})
nil

akar.try-out=> cache
{"p" "d", "q" "e"}

akar.try-out=> (act-on-event {:evt-type :child-updated :data "e" :path "r"})
nil

akar.try-out=> cache
{"p" "d", "q" "e", "r" "e"}

akar.try-out=> (act-on-event {:evt-type :child-removed :path "r"})
"e"

akar.try-out=> cache
{"p" "d", "q" "e"}

akar.try-out=> (act-on-event {:evt-type :child-transmogrified :path "r"})
nil

akar.try-out=> cache
{"p" "d", "q" "e"}
```

Well... That escalated quickly!

As we marvel at the functional elegance of this piece, we can't help but feel that this is a kind of syntax only a mother would love. We could define helper pattern functions, but for such simple logic, it would be nice not to have to do that.

As has been said before by many a great men, [syntax](http://www.eecg.toronto.edu/~jzhu/csc326/readings/iverson.pdf) [matters](http://tomasp.net/academic/papers/computation-zoo/talk-tfp.pdf).

Akar acknowledges this, and features a syntactic layer that makes common use cases convenient, but at the same time, stays true to the first-class spirit of the core model. The translation rules are simple and easily tractable. The purpose of the syntactic layer is not to shield users from the underlying model. Users are expected to know the underlying functions in order to be able to use this library effectively.

### seqex

In ClojureConj 2013, [Jonathan Claggett](https://github.com/jclaggett) and [Chris Houser](https://github.com/Chouser) had a talk called ["Illuminated Projects"](https://www.youtube.com/watch?v=o75g9ZRoLaw), where they presented [seqex](https://github.com/jclaggett/seqex), a project they had been working on. (If you write Clojure (or, are simply enthusiastic about it), I cannot recommend you this talk enough. Queue it up!)  
 
seqex is pure brilliance. It allows you to define new syntax as a set of grammar rules. This makes it much simpler to create new syntax, gives you auto-generated documentation, and produces better error messages (by Clojure standards).
   
The syntax module in Akar was built using seqex.  

Run the following lines in your REPL, and marvel at the output. :smile: 
  
```clojure
(syndoc match)
```

![syndoc](graphics/syndoc.png)

(`syndoc` does not work with Windows consoles. This is [a known issue](https://github.com/jclaggett/seqex/issues/8).)

`match` is a syntax/macro version of the function `match*`. We also have `clause`, `clauses`, and so on.

### akar.syntax

We will go over the important bits of syntax supported by Akar. We will use functions `syndoc`, `parse-forms` (also from seqex) and `macroexpand-1` to study these. You have already seen `syndoc`. The latter two will help us see how various syntactic patterns translate to corresponding functions. 

Syntactic patterns map to corresponding pattern functions, plus name bindings introduced by that pattern. 

(Note that you will never need to touch the individual syntactic pattern rules directly. This is only for educational purposes.)

#### `any'` syntatic patterns
  
```clojure
akar.try-out=> (syndoc any')
  any' => :_ | :any
nil

akar.try-out=> (parse-forms any' '(:_))
{:pattern akar.patterns/!any, :bindings []}

akar.try-out=> (parse-forms any' '(:any))
{:pattern akar.patterns/!any, :bindings []}

akar.try-out=> (parse-forms any' '(:wrong-keyword))
Bad value: :wrong-keyword
Expected any of:
    :_
    :any
nil
```

`any'` syntactic patterns map to `!any` function, and introduce no bindings.

#### `bind'` syntactic patterns

  
```clojure
akar.try-out=> (parse-forms bind' '(x))
{:pattern akar.patterns/!bind, :bindings [x]}
```

As can be seen, the symbol is being introduced as a binding. 

To see how this gets consumed, let's write a full `match` expression.

```clojure
akar.try-out=> (match 3
                      x (inc x))
4
```

It's educational to see what this expands to:

```clojure
akar.try-out=> (pprint (macroexpand-1 '(match 3 x (inc x))))
(akar.primitives/match* 3 (akar.primitives/or-else
                            (akar.primitives/clause* akar.patterns/!bind (clojure.core/fn [x] (inc x)))))
nil
```

#### `literal'` syntactic patterns

The literals are translated to `!constant` patterns. 

```clojure
akar.try-out=> (parse-forms literal' '(9))
{:pattern (akar.patterns/!constant 9), :bindings []}

akar.try-out=> (match 9
                      9 :nine)
:nine

akar.try-out=> (pprint (macroexpand-1 `(match 9 9 :nine)))
(akar.primitives/match* 9 (akar.primitives/or-else
                            (akar.primitives/clause* (akar.patterns/!constant 9) (clojure.core/fn [] :nine))))
nil

```

You can of course use `!constant` pattern for non-literals, but in that case, it needs to be invoked differently. See the section on arbitrary syntactic patterns for more.

#### `guard-pattern'` syntactic patterns

```clojure
akar.try-out=> (match 11
                      (:guard x odd?) x)
11

akar.try-out=> (pprint (macroexpand-1 '(match 11 (:guard x odd?) x)))
(akar.primitives/match* 11 (akar.primitives/or-else 
                             (akar.primitives/clause* (akar.combinators/!guard akar.patterns/!bind odd?) (clojure.core/fn [x] x))))
nil
```

#### `view-pattern'` syntactic patterns

```clojure
akar.try-out=> (match {:name "gazo"}
                      (:view :name "gazo") :yay)
:yay

akar.try-out=> (pprint (macroexpand-1
                 '(match {:name "gazo"}
                         (:view :name "gazo") :yay)))
(akar.primitives/match* {:name "gazo"} (akar.primitives/or-else
                                         (akar.primitives/clause* (akar.combinators/!view :name (akar.patterns/!constant "gazo")) (clojure.core/fn [] :yay))))
nil
```

#### Pattern combinators, syntactically

You can use `:and` and `:or` to combine multiple patterns. (We will stop including macro expansions from this point onwards in this tutorial. You are still encouraged to keep exploring them in your REPL.)

```clojure
akar.try-out=> (match 45
                      (:and x (:or 45 55)) x)
45
```

You may notice here that `:and` when used in conjunction with binding patterns allows you to bind the value being matched. This subsumes the functionality of `!at`, and thus we do not need a separate syntax support for as-patterns/at-patterns.

Let's play some more.

```clojure
akar.try-out=> (match 45
                      (:or x 45) x)

RuntimeException Bindings encountered: [x]
:or syntactic patterns do not support bindings.
Please ignore the bindings using :_  akar.syntax/ensure-no-bindings-for-or (syntax.clj:70)

```

Whoops. The pattern fails with an error. Luckily the error message is self-explanatory.

The reason `:or` does not support bindings is that it's impossible to tell at the time of syntax parsing which names are actually being bound. Underlying `!or` combinator does not suffer from this limitation due to positional nature of emissions.

For similar reasons, the `!not` combinator has no syntactic counterpart either.

#### Collection patterns, syntactically

Sequences and maps are the most important data structures in Clojure, and as such, have a dedicated syntax support in Akar. Some examples below:

```clojure
akar.try-out=> (def data {:tag :i :contents ["w" "s" "d" "j"]})
#'akar.try-out/data

akar.try-out=> (match data
                      {:tag :i :contents (:seq [prim & secs])} {:primary prim
                                                                :secondaries secs})
{:primary "w", :secondaries ["s" "d" "j"]}
```

The Zookeeper example from earlier can now be written in a much neater way:

```clojure
(defn act-on-event [evt]
  (match evt
         {:evt-type (:or :child-added :child-updated) :data data :path path} (.put cache path data)
         {:evt-type :child-removed                               :path path} (.remove cache data)
         :_                                                                   nil))
```

#### Data type patterns, syntactically

```clojure
akar.try-out=> (def data [:node 1 2])
#'akar.try-out/data

akar.try-out=> (match data
                      (:variant :node [1 n]) n)
2

akar.try-out=> (defrecord Node [lvalue rvalue])
akar.try_out.Node

akar.try-out=> (def data (->Node 1 2))
#'akar.try-out/data

akar.try-out=> (match data
                      (:record Node [m 2]) m)
1
```

#### Type-casing pattern, syntactically

```clojure
akar.try-out=> (defn attempt [f handler]
                 (try (f)
                      (catch Exception e (handler e))))
          
#'akar.try-out/attempt

akar.try-out=> (attempt (fn [] (/ 3 0))
                        (fn [e] (match e
                                       (:type ArithmeticException :_) :nan)))
:nan
```

You will notice that `:type` by default introduces a binding for the value being matched. By doing so, we break consistency with other syntactic patterns. This decision was made out of a practical consideration for the most common use cases of this pattern.

#### Arbitary patterns, syntactically

Finally, we need a way to use arbitrary pattern functions in the syntactic layer. A special vector syntax is reserved for this purpose. The first element of vector is the pattern function, and the rest are emissions that can be further matched using syntactic patterns.

The following example should make it clear how this is to be used.

```clojure
akar.try-out=> (defn find [list pred]
                          (match list
                                 [!cons (:guard x pred) :_] x
                                 [!cons :_              xs] (find xs pred)
                                 [!empty]                   nil))
#'akar.try-out/find

akar.try-out=> (find [3 4 5] even?)
4

akar.try-out=> (find [3 4 5] odd?)
3

akar.try-out=> (find [3 4 5] zero?)
nil
```

The first element does not have to be a symbol. It can be any form as long as it results into a pattern function at runtime. The following example should illustrate this:

```clojure
akar.try-out=> (defn parse-alien-language [sentence]
                 (match sentence
                        [(!regex #"(.*) is (.*)") a b] {:tag :definition
                                                        :obj b
                                                        :meaning a}
                        :_                              :syntax-error))
#'akar.try-out/parse-alien-language

akar.try-out=> (parse-alien-language "teyfir is money")
{:tag :definition, :obj "money", :meaning "teyfir"}
```

The ability to use arbitrary pattern functions thusly gives Akar virtually unlimited flexibility. 

## End

Congratulations. You have reached the end of Akar tutorial. If you have any questions, please get in touch at our [gitter channel](https://gitter.im/missingfaktor/akar).

We hope you have great fun using Akar.

Thank you!
