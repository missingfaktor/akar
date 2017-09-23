(ns akar.util
  (:require [clojure.walk :as cw]))

(defn ^:private postwalk-fn-replace
  "Calls the matching-fn on every expression and if matching-fn
   returned true, replaces the expression by the result of calling
   replacing-fn with the given expr as the parameter"
  [matching-fn replacing-fn form]
  (cw/postwalk (fn [x]
                 (if (matching-fn x)
                   (replacing-fn x)
                   x))
               form))


(defmacro defn-trampolined
  "Given a function body which uses trampolined-recur for
   recursion (instead of recur), this macro generates a function with
   given name.

   The generated function accepts the same arguments which are passed to
   the macro as args parameter. The args parameter is expected to be a vector.

   The macro replaces all the occurrences of trampolined-recur in function
   body with calls to anonymous function.

   The generated function uses trampolining to achieve recursion. In other
   words, we swap stack for heap for recursive calls.
   https://clojuredocs.org/clojure.core/trampoline

   If the given function body does not have any trampoline-recur occurrences,
   the generated function body will be exactly same as passed except
   it will be wrapped in an anonymous function and called with trampoline."
  [fn-name args & body]
  (let [tail-rec-fn-name (symbol (str fn-name "*"))
        fn-body# (postwalk-fn-replace (fn [expr]
                                        (and (seq? expr)
                                             (= (first expr) 'trampolined-recur)))
                                      (fn [expr]
                                        `(fn [] ~(cons tail-rec-fn-name (rest expr))))
                                      (cons 'do body))
        tramp-fn# `(defn ~fn-name ~args (trampoline (fn ~tail-rec-fn-name ~args ~fn-body#) ~@args))]
    tramp-fn#))
