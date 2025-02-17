#!/usr/bin/env bash

set -ex

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR

pushd ../resources
rm *.dex || true
rm *.apk || true
popd

for folder in `find "$DIR" -type d -mindepth 1 -maxdepth 1`; do
  name=`basename $folder`
  pushd "$folder"
  rm -f *.class
  javac *.java
  cp *.class "../../resources/"

  if [ "desugaring" != "$name" ]; then
    zip -r "$folder.jar" *.class
    cp "$folder.jar" "../../resources/"
    dx --dex --output="$folder.dex" "$folder.jar"
    rm "$folder.jar"
    cp "$folder.dex" "../../resources/"
    rm "$folder.dex"
  fi

  rm *.class
  popd
done

pushd ../resources
zip three.apk params_joined.dex types.dex visibilities.dex
zip one.apk types.dex
zip three.jar Params.class Types.class Visibilities.class

zip empty.jar Params.class
zip -d empty.jar Params.class

cp three.jar classes.jar
zip three.aar classes.jar
rm classes.jar
