(ns geocoordinates.core
  (:require [geocoordinates.math :as math]))

(def airy-1830-ellipsoid-constants
  "Airy 1830 biaxial ellipsoid shape and size (in metres)."
  {:semi-major-axis-a 6377563.396
   :semi-minor-axis-b 6356256.909})

(def national-grid-transverse-mercator-projection-constants
  "National Grid Transverse Mercator projection constants
  (latitude and longitude in decimal degrees, easting and northing in metres)."
  {:scale-factor-on-central-meridian-f0 0.9996012717
   :true-origin-latitude-φ0 49
   :true-origin-longitude-λ0 -2
   :true-origin-easting-e0 400000
   :true-origin-northing-n0 -100000})

(def national-grid-constants
  "Ellipsoid and projection constants for the National Grid projection."
  {:ellipsoid-constants airy-1830-ellipsoid-constants
   :transverse-mercator-projection-constants national-grid-transverse-mercator-projection-constants})

(defn- meridional-arc
  "Compute the meridional arc."
  [bf0 n true-origin-latitude-φ0 initial-or-final-latitude-φ]
  (let [t1 (* (+ 1 n (* (/ 5 4) (math/exp n 2)) (* (/ 5 4) (math/exp n 3))) 
              (- initial-or-final-latitude-φ true-origin-latitude-φ0))
        t2 (*  (+ (* 3 n) (* 3 (math/exp n 2)) (* (/ 21 8) (math/exp n 3)))
               (math/sin (- initial-or-final-latitude-φ true-origin-latitude-φ0))
               (math/cos (+ initial-or-final-latitude-φ true-origin-latitude-φ0)))
        t3 (* (+ (* (/ 15 8) (math/exp n 2)) (* (/ 15 8) (math/exp n 3)))
              (math/sin (* 2 (- initial-or-final-latitude-φ true-origin-latitude-φ0)))
              (math/cos (* 2 (+ initial-or-final-latitude-φ true-origin-latitude-φ0))))
        t4 (* (* (/ 35 24) (math/exp n 3))
              (math/sin (* 3 (- initial-or-final-latitude-φ true-origin-latitude-φ0)))
              (math/cos (* 3 (+ initial-or-final-latitude-φ true-origin-latitude-φ0))))]

    (* bf0 (- (+ (- t1 t2) t3) t4))))

(defn- initial-latitude
  "Compute the initial value for latitude in radians."
  [northing true-origin-northing-n0 af0 true-origin-latitude-φ0 n bf0]
  (let [calculate-arc (fn [φ1-val]
                        (meridional-arc bf0 n true-origin-latitude-φ0 φ1-val))
        calculate-φ (fn [arc-val φ1-val] 
                      (+ (/ (- northing true-origin-northing-n0 arc-val) af0) φ1-val))
        initial-φ (+ (/ (- northing true-origin-northing-n0) af0) true-origin-latitude-φ0)
        initial-arc (calculate-arc initial-φ)]
    
    (loop [φ initial-φ
           arc initial-arc]
      (if (> (math/abs (- northing true-origin-northing-n0 arc)) (math/exp 10 -5))
        (let [new-φ (calculate-φ arc φ)
              new-arc (meridional-arc bf0 n true-origin-latitude-φ0 new-φ)]
          (recur new-φ new-arc))
        (calculate-φ arc φ)))))

(defn- compute-parameters
  "Compute the parameters used in the conversion."
  [easting northing constants]
  (let [af0 (* (:semi-major-axis-a (:ellipsoid-constants constants))
               (:scale-factor-on-central-meridian-f0 (:transverse-mercator-projection-constants constants)))
        bf0 (* (:semi-minor-axis-b (:ellipsoid-constants constants))
               (:scale-factor-on-central-meridian-f0 (:transverse-mercator-projection-constants constants)))
        e2 (/ (- (math/exp af0 2) (math/exp bf0 2)) (math/exp af0 2))
        n (/ (- af0 bf0) (+ af0 bf0))
        et (- easting (:true-origin-easting-e0 (:transverse-mercator-projection-constants constants)))
        φd (initial-latitude northing
                             (:true-origin-northing-n0 (:transverse-mercator-projection-constants constants)) 
                             af0
                             (math/decimal-degrees->radians (:true-origin-latitude-φ0
                                                             (:transverse-mercator-projection-constants
                                                              constants)))
                             n
                             bf0)
        ν (/ af0 
             (math/sqrt (- 1 (* e2 (math/exp (math/sin φd) 2)))))
        ρ (/ (* ν (- 1 e2))
             (- 1 (* e2 (math/exp (math/sin φd) 2))))
        η2 (- (/ ν ρ) 1)]
    
    {:af0 af0 :bf0 bf0 :e2 e2 :n n :et et :φd φd :ν ν :ρ ρ :η2 η2}))

(defn- easting-northing->latitude
  "Un-project Transverse Mercator easting and northing back to latitude."
  [easting northing constants]
  (let [{af0 :af0 bf0 :bf0 e2 :e2 n :n et :et φd :φd ν :ν ρ :ρ η2 :η2}
        (compute-parameters easting northing constants)
        VII (/ (math/tan φd) (* 2 ρ ν))
        VIII (* (/ (math/tan φd) (* 24 ρ (math/exp ν 3)))
                (+ 5 (* 3 (math/exp (math/tan φd) 2)) (- η2 (* 9 η2 (math/exp (math/tan φd) 2)))))
        IX (* (/ (math/tan φd) (* 720 ρ (math/exp ν 5)))
              (+ 61 (* 90 (math/exp (math/tan φd) 2)) (* 45 (math/exp (math/tan φd) 4))))]
    
    (* (/ 180 math/pi)
       (+ (- φd (* (math/exp et 2) VII)) (- (* (math/exp et 4) VIII) (* (math/exp et 6) IX))))))

(defn- easting-northing->longitude
  "Un-project Transverse Mercator easting and northing back to longitude."
  [easting northing constants]
  (let [{af0 :af0 bf0 :bf0 e2 :e2 n :n et :et φd :φd ν :ν ρ :ρ η2 :η2}
        (compute-parameters easting northing constants)
        λ0 (math/decimal-degrees->radians (:true-origin-longitude-λ0
                                           (:transverse-mercator-projection-constants
                                            constants)))
        X (/ (math/exp (math/cos φd) -1) ν)
        XI (* (/ (math/exp (math/cos φd) -1) (* 6 (math/exp ν 3)))
              (+ (/ ν ρ) (* 2 (math/exp (math/tan φd) 2))))
        XII (* (/ (math/exp (math/cos φd) -1) (* 120 (math/exp ν 5)))
               (+ 5 (* 28 (math/exp (math/tan φd) 2)) (* 24 (math/exp (math/tan φd) 4))))
        XIIA (* (/ (math/exp (math/cos φd) -1) (* 5040 (math/exp ν 7)))
                (+ 61 
                   (* 662 (math/exp (math/tan φd) 2))
                   (* 1320 (math/exp (math/tan φd) 4))
                   (* 720 (math/exp (math/tan φd) 6))))]
    (* (/ 180 math/pi)
       (- (+ (- (+ λ0 (* et X)) (* (math/exp et 3) XI)) (* (math/exp et 5) XII)) (* (math/exp et 7) XIIA)))))

(defn easting-northing->latitude-longitude
  ([{easting :easting northing :northing}]
   (easting-northing->latitude-longitude {:easting easting
                                          :northing northing}
                                         national-grid-constants))
  ([{easting :easting northing :northing} constants]
   {:latitude (easting-northing->latitude easting northing constants)
    :longitude (easting-northing->longitude easting northing constants)}))
