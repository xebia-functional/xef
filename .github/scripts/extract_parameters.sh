#!/bin/bash

if [[ $1 == alpha-* ]]
then
  echo "stage=alpha" >> "$GITHUB_OUTPUT"
  echo "scope=$(echo "$1" | sed 's/alpha-\(.*\)/\1/')" >> "$GITHUB_OUTPUT"
else
  echo "stage=$1" >> "$GITHUB_OUTPUT"
  echo "scope=auto" >> "$GITHUB_OUTPUT"
fi
