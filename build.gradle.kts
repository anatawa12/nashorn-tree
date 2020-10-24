/*
 * This code is licensed under CC0.
 * http://creativecommons.org/publicdomain/zero/1.0/deed.ja
 */

plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jruby.joni:joni:2.1.40")
    testCompile("junit", "junit", "4.12")
}
