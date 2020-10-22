#!/bin/zsh

set -eu

SCRIPT_DIR="$(dirname "$(readlink -f $0)")"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

JDK11_TAG_NAME=jdk-11.0.9+10
JDK11_NASHORN_DIR="src/jdk.scripting.nashorn/share/classes/"
JDK11_TGZ_URL="http://hg.openjdk.java.net/jdk-updates/jdk11u/archive/$JDK11_TAG_NAME.tar.gz/$JDK11_NASHORN_DIR"
TAGS_URL="http://hg.openjdk.java.net/jdk-updates/jdk11u/raw-file/tip/.hgtags"

WORKDIR="$REPO_DIR/work"

SRC_DIR="$REPO_DIR/src/main/java"
OUTPUT_DIR="$SRC_DIR/com/anatawa12/nashorn"

rm -rf "$WORKDIR"
mkdir -p "$WORKDIR"

function get_sources() {
  echo "getting source..."
  mkdir "$WORKDIR/jdk11u-$JDK11_TAG_NAME"
  curl -sL "$JDK11_TGZ_URL" | tar -x -C "$WORKDIR"
}

function move_sources() {
  echo "moving sources..."
  mv "$WORKDIR/jdk11u-$JDK11_TAG_NAME/$JDK11_NASHORN_DIR"/* "$SRC_DIR"
}

function move_package() {
  echo "moving package..."
  IN_JDK="$SRC_DIR/jdk/nashorn"

  mkdir -p "$OUTPUT_DIR"

  cd "$IN_JDK"

  sed_script="s/jdk.nashorn/com.anatawa12.nashorn/g"

  find . -type f > "$WORKDIR/files.txt"
  while read -r filePath; do
    mkdir -p "$(dirname "$OUTPUT_DIR/$filePath")"
    sed -E $sed_script < "$IN_JDK/$filePath" > "$OUTPUT_DIR/$filePath"
  done < "$WORKDIR/files.txt"

  rm -rf "$IN_JDK"
}

function move_module_info() {
  echo "moving module info..."
  mv "$SRC_DIR/module-info.java" "$SRC_DIR/module-info.java.txt"
}

function modify_each_file() {
  while read -r filePath; do
    mv "$OUTPUT_DIR/$filePath" "$OUTPUT_DIR/$filePath.tmp"
    "$@" < "$OUTPUT_DIR/$filePath.tmp" > "$OUTPUT_DIR/$filePath"
    rm "$OUTPUT_DIR/$filePath.tmp"
  done < "$WORKDIR/files.txt"
}

function remove_deprecated_flag() {
  echo "removing deprecated flag"
  modify_each_file grep -v "^@Deprecated(since=\"11\", forRemoval=true)"
  # shellcheck disable=SC2016
  modify_each_file awk '
    BEGIN{
      v = "false"
    }
    /^ \* @deprecated/ { v = "true" }
    /^ \* *(\*/ *)?$/ { v = "false" }
    {
      if (v == "false") print $0
      else print "//" $0
    }
    '
}

function write_commit_hash() {
    COMMIT_HASH=$(curl -sL "$TAGS_URL" | grep -F "$JDK11_TAG_NAME" | head -1 | cut -f 1 --delim=" ")
    echo "# do not edit this file." > "$REPO_DIR/remote-commit-hash.txt"
    echo "$COMMIT_HASH" >> "$REPO_DIR/remote-commit-hash.txt"
}

git checkout upstream-master
rm -rf "$SRC_DIR"
mkdir -p "$SRC_DIR"

get_sources
move_sources
write_commit_hash

cd "$REPO_DIR"
git add "."
git commit -m "sync with $COMMIT_HASH"
git checkout -b "automatic-changes"

move_module_info

cd "$REPO_DIR"
git add "."
git commit -m "move module info"

move_package

cd "$REPO_DIR"
git add "."
git commit -m "move package"

remove_deprecated_flag

cd "$REPO_DIR"
git add "."
git commit -m "remove deprecated flag"

git checkout master
git merge --no-ff automatic-change

rm -rf "$WORKDIR"
