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
