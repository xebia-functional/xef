package com.server.movile.xef.android.ui.screens

sealed class Screens(val screen: String) {
    object Start : Screens("start")
    object Home : Screens("home")
    object Organizations : Screens("organizations")
    object Assistants : Screens("assistants")
    object Projects : Screens("projects")
    object Chat : Screens("chat")
    object GenericQuestion : Screens("genericQuestion")
    object Settings : Screens("settings")
    object CreateAssistant : Screens("createAssistant")
}
