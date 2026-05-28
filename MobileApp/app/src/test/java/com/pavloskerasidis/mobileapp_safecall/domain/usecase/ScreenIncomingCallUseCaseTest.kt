package com.pavloskerasidis.mobileapp_safecall.domain.usecase

import com.pavloskerasidis.mobileapp_safecall.domain.repository.CallBlocklistRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenIncomingCallUseCaseTest {

    private val blocklist: CallBlocklistRepository = mockk()
    private val useCase = ScreenIncomingCallUseCase(blocklist)

    @Test
    fun `null number is allowed`() = runTest {
        assertEquals(ScreenIncomingCallUseCase.Decision.Allow, useCase(null))
    }

    @Test
    fun `blocked number is rejected`() = runTest {
        coEvery { blocklist.isBlocked("+30123") } returns true
        assertEquals(ScreenIncomingCallUseCase.Decision.Reject, useCase("+30123"))
    }

    @Test
    fun `unknown number is allowed`() = runTest {
        coEvery { blocklist.isBlocked("+30999") } returns false
        assertEquals(ScreenIncomingCallUseCase.Decision.Allow, useCase("+30999"))
    }
}
