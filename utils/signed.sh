#!/bin/bash

# Check that the last commit message contains the "Signed-off-by <>" line

signature="$(git log -1 --show-signature |grep Signed-off-by:)"

if [ -z "$signature" ]; then
    echo "Signed-off-by is missing from the commit message; please run \"git commit -s --amend\""
    exit 1
fi
