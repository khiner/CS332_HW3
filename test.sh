#!/bin/bash

# redirect compilation errors to temp file
javac compiler/*.java mini/*.java $1$2.java 2> $1java_error
ERROR=$(<$1java_error)

# cleanup temp error file
rm $1java_error

# check for Java compilation failures
if [ ! -z "$ERROR" ]; then
    echo $ERROR
    echo "Errors during Java compilation! Exiting test."
    exit
fi

# compile and display result
echo "Compiling $1$2.mini to $1$2.s"
COMP=$(java mini.Compiler $1$2.mini $1$2.s)
echo $COMP

# check for failures
if [ "$COMP" != "Total failures found: 0" ]; then
    echo "Errors during Mini compilation! Exiting test."
    exit
fi

gcc -m32 -o $1$2-mini $1$2.s runtime.c
cd $1
java $2 > $2-expected.out
./$2-mini > $2-actual.out
echo "Comparing output of $2.java and $2.mini"
DIFF=$(diff $2-expected.out $2-actual.out)
if [ -z "$DIFF" ]; then
    echo "Test Passed!"
else
    echo "Diff:"
    echo $DIFF
fi

#cleanup
rm $2.class $2.s $2-mini
