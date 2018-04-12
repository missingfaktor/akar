(ns akar-exceptions.core
  (:require [clojure.spec.alpha :as sp]
            [akar.primitives :refer [clause-applied?]]
            [akar.syntax :refer [match try-match]]
            [akar-commons.syntax-utilities :refer :all])
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
               :parser {:name ::attempt
                        :spec (sp/cat :name '#{attempt}
                                      :block sp/form
                                      :on-error-token '#{:on-error}
                                      :error-handler sp/form
                                      :ultimately-part (sp/? (sp/cat :ultimately-token '#{:ultimately}
                                                                     :ultimately-block sp/form)))}

               :codegen (fn [{:keys [block error-handler ultimately-part]}]
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

(defn raise [exception-like]
  (match exception-like
         (:type Throwable)                    (throw exception-like)
         (:and {:message (:and (:type String)
                               message)}
               the-whole-map)                 (throw (ex-info message the-whole-map))
         {}                                   (throw (ex-info "An error was raised." exception-like))
         not-even-a-map                       (throw (ex-info "An error was raised." {:object not-even-a-map}))))


(def !ex-info
  (fn [arg]
    (if (instance? ExceptionInfo arg)
      [(.data arg)])))

(defn !ex [cls]
  (fn [arg]
    (if (and (instance? Exception arg)
             (instance? cls arg))
      [arg])))
