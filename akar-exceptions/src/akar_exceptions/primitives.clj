(ns akar-exceptions.primitives
  (:require [clojure.spec.alpha :as sp]
            [akar.syntax :refer [match]]
            [akar-exceptions.internal.syntax-utilities :refer :all]))

(defn attempt* [block on-error ultimately]
  (try (block)
       (catch Throwable throwable
         (on-error throwable))
       (finally (ultimately))))

(define-syntax attempt
               :parser {:name ::attempt
                        :spec (sp/cat :name '#{attempt}
                                      :block sp/form
                                      :on-error-token '#{:on-error}
                                      :error-handler sp/form
                                      :ultimately-part (sp/? (sp/cat :ultimately-token '#{:ultimately}
                                                                     :ultimately-block sp/form)))}

               :codegen (fn [{:keys [block error-handler ultimately-part]}]
                          `(attempt* (fn [] ~block)
                                     (fn [ex#] (match ex# ~@error-handler))
                                     (fn [] (:ultimately-block ~ultimately-part)))))

(defn raise [exception-like]
  (match exception-like
         (:type Throwable)                    (throw exception-like)
         (:and {:message (:and (:type String)
                               message)}
               the-whole-map)                 (throw (ex-info message the-whole-map))
         {}                                   (throw (ex-info "An error was raised." exception-like))
         not-even-a-map                       (throw (ex-info "An error was raised." {:object not-even-a-map}))))

