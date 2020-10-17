plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    val vertxVersion = "3.9.3"

    implementation("io.vertx:vertx-core:${vertxVersion}")
    implementation("io.vertx:vertx-web:${vertxVersion}")
    implementation("io.vertx:vertx-pg-client:${vertxVersion}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.create<JavaExec>("run") {
    main = "vertx.casestudy.Main"
    classpath = sourceSets["main"].runtimeClasspath
}