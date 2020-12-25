package com.robkonarski.workflow.data

import com.robkonarski.workflow.DatabaseConfig

fun h2() = DatabaseConfig.H2("jdbc:h2:mem:test", tablePrefix = "", clearDatabase = true)

fun mysql() = DatabaseConfig.Mysql("jdbc:mysql://localhost:3306/workflow_engine_test?sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'&jdbcCompliantTruncation=false", tablePrefix = "", user = "root", password = "root")
