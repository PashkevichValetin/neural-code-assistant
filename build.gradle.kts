plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.valentin.rag"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://spring.io") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	implementation(platform("dev.langchain4j:langchain4j-bom:0.35.0"))
	implementation("dev.langchain4j:langchain4j")
	implementation("dev.langchain4j:langchain4j-ollama-spring-boot-starter")
	implementation("dev.langchain4j:langchain4j-pgvector")
	implementation("dev.langchain4j:langchain4j-document-parser-apache-tika")

	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
springBoot {
	mainClass.set("com.valentin.rag.NeuralCodeAssistantApplication")
}
