package dev.strategia.aplino.event

import dev.strategia.aplino.TestBase
import dev.strategia.aplino.error.ApplicationException
import dev.strategia.aplino.error.BaseErrorService
import dev.strategia.aplino.error.BaseRetryStrategy
import dev.strategia.aplino.error.ErrorPolicy
import dev.strategia.aplino.error.ErrorResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

internal class BaseEventServiceTest : TestBase() {

    @Test
    fun messageListener() {
        val service = getService()

        val echoListener = EchoMessageListener()
        service.addEventListener(echoListener, false, EchoMessage::class)

        val message = EchoMessage("Appragiator")
        service.send(message)
        Thread.sleep(10) // Wait the listener to process the message.
        assertEquals(1, echoListener.called)

        service.send(message)
        Thread.sleep(10) // Wait the listener to process the message.
        assertEquals(2, echoListener.called)

        service.removeEventListener(echoListener)

        service.send(message)
        Thread.sleep(10) // Wait the listener to process the message.
        assertEquals(2, echoListener.called)
    }

    @Test
    fun messageListeners() {
        val service = getService()
        val echoListener1 = EchoMessageListener()
        service.addEventListener(echoListener1, false, EchoMessage::class)
        val echoListener2 = EchoMessageListener()
        service.addEventListener(echoListener2, true, EchoMessage::class)

        val message = EchoMessage("Diamond")
        service.send(message)
        Thread.sleep(10) // Wait the listeners to process the message.
        assertEquals(1, echoListener1.called)
        assertEquals(1, echoListener2.called)
    }

    @Test
    fun errorRetry() {
        val service = getService()
        val echoListener = EchoMessageListener()
        echoListener.stopHandling = true
        service.addEventListener(echoListener, false, EchoMessage::class)

        val message = EchoMessage("Appragiator")
        try {
            service.process(message)
            fail("Expected ApplicationException was not thrown")
        } catch (_: ApplicationException) {
            assertEquals(2, echoListener.called)
        }
    }

    private fun getService(): BaseEventService {
        val errorService = CustomErrorService()
        errorService.start()
        val service = BaseEventService(errorService = errorService)
        service.start()
        return service
    }

    class CustomErrorService : BaseErrorService() {

        override fun handleError(listener: EventListener, exception: Throwable, event: AppEvent):
                                 ErrorResponse {
            val response = ErrorResponse(ErrorPolicy.Retry)
            response.retryStrategy = BaseRetryStrategy(2)
            return response
        }
    }
}
