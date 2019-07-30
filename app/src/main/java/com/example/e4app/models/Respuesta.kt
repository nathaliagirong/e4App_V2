package com.example.e4app.models

data class Respuesta(
        val code: Int = 0,
        var description: String? = null,
        var response: Any = {}
)