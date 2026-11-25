package com.example.demo.common.exception

class EmailAlreadyExistsException(
    email: String,
) : RuntimeException("Email already in use: $email")
