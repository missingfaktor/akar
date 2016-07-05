# Akar FAQs

### Q. Why do pattern functions begin with a bang?

It helps to have a visual cue in the name of a function, indicating it is a pattern function. [Sigils](https://en.wikipedia.org/wiki/Sigil_(computer_programming)) are widely used in Lisps for this sort of thing: We use `foo?` for predicate functions, `?foo` for metavariables, `**foo**` for dynamic variables, `foo!` for side-effecting functions, `foo*` for auxiliary functions, and so on. `!foo` was untaken, and patterns seem like an idea fundamental enough to deserve their own sigil.

Alternatively, we could have prefixed all our pattern functions with `pat-`, but that would have been too verbose.

### Q. How does Akar compare with `core.match`?

[`core.match`](https://github.com/clojure/core.match), like most traditional implementations, is focused on performance. This is reflected in its tagline "**optimized** pattern matching library". The pattern matcher can be extended using some [extension protocols](https://github.com/clojure/core.match/wiki/Extending-match-for-new-Patterns), but the extensibility is arguably still quite limited.

 Akar was written with different focuses on mind: simplicity and abstraction. It trades off some performance in order to achieve these goals. Akar patterns being first class values can be manipulated and abstracted over more easily and in more ways. This gives us virtually unlimited extensibility.

`core.match` might eventually evolve into a [predicate dispatch](https://github.com/clojure/core.match/wiki/Crazy-Ideas) library. Akar has no such plans. It is a far less ambitious project from that perspective.

### Q. Why did you not use `clojure.spec` to implement the syntactic layer?

A major chunk of this library was written in November 2015. `clojure.spec` was not available at the time.

Migration to `clojure.spec` is on our radar.

### Q. Why did you not implement Akar using optics?

Optics, as implemented in the [Haskell lens library](https://hackage.haskell.org/package/lens), are incredibly general, and can indeed subsume pattern matching. But they are also incredibly complex. Besides, the type-class/constraint based encodings used in the lens library do not translate well to Clojure. Porting the lens library would be significantly more work.

Akar solves a more specific (or, less general) problem, and is much, much simpler.

That said, we might explore porting lenses to Clojure in future.

### Q. How relevant is pattern matching in Clojure?

As relevant as in any other language. :smile:

There is quite a bit of conditional-heavy Clojure code in the wild, that could be simplified using pattern matching. For instance, see the following definition of `zipmap`, and compare it with [the one from the standard library](https://github.com/clojure/clojure/blob/clojure-1.7.0/src/clj/clojure/core.clj#L2940).

```clojure
akar.try-out=> (defn zipmap' [keys vals]
                 (letfn [(aux [map keys vals]
                           (match [keys vals]
                                  (:seq [[!empty] :_]) map
                                  (:seq [:_ [!empty]]) map
                                  (:seq [[!cons k ks] [!cons v vs]]) (aux (assoc map k v) ks vs)))]
                   (aux {} keys vals)))
#'akar.try-out/zipmap'

akar.try-out=> (zipmap' [2 3 9] [:zwei :drei :nein :eins])
{9 :nein, 3 :drei, 2 :zwei}
```

This example was taken from Sean Johnson's ["Pattern Matching in Clojure"](https://www.youtube.com/watch?v=n7aE6k8o_BU) talk. The talk presents many more examples where pattern matching makes code simpler to write, understand, and modify.

Here are some more examples that could highly benefit from pattern matching, and potentially already use some limited form thereof:

0. **Exception handlers:** The `try`-`catch` construct in Clojure features a restricted form of pattern matching that allows you to match by the class of exception thrown, and bind the exception to a local variable. The very popular [slingshot](https://github.com/scgilardi/slingshot) library extends this with [more pattern matching features](https://github.com/scgilardi/slingshot/blob/release/src/slingshot/support.clj#L122), such as predicates, map patterns etc.
0. **`receive` blocks in actors:** Erlang actors make a heavy use of pattern matching, and so does [Pulsar](http://docs.paralleluniverse.co/pulsar/), the actors library for Clojure. They use `core.match` for this purpose.
0. **Route matchers:** One could use patterns to define routes, wherein we could match specific segments against some regular expressions, or any other arbitrary criteria, and extract the relevant inputs.
0. **Mocking in testing:** Mocking typically involves matching function arguments for some criteria, and accordingly giving some output. [Midje](https://github.com/marick/Midje) has a notion of [prerequisites](https://github.com/marick/Midje/wiki/Describing-one-checkable's-prerequisites), which provides some limited form of pattern matching.

### Q. What are Akar's performance characteristics like?

Traditionally, pattern matches are compiled to [highly efficient matching automata](http://pauillac.inria.fr/~maranget/papers/ml05e-maranget.pdf), typically decision trees, realized using low level tests, jump tables, and so on.

Akar does not do any of that. With Akar, it's function composition all the way down. If you are programming in a higher-order functional style pervasively, Akar is unlikely to create any noticeable additional overhead. Also, this is unlikely to be a performance bottleneck for the use cases Clojure is normally employed for.

As always, you should profile your application, and find out if this is causing performance issues.

If some patterns are too expensive, you can write a custom pattern function that does it in a more efficient manner. Here is an example.

```clojure
akar.try-out=> (def valid-header #"HDR (.*)")
#'akar.try-out/valid-header

akar.try-out=> (defn extract-header [rec]
                 (match rec
                        {:tag :record :header [(!regex valid-header) header]} header))
#'akar.try-out/extract-header

akar.try-out=> (extract-header {:tag :record :header "HDR X11"})
"X11"

akar.try-out=> (extract-header {:tag :record :header "HD--X11"})
RuntimeException Pattern match failed. None of the clauses applicable to the value: {:header "HD--X11", :tag :record}.  akar.primitives/match* (primitives.clj:56)

; Let's see how the compiled pattern match looks like.

akar.try-out=> (pprint (macroexpand-1 '(match rec {:tag :record
                                                   :header [(!regex valid-header) header]} header)))
(akar.primitives/match*
 rec
 (akar.primitives/or-else
  (akar.primitives/clause*
   (akar.combinators/!and
    (akar.patterns/!pred clojure.core/map?)
    (akar.combinators/!further
     (akar.patterns/!key :tag)
     [(akar.patterns/!constant :record)])
    (akar.combinators/!further
     (akar.patterns/!key :header)
     [(akar.combinators/!further
       (!regex valid-header)
       [akar.patterns/!bind])]))
   (clojure.core/fn [header] header))))
nil

; Okay, that's quite a bit!
; Assume that you use this pattern a lot, and found that optimizing this away will give you a tangible speedup. You can
; write a custom pattern in such a case like so:

akar.try-out=> (defn !header [rec]
                 (if (= (:tag rec) :record)
                     (if-let [header (:header rec)]
                       (some->> header (re-seq valid-header) first rest))))
#'akar.try-out/!header

akar.try-out=> (defn extract-header [rec]
                 (match rec
                        [!header header] header))
#'akar.try-out/extract-header

akar.try-out=> (extract-header {:tag :record :header "HDR X11"})
"X11"

akar.try-out=> (extract-header {:tag :record :header "HDR--11"})
RuntimeException Pattern match failed. None of the clauses applicable to the value: {:header "HDR--11", :tag :record}.  akar.primitives/match* (primitives.clj:56)
```
