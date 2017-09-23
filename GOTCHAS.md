# Gotchas, pitfalls, and caveats

## Clojure's tail call optimisation does not work with Akar's `match` blocks

Due to the way Akar works, a tail of a function no longer remains a tail after syntax transformation. This should be illustrated by the example below:

```clojure
akar.try-out=> (macroexpand-1 `(match x
          #_=>           0  running-total
          #_=>           :_ (recur (dec x) (+ running-total x))))
          
(akar.primitives/match* akar.try-out/x 
  (akar.primitives/or-else 
    (akar.primitives/clause* (akar.patterns/!constant 0) 
                             (clojure.core/fn [] akar.try-out/running-total)) 
    (akar.primitives/clause* akar.patterns/!any 
                             (clojure.core/fn [] 
                               (recur (clojure.core/dec akar.try-out/x) 
                               (clojure.core/+ akar.try-out/running-total akar.try-out/x))))))
```

This will lead to an error shown below:

```clojure
akar.try-out=> (defn tail-recursive-sum [x running-total]
          #_=>    (match x
          #_=>           0  running-total
          #_=>           :_ (recur (dec x) (+ running-total x))))

CompilerException java.lang.IllegalArgumentException: Mismatched argument count to recur, expected: 0 args, got: 2, compiling:(/private/var/folders/gx/m4wnr9d52fn9t98dwfrpls8h0000gn/T/form-init2545270772814762048.clj:4:14)
```

There is no good way to make tail call optimisation work in this case. At least we could not come up with any.

A workaround commonly employed in this sort of situation is using a technique called [trampolining](https://en.wikipedia.org/wiki/Trampoline_(computing)), which essentially 
swaps stack for heap for our function calls. 

Clojure provides a function named [`trampoline`](https://clojuredocs.org/clojure.core/trampoline) for this purpose. Akar bundles a handy little macro ma,ed `defn-trampolined` to make use of `trampoline` more pleasant. Its interface is designed in a way so as to mirror that of regular `def`-`recur`. 

Example usage:

```clojure
(defn-trampolined tail-recursive-sum [x running-total]
      (match x
             0  running-total
             :_ (trampolined-recur (dec x) (+ running-total x))))
             
(tail-recursive-sum 100000 0)
```

Look up the source! Expand the macro at console to see how it works.
