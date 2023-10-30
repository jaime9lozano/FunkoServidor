
plugins {
    id("java")
}

group = "jaime"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
dependencies {
    // Project Reactor
    implementation("io.projectreactor:reactor-core:3.5.10")
    // Para test: https://www.baeldung.com/reactive-streams-step-verifier-test-publisher
    // NO lo vamos a usar, pero lo dejo por si acaso
    // testImplementation("io.projectreactor:reactor-test:3.5.10")

    // R2DBC
    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")

    // Lombook para generar código, poner todo esto para que funcione
    implementation("org.projectlombok:lombok:1.18.28")
    testImplementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")

    // Logger
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // JWT
    implementation("com.auth0:java-jwt:4.2.1")

    // BCcrypt
    implementation("org.mindrot:jbcrypt:0.4")

    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
task("jarCliente", type = Jar::class) {
    archiveFileName = "client.jar"
    manifest {
        attributes["Main-Class"] = "jaime.client.Client"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
task("jarServidor", type = Jar::class) {
    archiveFileName = "server.jar"
    manifest {
        attributes["Main-Class"] = "jaime.server.Server"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
tasks.named("build") {
    dependsOn("jarCliente", "jarServidor")
}
tasks.test {
    useJUnitPlatform()
}