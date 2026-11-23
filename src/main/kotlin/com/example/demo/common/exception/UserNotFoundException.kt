package com.example.demo.common.exception

class UserNotFoundException(email: String) :
    RuntimeException("User not found: $email")
