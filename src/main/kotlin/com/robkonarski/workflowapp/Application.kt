package com.robkonarski.workflowapp

import com.google.gson.*
import com.robkonarski.workflow.DatabaseConfig
import com.robkonarski.workflow.WorkflowEngine
import com.robkonarski.workflowapp.examples.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun Application.postgres() = DatabaseConfig.Postgres(
    url = environment.config.property("database.postgres.url").getString(),
    user = environment.config.property("database.postgres.user").getString(),
    password = environment.config.property("database.postgres.password").getString(),
    tablePrefix = "workflow_"
)

private fun Application.mysql() = DatabaseConfig.Mysql(
    url = environment.config.property("database.mysql.url").getString(),
    user = environment.config.property("database.mysql.user").getString(),
    password = environment.config.property("database.mysql.password").getString(),
    tablePrefix = ""
)

private fun Application.oracle() = DatabaseConfig.Oracle(
    url = environment.config.property("database.oracle.url").getString(),
    user = environment.config.property("database.oracle.user").getString(),
    password = environment.config.property("database.oracle.password").getString(),
    tablePrefix = ""
)

private fun Application.h2() = DatabaseConfig.H2(
    url = environment.config.property("database.h2.url").getString(),
    tablePrefix = ""
)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@Suppress("unused")
fun Application.module() {
    val workflowEngine = createEngine()
    //val workflowEngine = JavaApplication.createEngine(this)

    install(CORS) {
        anyHost()
        header("Authorization")
        allowNonSimpleContentTypes = true
    }

    install(CallLogging)

    install(ContentNegotiation) {
        gson { build() }
    }

    routing {
        route("/demo") {
            get {
                val workflows = demoWorkflow(DemoInput(100, "hello dear reader"), count = 10)
                workflowEngine.createWorkflows(workflows)
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }
}

private fun GsonBuilder.build() = apply {
    registerTypeAdapter(LocalDate::class.java, object : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?) =
            JsonPrimitive(src?.toString())

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?) =
            LocalDate.parse(json?.asString)
    })
    registerTypeAdapter(
        LocalDateTime::class.java,
        object : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
            override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?) =
                JsonPrimitive("${src?.toString()}Z")

            override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?) =
                LocalDateTime.parse(json?.asString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        })
}

private fun Application.createEngine() = WorkflowEngine.createDefault(
    //databaseConfig = h2(),
    //databaseConfig = postgres(),
    //databaseConfig = mysql(),
    databaseConfig = oracle(),
    registeredActivities = listOf(
        JavaInputActivity(),
        JavaTerminalActivity(),
        TestInputActivity(),
        TestStageOneActivity(),
        TestFinalActivity(),
        TestBrokerActivity(),
        DemoInputActivity(),
        DemoTerminalActivity()
    )
).apply { start() }
