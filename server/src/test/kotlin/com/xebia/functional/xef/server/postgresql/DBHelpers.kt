package com.xebia.functional.xef.server.postgresql

import com.xebia.functional.xef.server.db.tables.Organization
import com.xebia.functional.xef.server.db.tables.Project
import com.xebia.functional.xef.server.db.tables.User
import org.jetbrains.exposed.dao.id.EntityID

object DBHelpers {

    fun testUser(
        fName: String = "test",
        fEmail: String = "test@test/com",
        fPasswordHash: String = "passwordTest",
        fSalt: String = "saltTest",
        fAuthToken: String = "authTokenTest"
    ): User {
        return User.new {
            name = fName
            email = fEmail
            passwordHash = fPasswordHash
            salt = fSalt
            authToken = fAuthToken
        }
    }

    fun testOrganization(
        fName: String = "testOrg",
        fOwnerId: EntityID<Int>
    ): Organization {
        return Organization.new {
            name = fName
            ownerId = fOwnerId
        }
    }

    fun testProject(
        fName: String = "testProject",
        fOrgId: EntityID<Int>
    ): Project {
        return Project.new {
            name = fName
            orgId = fOrgId
        }
    }

}
