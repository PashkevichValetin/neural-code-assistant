

plugins {
	java
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.valentin"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val langchain4jVersion = "0.35.0"

dependencies {

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	runtimeOnly("org.postgresql:postgresql")
	implementation("org.apache.tika:tika-core:2.9.1")


	implementation("dev.langchain4j:langchain4j:$langchain4jVersion")
	implementation("dev.langchain4j:langchain4j-pgvector:$langchain4jVersion")
	implementation("dev.langchain4j:langchain4j-ollama:$langchain4jVersion")

	implementation("dev.langchain4j:langchain4j-core:${langchain4jVersion}")
	implementation("dev.langchain4j:langchain4j-document-parser-apache-tika:${langchain4jVersion}")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
