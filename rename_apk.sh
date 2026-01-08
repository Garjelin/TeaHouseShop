#!/bin/bash

VERSION_NAME=$(grep 'projectVersionName' gradle/libs.versions.toml | cut -d'"' -f2)

find app/build/outputs/apk -name "*.apk" ! -name "*unaligned*" -exec bash -c '
    for file; do 
        dir=$(dirname "$file")
        mv "$file" "$dir/teahouse-'"$VERSION_NAME"'-${dir##*/}.apk"
    done
' bash {} +

echo "✅ Renamed with version: $VERSION_NAME"

