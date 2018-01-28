(ns amsble-clj.core
  (:require [clojure.reflect :as r])
  (:import [asmble.cli Translate]
           [asmble.run.jvm ScriptContext ExceptionTranslator]
           [asmble.compile.jvm ClsContext]
           [asmble.ast SExpr]
           [asmble.util Logger]
           [asmble.io StrToSExpr SExprToAst]
           [kotlin.jvm.functions Function1]))


(defn fn->kotlinFn1 [one-arg-fn]
  (proxy [Function1] []
    (invoke [arg]
      (one-arg-fn arg))))


(defn create-script-context [{:keys [package-name modules registrations logger adjust-context class-loader exception-translator default-max-mem-pages]
                              :or {package-name "package"
                                   modules '()
                                   registrations {}
                                   logger (new asmble.util.Logger$Print asmble.util.Logger$Level/DEBUG)
                                   adjust-context (fn->kotlinFn1 identity)
                                   class-loader (new asmble.run.jvm.ScriptContext$SimpleClassLoader
                                                     (.getClassLoader ScriptContext)
                                                     logger)
                                   exception-translator (ExceptionTranslator/Companion)
                                   default-max-mem-pages 1}}]
  (new ScriptContext

       package-name
       modules
       registrations
       logger
       adjust-context
       class-loader
       exception-translator
       default-max-mem-pages))


(defn wast->module [wast-text]
  (let [ctx (create-script-context {:package-name "foovar"})
        sexpr (-> (StrToSExpr/Companion)
                   (.parse wast-text)
                   (.getVals)
                   (asmble.ast.SExpr$Multi.))
        module (->> sexpr
                    (.toScript (SExprToAst/Companion))
                    (.getCommands)
                    (reduce #(.runCommand %1 %2) ctx)
                    (.getModules)
                    first)]
    (.instance module ctx)))


#_(let [wast-text "(module
             (func (export \"doAdd20\") (param $i i32) (result i32)
               (i32.add (get_local 0) (i32.const 20))))"]
  (-> wast-text
    (wast->module)
    (.doAdd20 22)))
