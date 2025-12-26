package com.behnamuix.antisleep

 fun detectFaceShape(width: Float, height: Float): String {

    val ratio = height / width

    return when {
        ratio > 1.5f -> "مستطیلی"
        ratio in 1.35f..1.5f -> "بیضی"
        ratio in 1.15f..1.35f -> "گرد"
        ratio < 1.15f -> "مربعی"
        else -> "نامشخص"
    }
}