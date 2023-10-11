package com.xebia.functional.xef.conversation.mlflow

import com.xebia.functional.xef.mlflow.MlflowClient

fun printModel(model: MlflowClient.RouteModel): String =
    "(name = '${model.name}', provider = '${model.provider}')"

fun printRoute(r: MlflowClient.RouteDefinition): String =
    """
        Name: ${r.name}
        * Route type: ${r.routeType}
        * Route url: ${r.routeUrl}
        * Model: ${printModel(r.model)}
    """.trimIndent()