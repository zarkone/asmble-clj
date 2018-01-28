# Usage

```clojure
(use 'amsble-clj.core)

(let [wast-text "(module
             (func (export \"doAdd20\") (param $i i32) (result i32)
               (i32.add (get_local 0) (i32.const 20))))"]
  (-> wast-text
    (wast->module)
    (.doAdd20 22)))

```
