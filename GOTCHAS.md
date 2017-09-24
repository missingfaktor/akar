# This document lists gotchs for using akar.

* #### Using akar `match` clauses along with `recur`
Since Akar patterns are first class functions, having `recur` along with akar patterns causes error. This is because the recur would no longer be in the tail position for the original function.

To work around this issue, Akar has a macro called `defn-trampolined`.

The macro removes the `trampolined-recur` occurrences in function body and generates function which uses trampoline to achieve recursion.

Please note we replace `trampolined-recur` and not `recur`. This is done intentionally to avoid having any unintended effect of the replacement.


Example usage:
```
(defn-trampolined tail-recursive-sum [x running-total]
      (match x
             0  running-total
             :_ (trampolined-recur (dec x) (+ running-total x))))

```

The above definition generates a function with name `tail-recursive-sum` which internally uses [trampoline](https://clojuredocs.org/clojure.core/trampoline) to achieve recursion.
