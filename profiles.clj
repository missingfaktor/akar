{:user {:plugins      [[jonase/eastwood "0.2.1"]]
        :dependencies [[clj-stacktrace "0.2.8"]]
        :injections   [(let [orig (ns-resolve (doto 'clojure.stacktrace require)
                                    'print-cause-trace)
                             new (ns-resolve (doto 'clj-stacktrace.repl require)
                                   'pst)]
                         (alter-var-root orig (constantly (deref new))))]}}
