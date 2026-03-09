package com.dens.eduquiz

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform