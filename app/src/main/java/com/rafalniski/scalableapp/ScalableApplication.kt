package com.rafalniski.scalableapp

import android.app.Application

class ScalableApplication : Application() {
    val container by lazy { AppContainer() }
}
