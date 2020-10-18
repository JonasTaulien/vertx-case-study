plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    val vertxVersion = "3.9.3"
    val junit5Version = "5.5.1"

    // Vert.x
    implementation("io.vertx:vertx-web:${vertxVersion}")
    implementation("io.vertx:vertx-pg-client:${vertxVersion}")
    implementation("io.vertx:vertx-auth-jwt:${vertxVersion}")
    implementation("io.vertx:vertx-config:${vertxVersion}")
    implementation("io.vertx:vertx-rx-java2:${vertxVersion}")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // DI
    implementation("com.google.inject:guice:4.2.3")

    // Testing
    testImplementation("io.vertx:vertx-junit5:${vertxVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junit5Version}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junit5Version}")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("io.rest-assured:rest-assured:4.3.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.create<JavaExec>("run") {
    main = "vertx.casestudy.Main"
    classpath = sourceSets["main"].runtimeClasspath
    systemProperties["vertx.logger-delegate-factory-class-name"] = "io.vertx.core.logging.SLF4JLogDelegateFactory"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}