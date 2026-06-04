package grpc1.example.app

import grpc1.example.proto.HelloMessage
import grpc1.example.proto.HelloServiceGrpc
import grpc1.example.proto.StreamRequest
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
class HelloClient {

    private val log = LoggerFactory.getLogger(HelloClient::class.java)

    private val serverHost = System.getenv("GRPC_SERVER_HOST") ?: "localhost"
    private val serverPort = (System.getenv("GRPC_SERVER_PORT") ?: "9090").toInt()

    private val channel = ManagedChannelBuilder
        .forAddress(serverHost, serverPort)
        .usePlaintext()
        .build()

    private val stub = HelloServiceGrpc.newStub(channel)

    @EventListener(ApplicationReadyEvent::class)
    fun startListening() {
        log.info("Connecting to $serverHost:$serverPort")
        val thread = Thread(::runLoop, "grpc-client-thread")
        thread.isDaemon = false
        thread.start()
    }

    private fun runLoop() {
        while (true) {
            val latch = CountDownLatch(1)
            log.info("Subscribing to server stream...")
            stub.streamHello(StreamRequest.getDefaultInstance(), object : StreamObserver<HelloMessage> {
                override fun onNext(value: HelloMessage) {
                    log.info(">>> ${value.text}")
                }

                override fun onError(t: Throwable) {
                    log.warn("Stream error: ${t.message}. Reconnecting in 5s...")
                    latch.countDown()
                }

                override fun onCompleted() {
                    log.info("Stream completed. Reconnecting in 5s...")
                    latch.countDown()
                }
            })
            latch.await()
            Thread.sleep(5_000)
        }
    }
}
