# Akar FAQs

**Q. Why do pattern functions begin with a bang?**

It helps to have a visual cue in the name of a function, that indicates that it is a pattern function. [Sigils](https://en.wikipedia.org/wiki/Sigil_(computer_programming)) are widely used in Lisps for this sort of thing: We use `foo?` for predicate functions, `?foo` for metavariables, `**foo**` for dynamic variables, `foo!` for side-effecting functions, `foo*` for auxiliary functions, and so on. `!foo` was untaken, and patterns seem like an idea fundamental enough to deserve their own sigil.

We could have alternatively prefixed all our pattern functions with `pat-`, but that would have been too verbose.

**Q. How does Akar compare with `core.match`?**

[`core.match`](https://github.com/clojure/core.match), like most traditional implementations, is focused on performance. This is reflected in its tagline "**optimized** pattern matching library". The pattern matcher can be extended using some [extension protocols](https://github.com/clojure/core.match/wiki/Extending-match-for-new-Patterns), but the extensibility is arguably still quite limited.

 Akar was written with different focuses on mind: simplicity and abstraction. It trades off some performance in order to achieve these goals. Akar patterns being first class values can be manipulated and abstracted over more easily and in more ways. This gives us virtually unlimited extensibility.

`core.match` might eventually evolve into a [predicate dispatch](https://github.com/clojure/core.match/wiki/Crazy-Ideas) library. Akar has no such plans. It is a far less ambitious project from that perspective.


**Q. Why did you not implement Akar using optics?**

TODO
Haskell world
Optics
Simplicity
Bondi

**Q. What are Akar's performance characteristics like?**

TODO
Decision trees
HOFs
Profile
Write custom functions

**Q. How relevant is pattern matching in Clojure?**

TODO
Hickey's talk: Complecting, positionality
Slingshot, Pulsar
`if`, `cond` heavy code

**Q. Why did you not use `core.spec` to implement the syntactic layer?**

TODO
November
