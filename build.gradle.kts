plugins {
    id("java")
}

group = "org.maxnnsu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.apache.pdfbox:pdfbox:2.0.24")
    implementation("com.h2database:h2:1.4.200")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    compileOnly("org.projectlombok:lombok:1.18.28")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}