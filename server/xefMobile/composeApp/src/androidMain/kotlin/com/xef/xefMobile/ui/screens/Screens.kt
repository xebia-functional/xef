package com.xef.xefMobile.ui.screens

sealed class Screens(val screen: String) {
  object Login : Screens("loginScreen")

  object Register : Screens("registerScreen")

  object Start : Screens("startScreen")

  object Home : Screens("homeScreen")

  object Organizations : Screens("organizationsScreen")

  object Assistants : Screens("assistantsScreen")

  object Projects : Screens("projectsScreen")

  object Chat : Screens("chatScreen")

  object GenericQuestion : Screens("genericQuestionScreen")

  object Settings : Screens("settingsScreen")

  object CreateAssistant : Screens("createAssistantScreen")

  object CreateAssistantWithArgs : Screens("createAssistantScreen/{assistantId}") {
    fun createRoute(assistantId: String) = "createAssistantScreen/$assistantId"
  }
}
