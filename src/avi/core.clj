(ns avi.core
  (:import [avi.terminal Screen])
  (:require [avi.buffer :as b]
            [avi.normal :as normal]
            [avi.render :as render])
  (:gen-class))

(defn start
  [[lines columns] filename]
  {:mode :normal
   :buffer (b/open filename)
   :lines lines
   :columns columns
   :count nil
   :beep? false})

(defmulti process (fn [_ [event-kind]]
                    event-kind))

(defmethod process :keystroke
  [editor [_ event-data]]
  (normal/process editor event-data))

(defmethod process :resize
  [editor [_ [lines columns]]]
  (assoc editor
         :lines lines
         :columns columns))

(defn- update-screen
  [editor]
  (let [{chars :chars,
         attrs :attrs,
         width :width,
         [i j] :cursor} (render/render editor)]
    (Screen/refresh i j width chars attrs)))

(defn- screen-size
  []
  (let [size (Screen/size)]
    [(get size 0) (get size 1)]))

(defn -main
  [filename]
  (Screen/start)
  (loop [[height width] (screen-size)
         editor (start [height width] filename)]
    (if (:beep? editor)
      (Screen/beep))
    (let [editor (if (or (not= width (:columns editor))
                         (not= height (:lines editor)))
                   (process editor [:resize [height width]])
                   editor)]
      (update-screen editor)
      (if-not (= (:mode editor) :finished)
        (recur
          (screen-size)
          (process editor [:keystroke (Screen/getch)])))))
  (Screen/stop))
