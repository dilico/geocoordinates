(ns geocoordinates.core-test
  (:require [clojure.test :refer :all]
            [geocoordinates.core :as geo]))

(deftest test-easting-northing->latitude-longitude
  (testing "convert easting and northing to latitude and longitude using National Grid constants"
    (are [easting northing latitude longitude] 
        (= (geo/easting-northing->latitude-longitude {:easting easting
                                                      :northing northing}
                                                     :national-grid) {:latitude latitude
                                                                      :longitude longitude})
      651409.903 313177.27 52.65757030193327 1.7179215806451056
      363606.5 172914.3 51.45355057024397 -2.52382540824567)))

(deftest test-latitude-longitude->easting-northing
  (testing "convert latitude and longitude to easting and northing using National Grid constants"
    (are [easting northing latitude longitude] 
        (= (geo/latitude-longitude->easting-northing {:latitude latitude
                                                      :longitude longitude}
                                                     :national-grid) {:easting easting
                                                                      :northing northing})
      651409.902802227 313177.2699187288 52.65757030193327 1.7179215806451056
      363606.5000000153 172914.2999999999 51.45355057024397 -2.52382540824567)))
