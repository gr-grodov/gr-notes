
POST("http://localhost:8090/api/notes") {
    contentType("application/json")
    body(
        """
        {
            "title": "Первая заметка",
            "content": "Контент"
        }
        """.trimIndent()
    )

}

GET("http://localhost:8090/api/notes")