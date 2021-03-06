(ns avi.normal-test
  (:require [midje.sweet :refer :all]
            [avi.test-helpers :refer :all]))

(facts "regarding repeating commands"
  (fact "`1` through `9` can be used as repeat counts."
    (cursor :when-editing "0123456789x" :after-typing "1l") => [0 1]
    (cursor :when-editing "0123456789x" :after-typing "2l") => [0 2]
    (cursor :when-editing "0123456789x" :after-typing "3l") => [0 3]
    (cursor :when-editing "0123456789x" :after-typing "4l") => [0 4]
    (cursor :when-editing "0123456789x" :after-typing "5l") => [0 5]
    (cursor :when-editing "0123456789x" :after-typing "6l") => [0 6]
    (cursor :when-editing "0123456789x" :after-typing "7l") => [0 7]
    (cursor :when-editing "0123456789x" :after-typing "8l") => [0 8]
    (cursor :when-editing "0123456789x" :after-typing "9l") => [0 9])
  (fact "Multiple digits can be used as the repeat count."
    (cursor :when-editing "0000000000111111111" :after-typing "17l") => [0 17])
  (fact "`0` can be used in a repeat count."
    (cursor :when-editing "0000000000111111111" :after-typing "10l") => [0 10])
  (fact "The repeat goes away after a command is executed."
    (cursor :when-editing "0123456789x" :after-typing "4ll") => [0 5])
  (fact "None of the digits clear the repeat count."
    (:count (editor :after-typing "1234567890")) => 1234567890))

(facts "regarding cursor movement"
  (fact "The cursor starts on line 1, column 0."
    (cursor) => [0 0])

  (facts "about `j`"
    (fact "`j` moves the cursor down one line."
      (cursor :after-typing "j") => [1 0]
      (cursor :after-typing "jj") => [2 0])
    (fact "`j` can move to a zero-length line."
      (cursor :when-editing "One\n\nTwo" :after-typing "j") => [1 0])
    (fact "`j` won't move the cursor below the last line."
      (cursor :after-typing "jjjj") => [3 0]
      (editor :after-typing "jjjj") => beeped)
    (fact "`j` won't place the cursor after the end of the line."
      (cursor :when-editing "Hello\nOne" :after-typing "llllj") => [1 2])
    (fact "`j` remembers the last explicitly-set column."
      (cursor :when-editing "Hello\n.\nThere" :after-typing "lljj") => [2 2]))

  (facts "about `k`"
    (fact "`k` moves the cursor up one line."
      (cursor :after-typing "jk") => [0 0]
      (cursor :after-typing "jjjkk") => [1 0])
    (fact "`k` won't move above the first line."
      (cursor :after-typing "k") => [0 0]
      (editor :after-typing "k") => beeped
      (editor :after-typing "kj") => did-not-beep)
    (fact "`k` can move to a zero-length line."
      (cursor :when-editing "\nOne" :after-typing "jk") => [0 0])
    (fact "`k` won't place the cursor after the end of the line."
      (cursor :when-editing "One\nHello" :after-typing "jllllk") => [0 2]))

  (facts "about `l`"
    (fact "`l` moves to the right one character."
      (cursor :after-typing "l") => [0 1]
      (cursor :after-typing "ll") => [0 2])
    (fact "`l` won't move beyond the end of the line."
      (cursor :after-typing "lll") => [0 2]
      (editor :after-typing "lll") => beeped))

  (facts "about `h`"
    (fact "`h` moves to the left one character."
      (cursor :after-typing "llh") => [0 1])
    (fact "`h` won't move before the beginning of the line."
      (cursor :after-typing "h") => [0 0]
      (editor :after-typing "h") => beeped))

  (facts "about moving to the beginning or end of line"
    (fact "`0` moves to the first character on the line."
      (cursor :after-typing "ll0") => [0 0]
      (editor :when-editing "\n" :after-typing "0") => did-not-beep)

    (fact "`$` moves to the last character on the line."
      (cursor :after-typing "$") => [0 2])
    (fact "`^` moves to the first non-space character"
      (cursor :when-editing "bob" :after-typing "$^") => [0 0]
      (cursor :when-editing "  bob" :after-typing "$^") => [0 2]
      (editor :when-editing ".\n\n." :after-typing "j^") => did-not-beep
      (cursor :when-editing "   " :after-typing "0^") => [0 2]))

  (facts "about `G`"
    (fact "`G` moves to the last line."
      (cursor :when-editing ".\n.\nThree" :after-typing "G") => [2 0])
    (fact "`G` moves to the line in the count register."
      (cursor :when-editing ".\n.\nThree" :after-typing "2G") => [1 0])))
