(ns geocoordinates.math
  (:refer-clojure :exclude [abs]))

(def pi
  "Number pi."
  Math/PI)

(defn abs
  "Return the absolute number."
  [x]
  (Math/abs x))

(defn exp
  "Return the base to the exponent power."
  [base exponent]
  (Math/pow base exponent))

(defn sqrt
  "Returns the square root."
  [x]
  (Math/sqrt x))

(defn decimal-degrees->radians
  "Convert decimal degrees to radians."
  [degrees]
  (* degrees (/ pi 180)))

(defn sin
  "Return the sine of an angle."
  [x]
  (Math/sin x))

(defn cos
  "Return the cosine of an angle."
  [x]
  (Math/cos x))

(defn tan
  "Return the tangent of an angle."
  [x]
  (Math/tan x))
