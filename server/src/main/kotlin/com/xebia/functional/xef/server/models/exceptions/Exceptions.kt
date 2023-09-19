package com.xebia.functional.xef.server.models.exceptions

sealed class XefExceptions(
    override val message: String
): Throwable() {
    class ValidationException(override val message: String): XefExceptions(message)
    class OrganizationsException(override val message: String): XefExceptions(message)
    class ProjectException(override val message: String): XefExceptions(message)
    class AuthorizationException(override val message: String): XefExceptions(message)
    class UserException(override val message: String): XefExceptions(message)
}
