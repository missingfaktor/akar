(ns akar-exceptions.core
  (:require [clojure.spec.alpha :as s]
            [akar.primitives :refer [clause-applied?]]
            [akar.syntax :refer [match try-match]]
            [panini.core :refer [define-syntax]])
  (:import [clojure.lang ExceptionInfo]))

(defn attempt* [block on-error ultimately]
  (if on-error
    (try (block)
         (catch Throwable throwable
           (on-error throwable))
         (finally
           (ultimately)))
    (try (block)
         (finally
           (ultimately)))))

(define-syntax attempt
  :grammar (s/cat :block any?
                  :on-error-token #{:on-error}
                  :error-handler any?
                  :ultimately-part (s/? (s/cat :ultimately-token #{:ultimately}
                                               :ultimately-block any?)))
  :target (fn [{:keys [block error-handler ultimately-part]}]
            (let [transformed-error-handler (if (empty? error-handler)
                                              `nil
                                              `(fn [ex#]
                                                 (let [result# (try-match ex# ~@error-handler)]
                                                   (if (clause-applied? result#)
                                                     result#
                                                     (throw ex#)))))]
              `(attempt* (fn []
                           ~block)
                         ~transformed-error-handler
                         (fn []
                           ~(:ultimately-block ultimately-part))))))

(defn raise [throwable-like]
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (match throwable-like
    (:type Throwable)                    (throw throwable-like)
    (:and {:message (:and (:type String)
                          message)}
          the-whole-map)                 (throw (ex-info message the-whole-map))
    {}                                   (throw (ex-info "An error was raised." throwable-like))
    not-even-a-map                       (throw (ex-info "An error was raised." {:object not-even-a-map}))))


(def !ex-info
  (fn [arg]
    (when (instance? ExceptionInfo arg)
      [(.data arg)])))

(defn !ex [cls]
  (fn [arg]
    (when (and (instance? Exception arg)
               (instance? cls arg))
      [arg])))
