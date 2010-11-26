#!/bin/sh
total=0; for i in `grep "^Tests run" test-output/*| cut -f 3 -d' '|cut -f1 -d','`; do total=$(( $total + $i )); done
echo total tests executed: $total
