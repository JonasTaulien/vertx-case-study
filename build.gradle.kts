plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {

}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.create<JavaExec>("run") {
    main = "vertx.casestudy.Main"
    classpath = sourceSets["main"].runtimeClasspath
}